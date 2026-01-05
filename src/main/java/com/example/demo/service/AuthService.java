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

    // Generate JWT token
    CustomUserDetails userDetails = new CustomUserDetails(savedUser);
    String token = jwtService.generateToken(userDetails);

    return buildAuthResponse(token, savedUser);
  }

  /** Login user */
  public AuthResponse login(AuthRequest request) {
    // Authenticate user
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    // Generate JWT token
    String token = jwtService.generateToken(userDetails);

    return buildAuthResponse(token, userDetails.getUser());
  }

  /** Logout user by blacklisting the token */
  public void logout(String token) {
    // Extract expiration to calculate TTL
    java.time.Instant expiration = jwtService.extractExpirationInstant(token);
    long ttlSeconds = java.time.Duration.between(java.time.Instant.now(), expiration).getSeconds();

    if (ttlSeconds > 0) {
      tokenBlacklistService.blacklistToken(token, ttlSeconds);
    }
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
  }

  /** Build AuthResponse from token and user */
  private AuthResponse buildAuthResponse(String token, User user) {
    Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    return new AuthResponse(
        token,
        "Bearer",
        jwtService.extractExpirationInstant(token),
        user.getUsername(),
        user.getEmail(),
        roleNames);
  }
}
