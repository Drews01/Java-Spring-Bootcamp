package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for authentication endpoints.
 *
 * <p>**Property 2: Authentication Response Format Preservation**
 *
 * <p>These tests validate that after refactoring (extracting OAuthService, implementing
 * interfaces), all authentication endpoints continue to return the correct AuthResponse format with
 * all required fields.
 *
 * <p>**Validates: Requirements 1.5, 1.6**
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenticationEndpointsIntegrationTest {

  @Autowired private com.example.demo.service.AuthService authService;

  @Autowired private com.example.demo.service.OAuthService oAuthService;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @MockBean private com.example.demo.service.RefreshTokenService refreshTokenService;

  private Role userRole;

  @BeforeEach
  void setUp() {
    // Clean up test data - only delete users, keep roles
    userRepository.deleteAll();

    // Get or create USER role
    if (!roleRepository.findByName("USER").isPresent()) {
      userRole = Role.builder().name("USER").build();
      roleRepository.save(userRole);
    } else {
      userRole = roleRepository.findByName("USER").get();
    }
  }

  @Test
  void register_ShouldReturnCorrectAuthResponseFormat() {
    // Given
    RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

    // When
    AuthResponse response = authService.register(request);

    // Then - Validate AuthResponse structure
    assertNotNull(response, "AuthResponse should not be null");
    assertNotNull(response.token(), "Access token should not be null");
    assertNotNull(response.refreshToken(), "Refresh token should not be null");
    assertEquals("Bearer", response.tokenType(), "Token type should be Bearer");
    assertNotNull(response.expiresAt(), "Access token expiry should not be null");
    assertNotNull(response.refreshExpiresAt(), "Refresh token expiry should not be null");
    assertEquals("testuser", response.username(), "Username should match");
    assertEquals("test@example.com", response.email(), "Email should match");
    assertNotNull(response.roles(), "Roles should not be null");
    assertTrue(response.roles().contains("USER"), "Roles should contain USER");

    // Validate token expiry times are in the future
    assertTrue(
        response.expiresAt().isAfter(Instant.now()), "Access token expiry should be in the future");
    assertTrue(
        response.refreshExpiresAt().isAfter(Instant.now()),
        "Refresh token expiry should be in the future");
  }

  @Test
  void login_ShouldReturnCorrectAuthResponseFormat() {
    // Given - Create a user first
    User user =
        User.builder()
            .username("loginuser")
            .email("login@example.com")
            .password(passwordEncoder.encode("password123"))
            .isActive(true)
            .roles(Set.of(userRole))
            .build();
    userRepository.save(user);

    AuthRequest request = new AuthRequest("loginuser", "password123", null, null, null);

    // When
    AuthResponse response = authService.login(request);

    // Then - Validate AuthResponse structure
    assertNotNull(response, "AuthResponse should not be null");
    assertNotNull(response.token(), "Access token should not be null");
    assertNotNull(response.refreshToken(), "Refresh token should not be null");
    assertEquals("Bearer", response.tokenType(), "Token type should be Bearer");
    assertNotNull(response.expiresAt(), "Access token expiry should not be null");
    assertNotNull(response.refreshExpiresAt(), "Refresh token expiry should not be null");
    assertEquals("loginuser", response.username(), "Username should match");
    assertEquals("login@example.com", response.email(), "Email should match");
    assertNotNull(response.roles(), "Roles should not be null");
    assertTrue(response.roles().contains("USER"), "Roles should contain USER");

    // Validate token expiry times are in the future
    assertTrue(
        response.expiresAt().isAfter(Instant.now()), "Access token expiry should be in the future");
    assertTrue(
        response.refreshExpiresAt().isAfter(Instant.now()),
        "Refresh token expiry should be in the future");
  }

  @Test
  void googleLogin_ShouldReturnCorrectAuthResponseFormat_WhenTokenIsInvalid() {
    // Note: Testing Google login with valid tokens requires actual Google tokens
    // This test validates that the service throws appropriate exceptions
    // Full integration testing with valid Google tokens should be done in E2E tests

    String invalidToken = "invalid-google-token";

    // When/Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> oAuthService.loginWithGoogle(invalidToken));

    assertTrue(
        exception.getMessage().contains("Google Login Failed"),
        "Exception message should indicate Google login failure");
  }

  @Test
  void authResponse_ShouldHaveAllRequiredFields() {
    // This test validates the AuthResponse structure itself
    // by creating a response and checking all fields are present

    RegisterRequest request =
        new RegisterRequest("structuretest", "structure@example.com", "password123");
    AuthResponse response = authService.register(request);

    // Validate all fields are present (not null)
    assertAll(
        "AuthResponse should have all required fields",
        () -> assertNotNull(response.token(), "token field should be present"),
        () -> assertNotNull(response.refreshToken(), "refreshToken field should be present"),
        () -> assertNotNull(response.tokenType(), "tokenType field should be present"),
        () -> assertNotNull(response.expiresAt(), "expiresAt field should be present"),
        () ->
            assertNotNull(response.refreshExpiresAt(), "refreshExpiresAt field should be present"),
        () -> assertNotNull(response.username(), "username field should be present"),
        () -> assertNotNull(response.email(), "email field should be present"),
        () -> assertNotNull(response.roles(), "roles field should be present"));
  }

  @Test
  void authResponse_TokensShouldBeDifferent() {
    // Validate that access token and refresh token are different
    RegisterRequest request = new RegisterRequest("tokentest", "token@example.com", "password123");
    AuthResponse response = authService.register(request);

    assertNotEquals(
        response.token(),
        response.refreshToken(),
        "Access token and refresh token should be different");
  }

  @Test
  void authResponse_RefreshTokenShouldExpireLater() {
    // Validate that refresh token expires after access token
    RegisterRequest request =
        new RegisterRequest("expirytest", "expiry@example.com", "password123");
    AuthResponse response = authService.register(request);

    assertTrue(
        response.refreshExpiresAt().isAfter(response.expiresAt()),
        "Refresh token should expire after access token");
  }
}
