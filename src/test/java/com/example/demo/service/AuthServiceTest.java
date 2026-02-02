package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private UserProfileRepository userProfileRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private TokenBlacklistService tokenBlacklistService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private PasswordResetService passwordResetService;
  @Mock private EmailService emailService;

  @InjectMocks private AuthService authService;

  private User user;
  private Role userRole;

  @BeforeEach
  void setUp() {
    userRole = Role.builder().id(1L).name("USER").build();
    user =
        User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("encodedPassword")
            .roles(new HashSet<>(Collections.singletonList(userRole)))
            .isActive(true)
            .build();

    ReflectionTestUtils.setField(authService, "userProfileRepository", userProfileRepository);
  }

  @Test
  void register_WithValidData_ShouldCreateUser() {
    RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password");

    when(userRepository.existsByUsername(request.username())).thenReturn(false);
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user); // Should return saved user
    when(jwtService.generateToken(any())).thenReturn("jwtToken");
    // Mock user repo check inside buildAuthResponse too if needed, or ensure user
    // is correctly passed

    // The service might be re-fetching user or doing logic.
    // Assuming simple save and token generation.

    AuthResponse response = authService.register(request);

    assertNotNull(response);
    assertEquals("jwtToken", response.token());
  }

  @Test
  void register_WithExistingEmail_ShouldThrowException() {
    RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password");
    when(userRepository.existsByUsername("testuser")).thenReturn(false);
    when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> authService.register(request));
  }

  @Test
  void login_WithValidCredentials_ShouldReturnTokens() {
    AuthRequest request = new AuthRequest("testuser", "password", null, null, null);
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal())
        .thenReturn(new com.example.demo.security.CustomUserDetails(user));

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    // User logic is now derived from Authentication, unrelated stub removed if
    // found,
    // but happy path might use repo elsewhere?
    // Actually login implementation gets user from principal.
    // However, generateToken might take UserDetails from principal too.
    when(jwtService.generateToken(any())).thenReturn("accessToken");

    AuthResponse response = authService.login(request);

    assertEquals("accessToken", response.token());
    assertEquals("testuser", response.username());
  }

  @Test
  void login_WhenUserNotActive_ShouldThrowException() {
    AuthRequest request = new AuthRequest("testuser", "password", null, null, null);
    user.setIsActive(false);

    // Provide the expected exception from AuthenticationManager
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(
            new org.springframework.security.authentication.DisabledException("User is disabled"));

    assertThrows(RuntimeException.class, () -> authService.login(request));
  }

  @Test
  void refreshAccessToken_WithValidToken_ShouldReturnNewToken() {
    // This depends on RefreshTokenService implementation details.
    // Assuming standard flow: verify token -> get user -> generate new access token

    when(refreshTokenService.validateRefreshToken("validRefreshToken"))
        .thenReturn(Optional.of(user.getId()));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    // JWT validation mock
    when(jwtService.isTokenValid(eq("validRefreshToken"), any())).thenReturn(true);
    when(jwtService.generateToken(any())).thenReturn("newAccessToken");

    AuthResponse response = authService.refreshAccessToken("validRefreshToken");

    assertEquals("newAccessToken", response.token());
  }

  @Test
  void logout_ShouldBlacklistToken() {
    String token = "Bearer someToken";

    // Mock expiration extraction to avoid NPE
    when(jwtService.extractExpirationInstant(token))
        .thenReturn(java.time.Instant.now().plusSeconds(3600));

    authService.logout(token);

    verify(tokenBlacklistService).blacklistToken(eq(token), anyLong());
  }

  @Test
  void forgotPassword_ShouldGenerateTokenAndSendEmail() {
    // Act
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordResetService.createPasswordResetToken(eq(1L), anyInt())).thenReturn("resetToken");

    authService.forgotPassword("test@example.com");

    verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString(), anyString());
  }

  @Test
  void resetPassword_WithValidToken_ShouldUpdatePassword() {
    // Act
    when(passwordResetService.validateToken("validToken")).thenReturn(Optional.of(user.getId()));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("newPassword")).thenReturn("newEncoded");

    authService.resetPassword("validToken", "newPassword");

    verify(userRepository).save(user);
    verify(passwordResetService).markTokenAsUsed("validToken");
  }
}
