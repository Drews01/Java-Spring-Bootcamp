package com.example.demo.service;

import com.example.demo.constants.ErrorMessage;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.enums.RoleName;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.JwtService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service.
 *
 * <p>Handles user authentication operations including:
 *
 * <ul>
 *   <li>User registration with email and password
 *   <li>Local and Google OAuth 2.0 authentication
 *   <li>JWT token generation and refresh
 *   <li>Password reset via email
 *   <li>Session management and logout
 * </ul>
 *
 * <p>Security features:
 *
 * <ul>
 *   <li>Passwords are encrypted using BCrypt
 *   <li>JWT tokens stored in HttpOnly cookies
 *   <li>Refresh token rotation for enhanced security
 *   <li>Token blacklisting on logout
 * </ul>
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 * @see JwtService
 * @see RefreshTokenService
 * @see TokenBlacklistService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserProfileRepository userProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;
  private final TokenBlacklistService tokenBlacklistService;
  private final PasswordResetService passwordResetService;
  private final EmailService emailService;
  private final FCMService fcmService;

  /**
   * Registers a new user in the system.
   *
   * <p>This method performs the following operations:
   *
   * <ol>
   *   <li>Validates that username and email are unique
   *   <li>Creates user with BCrypt-encrypted password
   *   <li>Assigns default USER role
   *   <li>Creates an empty UserProfile for the new user
   *   <li>Generates JWT access and refresh tokens
   * </ol>
   *
   * @param request the registration request containing username, email, and password
   * @return AuthResponse containing JWT tokens and user information
   * @throws IllegalArgumentException if username or email already exists
   */
  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException(ErrorMessage.USERNAME_EXISTS);
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException(ErrorMessage.EMAIL_EXISTS);
    }

    // Get or create USER role
    Role userRole =
        roleRepository
            .findByName(RoleName.USER.getRoleName())
            .orElseGet(
                () -> {
                  Role newRole = Role.builder().name(RoleName.USER.getRoleName()).build();
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

    // Create empty UserProfile for USER role
    UserProfile profile = UserProfile.builder().user(savedUser).build();
    userProfileRepository.save(profile);

    // Generate tokens
    CustomUserDetails userDetails = new CustomUserDetails(savedUser);
    String token = jwtService.generateToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    // Store refresh token
    refreshTokenService.createRefreshToken(
        savedUser.getId(), jwtService.getRefreshExpirationSeconds());

    return buildAuthResponse(token, refreshToken, savedUser);
  }

  /**
   * Authenticates a user with username/email and password.
   *
   * <p>On successful authentication:
   *
   * <ul>
   *   <li>Generates new JWT access and refresh tokens
   *   <li>Stores refresh token in Redis for validation
   *   <li>Optionally saves FCM device token for push notifications
   * </ul>
   *
   * @param request the login request containing credentials and optional FCM token
   * @return AuthResponse containing JWT tokens and user information
   * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
   */
  @Override
  public AuthResponse login(AuthRequest request) {
    // Authenticate user
    log.info("Login attempt for: {}", request.usernameOrEmail());
    if (request.fcmToken() != null) {
      log.info("Received FCM Token length: {}", request.fcmToken().length());
    } else {
      log.warn("No FCM Token received in login request");
    }

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

    // Save FCM device token if provided (for push notifications)
    if (request.fcmToken() != null && !request.fcmToken().isBlank()) {
      fcmService.saveDeviceToken(
          userDetails.getUser(), request.fcmToken(), request.deviceName(), request.platform());
    }

    return buildAuthResponse(token, refreshToken, userDetails.getUser());
  }

  /** Refresh access token */
  @Override
  public AuthResponse refreshAccessToken(String refreshToken) {
    // 1. Validate refresh token in Redis
    Long userId =
        refreshTokenService
            .validateRefreshToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.INVALID_REFRESH_TOKEN));

    // 2. Validate JWT signature and expiration
    // We create a dummy/temp user details just for validation or fetch user?
    // Better to fetch user to ensure they still exist and are active
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format(ErrorMessage.USER_NOT_FOUND, userId)));

    if (!Boolean.TRUE.equals(user.getIsActive())) {
      throw new IllegalArgumentException(ErrorMessage.USER_INACTIVE);
    }

    CustomUserDetails userDetails = new CustomUserDetails(user);
    if (!jwtService.isTokenValid(refreshToken, userDetails)) {
      throw new IllegalArgumentException(ErrorMessage.INVALID_REFRESH_TOKEN);
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
  @Override
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
  @Override
  @Transactional
  public void forgotPassword(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format(ErrorMessage.USER_NOT_FOUND_EMAIL, email)));

    // Create reset token (valid for 1 hour) in Redis
    String token = passwordResetService.createPasswordResetToken(user.getId(), 1);

    // Send email with frontend URL
    String resetLink = "https://starapp.my.id/reset-password?token=" + token;
    emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
  }

  /** Reset password */
  @Override
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
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format(ErrorMessage.USER_NOT_FOUND, userId)));

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
