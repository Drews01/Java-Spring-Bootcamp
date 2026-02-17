package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.enums.RoleName;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class OAuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private UserProfileRepository userProfileRepository;
  @Mock private JwtService jwtService;
  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private OAuthService oAuthService;

  private User existingUser;
  private User newUser;
  private Role userRole;

  @BeforeEach
  void setUp() {
    userRole = Role.builder().id(1L).name(RoleName.USER.getRoleName()).build();

    existingUser =
        User.builder()
            .id(1L)
            .username("existing@example.com")
            .email("existing@example.com")
            .password(null)
            .authProvider(AuthProvider.GOOGLE)
            .roles(new HashSet<>(Collections.singletonList(userRole)))
            .isActive(true)
            .build();

    newUser =
        User.builder()
            .id(2L)
            .username("new@example.com")
            .email("new@example.com")
            .password(null)
            .authProvider(AuthProvider.GOOGLE)
            .roles(new HashSet<>(Collections.singletonList(userRole)))
            .isActive(true)
            .build();

    // Set the Google client ID for testing
    ReflectionTestUtils.setField(oAuthService, "googleClientId", "test-client-id");
  }

  @Test
  void loginWithGoogle_WithInvalidToken_ShouldThrowException() {
    String invalidToken = "invalid-token";

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> oAuthService.loginWithGoogle(invalidToken));

    assertTrue(exception.getMessage().contains("Google Login Failed"));
  }

  @Test
  void createGoogleUser_ShouldUseRoleNameEnum() {
    // This test validates that RoleName enum is used instead of string literal "USER"
    // by verifying the role repository is called with RoleName.USER.getRoleName()

    String email = "test@example.com";
    String name = "Test User";

    when(roleRepository.findByName(RoleName.USER.getRoleName())).thenReturn(Optional.of(userRole));
    when(userRepository.save(any(User.class))).thenReturn(newUser);
    when(userProfileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());

    // Use reflection to call the private createGoogleUser method
    try {
      java.lang.reflect.Method method =
          OAuthService.class.getDeclaredMethod("createGoogleUser", String.class, String.class);
      method.setAccessible(true);
      User result = (User) method.invoke(oAuthService, email, name);

      assertNotNull(result);
      verify(roleRepository).findByName(RoleName.USER.getRoleName());
      verify(userRepository).save(any(User.class));
      verify(userProfileRepository).save(any(UserProfile.class));
    } catch (Exception e) {
      fail("Failed to invoke createGoogleUser method: " + e.getMessage());
    }
  }

  @Test
  void createGoogleUser_WithNewRole_ShouldCreateRole() {
    // Test that a new role is created if it doesn't exist
    String email = "test@example.com";
    String name = "Test User";

    when(roleRepository.findByName(RoleName.USER.getRoleName())).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class))).thenReturn(userRole);
    when(userRepository.save(any(User.class))).thenReturn(newUser);
    when(userProfileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());

    // Use reflection to call the private createGoogleUser method
    try {
      java.lang.reflect.Method method =
          OAuthService.class.getDeclaredMethod("createGoogleUser", String.class, String.class);
      method.setAccessible(true);
      User result = (User) method.invoke(oAuthService, email, name);

      assertNotNull(result);
      verify(roleRepository).findByName(RoleName.USER.getRoleName());
      verify(roleRepository).save(any(Role.class));
      verify(userRepository).save(any(User.class));
      verify(userProfileRepository).save(any(UserProfile.class));
    } catch (Exception e) {
      fail("Failed to invoke createGoogleUser method: " + e.getMessage());
    }
  }

  @Test
  void createGoogleUser_ShouldSetAuthProviderToGoogle() {
    // Test that created user has GOOGLE auth provider
    String email = "test@example.com";
    String name = "Test User";

    when(roleRepository.findByName(RoleName.USER.getRoleName())).thenReturn(Optional.of(userRole));
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User savedUser = invocation.getArgument(0);
              assertEquals(AuthProvider.GOOGLE, savedUser.getAuthProvider());
              assertEquals(email, savedUser.getEmail());
              assertEquals(email, savedUser.getUsername()); // Email used as username
              assertNull(savedUser.getPassword()); // No password for OAuth users
              return newUser;
            });
    when(userProfileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());

    // Use reflection to call the private createGoogleUser method
    try {
      java.lang.reflect.Method method =
          OAuthService.class.getDeclaredMethod("createGoogleUser", String.class, String.class);
      method.setAccessible(true);
      method.invoke(oAuthService, email, name);

      verify(userRepository).save(any(User.class));
    } catch (Exception e) {
      fail("Failed to invoke createGoogleUser method: " + e.getMessage());
    }
  }
}
