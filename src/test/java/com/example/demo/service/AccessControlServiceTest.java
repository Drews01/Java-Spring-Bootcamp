package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.UserRepository;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class AccessControlServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleMenuRepository roleMenuRepository;
  @Mock private MenuRepository menuRepository;

  @InjectMocks private AccessControlService accessControlService;

  @Test
  void hasMenu_WhenUserHasAccess_ShouldReturnTrue() {
    // Setup Security Context
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("testuser");
    when(authentication.isAuthenticated()).thenReturn(true);
    SecurityContextHolder.setContext(securityContext);

    // Setup mocks
    User user = User.builder().id(1L).roles(new HashSet<>()).build();
    Role role = Role.builder().id(10L).name("USER").build();
    user.getRoles().add(role);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    // Assume role names match
    when(roleMenuRepository.existsByRole_NameInAndMenu_Code(any(), eq("TEST_MENU")))
        .thenReturn(true);

    boolean result = accessControlService.hasMenu("TEST_MENU");

    assertTrue(result);
  }

  @Test
  void hasMenu_WhenUserLacksAccess_ShouldReturnFalse() {
    // Setup Security Context
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("testuser");
    when(authentication.isAuthenticated()).thenReturn(true);
    SecurityContextHolder.setContext(securityContext);

    // Setup mocks
    User user = User.builder().id(1L).roles(new HashSet<>()).build();
    Role role = Role.builder().id(10L).name("USER").build();
    user.getRoles().add(role);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(roleMenuRepository.existsByRole_NameInAndMenu_Code(any(), eq("TEST_MENU")))
        .thenReturn(false);

    boolean result = accessControlService.hasMenu("TEST_MENU");

    assertFalse(result);
  }
}
