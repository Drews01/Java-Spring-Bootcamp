package com.example.demo.service;

import com.example.demo.dto.BulkRoleMenuUpdateRequest;
import com.example.demo.dto.MenuGroupDTO;
import com.example.demo.dto.MenuItemDTO;
import com.example.demo.dto.RoleAccessDTO;
import com.example.demo.dto.RoleAccessSummaryDTO;
import com.example.demo.entity.Menu;
import com.example.demo.entity.Role;
import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.RoleMenuId;
import com.example.demo.enums.MenuCode;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.RoleRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RbacService {

  private final RoleRepository roleRepository;
  private final MenuRepository menuRepository;
  private final RoleMenuRepository roleMenuRepository;

  public Page<RoleAccessSummaryDTO> getAllRolesWithSummary(Pageable pageable) {
    Page<Role> rolesPage = roleRepository.findByDeletedFalse(pageable);
    int totalMenus = (int) menuRepository.count();

    return rolesPage.map(
        role -> {
          int assignedMenus = roleMenuRepository.findByRoleId(role.getId()).size();
          return new RoleAccessSummaryDTO(role.getId(), role.getName(), totalMenus, assignedMenus);
        });
  }

  public RoleAccessDTO getRoleAccess(Long roleId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    List<Menu> allMenus = menuRepository.findAll();
    Set<Long> assignedMenuIds =
        roleMenuRepository.findByRoleId(roleId).stream()
            .map(RoleMenu::getMenuId)
            .collect(Collectors.toSet());

    // Group menus by category
    Map<String, List<MenuItemDTO>> groupedMenus = new HashMap<>();

    for (Menu menu : allMenus) {
      String category = MenuCode.getCategory(menu.getCode());
      MenuItemDTO menuItem =
          new MenuItemDTO(
              menu.getMenuId(),
              menu.getCode(),
              menu.getName(),
              menu.getUrlPattern(),
              assignedMenuIds.contains(menu.getMenuId()));

      groupedMenus.computeIfAbsent(category, k -> new ArrayList<>()).add(menuItem);
    }

    // Convert to MenuGroupDTO list
    List<MenuGroupDTO> menuGroups =
        groupedMenus.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new MenuGroupDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

    return new RoleAccessDTO(role.getId(), role.getName(), menuGroups);
  }

  @Transactional
  public RoleAccessDTO updateRoleAccess(Long roleId, BulkRoleMenuUpdateRequest request) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    // Get current assigned menu IDs
    Set<Long> currentMenuIds =
        roleMenuRepository.findByRoleId(roleId).stream()
            .map(RoleMenu::getMenuId)
            .collect(Collectors.toSet());

    Set<Long> newMenuIds = request.menuIds() != null ? Set.copyOf(request.menuIds()) : Set.of();

    // Find menus to add
    Set<Long> toAdd =
        newMenuIds.stream().filter(id -> !currentMenuIds.contains(id)).collect(Collectors.toSet());

    // Find menus to remove
    Set<Long> toRemove =
        currentMenuIds.stream().filter(id -> !newMenuIds.contains(id)).collect(Collectors.toSet());

    // Add new mappings
    for (Long menuId : toAdd) {
      if (menuRepository.existsById(menuId)) {
        RoleMenu roleMenu =
            RoleMenu.builder().roleId(roleId).menuId(menuId).isActive(true).deleted(false).build();
        roleMenuRepository.save(roleMenu);
      }
    }

    // Remove old mappings (soft delete)
    for (Long menuId : toRemove) {
      RoleMenuId id = new RoleMenuId(roleId, menuId);
      roleMenuRepository
          .findById(id)
          .ifPresent(
              roleMenu -> {
                roleMenu.setDeleted(true);
                roleMenu.setIsActive(false);
                roleMenuRepository.save(roleMenu);
              });
    }

    return getRoleAccess(roleId);
  }

  public List<String> getCategories() {
    return java.util.Arrays.stream(MenuCode.values())
        .map(MenuCode::getCategory)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }
}
