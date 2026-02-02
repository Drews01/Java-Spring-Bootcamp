package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.AdminCreateUserRequest;
import com.example.demo.dto.UserListDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private EmailService emailService;
  @Mock private PasswordResetService passwordResetService;

  @InjectMocks private UserService userService;

  private User adminUser;
  private User regularUser;
  private Role adminRole;
  private Role userRole;

  @BeforeEach
  void setUp() {
    adminRole = Role.builder().id(1L).name("ADMIN").build();
    userRole = Role.builder().id(2L).name("USER").build();

    adminUser =
        User.builder()
            .id(1L)
            .username("admin")
            .email("admin@example.com")
            .roles(new HashSet<>(Collections.singletonList(adminRole)))
            .isActive(true)
            .build();

    regularUser =
        User.builder()
            .id(2L)
            .username("user")
            .email("user@example.com")
            .roles(new HashSet<>(Collections.singletonList(userRole)))
            .isActive(true)
            .build();
  }

  @Test
  void getUserById_WhenExists_ShouldReturnUser() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
    User result = userService.getUserById(2L);
    assertNotNull(result);
    assertEquals("user", result.getUsername());
  }

  @Test
  void getUserById_WhenDeleted_ShouldThrowException() {
    regularUser.setDeleted(true); // Simulate mapped deletion flag if implementation filters it
    // Logic in service: filter(u -> u.getDeleted() == null || !u.getDeleted())
    when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
    assertThrows(RuntimeException.class, () -> userService.getUserById(2L));
  }

  @Test
  void setUserActiveStatus_ShouldUpdateStatus() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    UserListDTO result = userService.setUserActiveStatus(2L, false, adminUser.getId());
    assertFalse(result.getIsActive());
  }

  @Test
  void setUserActiveStatus_SelfDeactivation_ShouldThrowException() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    assertThrows(
        IllegalArgumentException.class, () -> userService.setUserActiveStatus(1L, false, 1L));
  }

  @Test
  void setUserActiveStatus_LastAdmin_ShouldThrowException() {
    // Other admin user
    User otherAdmin =
        User.builder().id(3L).roles(Collections.singleton(adminRole)).isActive(true).build();

    when(userRepository.findById(3L)).thenReturn(Optional.of(otherAdmin));
    // Mock count active admins to return 1 (myself)
    when(userRepository.findByDeletedFalse()).thenReturn(Collections.singletonList(otherAdmin));

    assertThrows(
        IllegalArgumentException.class, () -> userService.setUserActiveStatus(3L, false, 1L));
  }

  @Test
  void updateUserRoles_ShouldUpdateRoles() {
    Set<String> newRoles = new HashSet<>();
    newRoles.add("ADMIN");

    when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    UserListDTO result = userService.updateUserRoles(2L, newRoles, 1L);
    assertTrue(result.getRoles().contains("ADMIN"));
  }

  @Test
  void updateUserRoles_RemoveSelfAdminRole_ShouldThrowException() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    // Request to change roles to USER (removing ADMIN)
    Set<String> newRoles = new HashSet<>();
    newRoles.add("USER");

    assertThrows(
        IllegalArgumentException.class, () -> userService.updateUserRoles(1L, newRoles, 1L));
  }

  @Test
  void createUserByAdmin_WithDuplicateEmail_ShouldThrowException() {
    AdminCreateUserRequest request =
        new AdminCreateUserRequest(
            "newuser", "existing@example.com", Collections.singleton("USER"));

    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> userService.createUserByAdmin(request));
  }
}
