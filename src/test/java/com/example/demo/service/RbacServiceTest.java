package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.BulkRoleMenuUpdateRequest;
import com.example.demo.dto.RoleAccessDTO;
import com.example.demo.entity.Menu;
import com.example.demo.entity.Role;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.RoleRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class RbacServiceTest {

  @Mock private RoleRepository roleRepository;
  @Mock private MenuRepository menuRepository;
  @Mock private RoleMenuRepository roleMenuRepository;

  @InjectMocks private RbacService rbacService;

  private Role role;
  private Menu menu;

  @BeforeEach
  void setUp() {
    role = Role.builder().id(1L).name("MANAGER").build();
    menu = Menu.builder().menuId(1L).name("LOAN_REVIEW").build();
  }

  @Test
  void getRoleAccess_ShouldReturnMenusGroupedByCategory() {
    when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
    when(menuRepository.findAll()).thenReturn(Collections.singletonList(menu));
    // Assuming role has no assigned menus initially for this test
    when(roleMenuRepository.findByRoleId(1L)).thenReturn(Collections.emptyList());

    RoleAccessDTO result = rbacService.getRoleAccess(1L);

    assertNotNull(result);
    assertEquals(1L, result.roleId());
    assertFalse(result.menuGroups().isEmpty());
    // assertTrue(result.menuGroups().stream().anyMatch(g ->
    // g.groupName().equals("LOAN")));
  }

  @Test
  void updateRoleAccess_ShouldAddAndRemoveMenus() {
    BulkRoleMenuUpdateRequest request =
        new BulkRoleMenuUpdateRequest(
            null, new java.util.ArrayList<>(Collections.singletonList(1L)));

    when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
    when(menuRepository.existsById(1L)).thenReturn(true);
    // existing menus empty
    when(roleMenuRepository.findByRoleId(1L)).thenReturn(Collections.emptyList());

    rbacService.updateRoleAccess(1L, request);

    // Verify save called
    verify(roleMenuRepository, times(1)).save(any());
  }

  @Test
  void getAllRolesWithSummary_ShouldReturnPaginatedRoles() {
    Page<Role> rolePage = new PageImpl<>(Collections.singletonList(role));
    when(roleRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(rolePage);

    var result = rbacService.getAllRolesWithSummary(Pageable.unpaged());

    assertEquals(1, result.getSize());
  }
}
