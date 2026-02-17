package com.example.demo.service;

import com.example.demo.constants.ErrorMessage;
import com.example.demo.dto.AuthResponse;
import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.enums.RoleName;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth Service.
 *
 * <p>Handles OAuth authentication operations including:
 *
 * <ul>
 *   <li>Google OAuth 2.0 authentication
 *   <li>User creation for new OAuth users
 *   <li>JWT token generation for OAuth users
 * </ul>
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService implements IOAuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserProfileRepository userProfileRepository;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  @Value("${app.google.client-id:}")
  private String googleClientId;

  /**
   * Authenticates a user using Google ID Token.
   *
   * @param idTokenString the Google ID token string
   * @return AuthResponse containing JWT tokens and user information
   */
  @Override
  @Transactional
  public AuthResponse loginWithGoogle(String idTokenString) {
    try {
      GoogleIdTokenVerifier verifier =
          new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
              .setAudience(Collections.singletonList(googleClientId))
              .build();

      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken == null) {
        throw new IllegalArgumentException(ErrorMessage.INVALID_GOOGLE_TOKEN);
      }

      GoogleIdToken.Payload payload = idToken.getPayload();
      String email = payload.getEmail();
      String name = (String) payload.get("name");

      if (email == null) {
        throw new IllegalArgumentException(ErrorMessage.EMAIL_NOT_IN_TOKEN);
      }

      // Check if user exists
      User user =
          userRepository
              .findByEmail(email)
              .orElseGet(
                  () -> {
                    // Create new user
                    return createGoogleUser(email, name);
                  });

      // Update auth provider if currently LOCAL (Account Linking) or ensure it's set
      if (user.getAuthProvider() == null) {
        user.setAuthProvider(AuthProvider.GOOGLE);
        userRepository.save(user);
      }

      // Generate tokens
      CustomUserDetails userDetails = new CustomUserDetails(user);
      String token = jwtService.generateToken(userDetails);
      String refreshToken = jwtService.generateRefreshToken(userDetails);

      // Store refresh token
      refreshTokenService.createRefreshToken(
          user.getId(), jwtService.getRefreshExpirationSeconds());

      return buildAuthResponse(token, refreshToken, user);

    } catch (Exception e) {
      log.error("Google Login Failed", e);
      throw new IllegalArgumentException("Google Login Failed: " + e.getMessage());
    }
  }

  /**
   * Creates a new user from Google OAuth data.
   *
   * @param email the user's email from Google
   * @param name the user's name from Google
   * @return the created User entity
   */
  private User createGoogleUser(String email, String name) {
    Role userRole =
        roleRepository
            .findByName(RoleName.USER.getRoleName())
            .orElseGet(
                () -> {
                  Role newRole = Role.builder().name(RoleName.USER.getRoleName()).build();
                  return roleRepository.save(newRole);
                });

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    User user =
        User.builder()
            .username(email) // Use email as username for Google users
            .email(email)
            .password(null) // No password
            .authProvider(AuthProvider.GOOGLE)
            .isActive(true)
            .roles(roles)
            .build();

    User savedUser = userRepository.save(user);

    // Create empty UserProfile for USER role
    UserProfile profile = UserProfile.builder().user(savedUser).build();
    userProfileRepository.save(profile);

    return savedUser;
  }

  /**
   * Builds an AuthResponse from token and user data.
   *
   * @param token the access token
   * @param refreshToken the refresh token
   * @param user the user entity
   * @return AuthResponse containing all authentication data
   */
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
