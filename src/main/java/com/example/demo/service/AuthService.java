package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.JwtService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Authentication Service Handles user registration and login logic */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;
  private final TokenBlacklistService tokenBlacklistService;
  private final PasswordResetService passwordResetService;
  private final EmailService emailService;

  /** Register a new user */
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username already exists");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already exists");
    }

    // Get or create USER role
    Role userRole =
        roleRepository
            .findByName("USER")
            .orElseGet(
                () -> {
                  Role newRole = Role.builder().name("USER").build();
                  return roleRepository.save(newRole);
                });

    // Create new user
    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    User user =
        User.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .isActive(true)
            .roles(roles)
            .build();

    User savedUser = userRepository.save(user);

    // Generate tokens
    CustomUserDetails userDetails = new CustomUserDetails(savedUser);
    String token = jwtService.generateToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    // Store refresh token
    refreshTokenService.createRefreshToken(
        savedUser.getId(), jwtService.getRefreshExpirationSeconds());

    return buildAuthResponse(token, refreshToken, savedUser);
  }

  /** Login user */
  public AuthResponse login(AuthRequest request) {
    // Authenticate user
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal(); // fixed cast

    // Generate tokens
    String token = jwtService.generateToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    // Store refresh token
    refreshTokenService.createRefreshToken(
        userDetails.getUser().getId(), jwtService.getRefreshExpirationSeconds());

    return buildAuthResponse(token, refreshToken, userDetails.getUser());
  }

  /** Refresh access token */
  public AuthResponse refreshAccessToken(String refreshToken) {
    // 1. Validate refresh token in Redis
    Long userId =
        refreshTokenService
            .validateRefreshToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));

    // 2. Validate JWT signature and expiration
    // We create a dummy/temp user details just for validation or fetch user?
    // Better to fetch user to ensure they still exist and are active
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!Boolean.TRUE.equals(user.getIsActive())) {
      throw new IllegalArgumentException("User is inactive");
    }

    CustomUserDetails userDetails = new CustomUserDetails(user);
    if (!jwtService.isTokenValid(refreshToken, userDetails)) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    // 3. Generate new access token
    String newAccessToken = jwtService.generateToken(userDetails);

    // 4. Rotate refresh token (optional, but good practice - let's keep simple for
    // now or
    // re-issue?)
    // For now, return same refresh token, OR generate new one. Plan said "Token
    // Rotation" options.
    // Let's implement rotation: revoke old, issue new.
    refreshTokenService.revokeRefreshToken(refreshToken);
    String newRefreshToken = jwtService.generateRefreshToken(userDetails);
    refreshTokenService.createRefreshToken(userId, jwtService.getRefreshExpirationSeconds());

    return buildAuthResponse(newAccessToken, newRefreshToken, user);
  }

  /** Logout user by blacklisting the token */
  public void logout(String token) {
    // Extract expiration to calculate TTL
    java.time.Instant expiration = jwtService.extractExpirationInstant(token);
    long ttlSeconds = java.time.Duration.between(java.time.Instant.now(), expiration).getSeconds();

    if (ttlSeconds > 0) {
      tokenBlacklistService.blacklistToken(token, ttlSeconds);
    }

    // If we had the refresh token in the request, we could revoke it here too.
    // Client should probably discard it.
    // NOTE: The logout endpoint only receives access token.
    // To revoke refresh token, we'd need it passed or revoke all?
    // Standard OAuth2 logout often takes refresh_token as hint.
    // For now, only access token is blacklisted.
  }

  /** Initiate password reset */
  @Transactional
  public void forgotPassword(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    // Create reset token (valid for 1 hour) in Redis
    String token = passwordResetService.createPasswordResetToken(user.getId(), 1);

    // Send email
    String resetLink = "http://localhost:8080/reset-password?token=" + token;
    emailService.sendSimpleMessage(
        user.getEmail(),
        "Password Reset Request",
        "To reset your password, click the link below:\n" + resetLink);
  }

  /** Reset password */
  @Transactional
  public void resetPassword(String token, String newPassword) {
    Long userId =
        passwordResetService
            .validateToken(token)
            .orElseThrow(
                () -> new IllegalArgumentException("Invalid or expired password reset token"));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Update password
    user.setPassword(passwordEncoder.encode(newPassword));

    // Update last password reset date to invalidate existing sessions
    user.setLastPasswordResetDate(java.time.LocalDateTime.now());
    userRepository.save(user);

    // Mark token as used (delete from Redis)
    passwordResetService.markTokenAsUsed(token);

    // Revoke all refresh tokens for this user
    refreshTokenService.revokeAllUserTokens(userId);
  }

  /** Build AuthResponse from token and user */
  private AuthResponse buildAuthResponse(String token, String refreshToken, User user) {
    Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    java.time.Instant accessExpiry = jwtService.extractExpirationInstant(token);
    java.time.Instant refreshExpiry = jwtService.extractExpirationInstant(refreshToken);

    return new AuthResponse(
        token,
        refreshToken,
        "Bearer",
        accessExpiry,
        refreshExpiry,
        user.getUsername(),
        user.getEmail(),
        roleNames);
  }
}
