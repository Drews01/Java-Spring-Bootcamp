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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RbacService {

  private final RoleRepository roleRepository;
  private final MenuRepository menuRepository;
  private final RoleMenuRepository roleMenuRepository;

  // Menu code to category mapping
  private static final Map<String, String> MENU_CATEGORY_MAP = new HashMap<>();

  static {
    // Admin Module
    MENU_CATEGORY_MAP.put("ADMIN_DASHBOARD", "Admin Module");
    MENU_CATEGORY_MAP.put("ADMIN_SYSTEM_LOGS", "Admin Module");

    // User Management
    MENU_CATEGORY_MAP.put("USER_LIST", "User Management");
    MENU_CATEGORY_MAP.put("USER_GET", "User Management");
    MENU_CATEGORY_MAP.put("USER_CREATE", "User Management");
    MENU_CATEGORY_MAP.put("USER_UPDATE", "User Management");
    MENU_CATEGORY_MAP.put("USER_DELETE", "User Management");
    MENU_CATEGORY_MAP.put("ADMIN_USER_LIST", "User Management");
    MENU_CATEGORY_MAP.put("ADMIN_USER_CREATE", "User Management");
    MENU_CATEGORY_MAP.put("ADMIN_USER_STATUS", "User Management");
    MENU_CATEGORY_MAP.put("ADMIN_USER_ROLES", "User Management");

    // Role Management
    MENU_CATEGORY_MAP.put("ROLE_LIST", "Role Management");
    MENU_CATEGORY_MAP.put("ROLE_CREATE", "Role Management");
    MENU_CATEGORY_MAP.put("ROLE_DELETE", "Role Management");

    // Menu Management
    MENU_CATEGORY_MAP.put("MENU_LIST", "Menu Management");
    MENU_CATEGORY_MAP.put("MENU_GET", "Menu Management");
    MENU_CATEGORY_MAP.put("MENU_CREATE", "Menu Management");
    MENU_CATEGORY_MAP.put("MENU_UPDATE", "Menu Management");
    MENU_CATEGORY_MAP.put("MENU_DELETE", "Menu Management");

    // Loan Workflow
    MENU_CATEGORY_MAP.put("LOAN_SUBMIT", "Loan Workflow");
    MENU_CATEGORY_MAP.put("LOAN_ACTION", "Loan Workflow");
    MENU_CATEGORY_MAP.put("LOAN_ALLOWED_ACTIONS", "Loan Workflow");
    MENU_CATEGORY_MAP.put("LOAN_QUEUE_MARKETING", "Loan Workflow");
    MENU_CATEGORY_MAP.put("LOAN_QUEUE_BRANCH_MANAGER", "Loan Workflow");
    MENU_CATEGORY_MAP.put("LOAN_QUEUE_BACK_OFFICE", "Loan Workflow");

    // Loan Application
    MENU_CATEGORY_MAP.put("LOAN_APP_CREATE", "Loan Application");
    MENU_CATEGORY_MAP.put("LOAN_APP_GET", "Loan Application");
    MENU_CATEGORY_MAP.put("LOAN_APP_BY_USER", "Loan Application");
    MENU_CATEGORY_MAP.put("LOAN_APP_BY_STATUS", "Loan Application");
    MENU_CATEGORY_MAP.put("LOAN_APP_LIST", "Loan Application");
    MENU_CATEGORY_MAP.put("LOAN_APP_UPDATE", "Loan Application");
    MENU_CATEGORY_MAP.put("LOAN_APP_DELETE", "Loan Application");

    // Loan History
    MENU_CATEGORY_MAP.put("LOAN_HISTORY_CREATE", "Loan History");
    MENU_CATEGORY_MAP.put("LOAN_HISTORY_GET", "Loan History");
    MENU_CATEGORY_MAP.put("LOAN_HISTORY_BY_LOAN", "Loan History");
    MENU_CATEGORY_MAP.put("LOAN_HISTORY_LIST", "Loan History");
    MENU_CATEGORY_MAP.put("LOAN_HISTORY_DELETE", "Loan History");

    // Product Management
    MENU_CATEGORY_MAP.put("PRODUCT_CREATE", "Product Management");
    MENU_CATEGORY_MAP.put("PRODUCT_LIST", "Product Management");
    MENU_CATEGORY_MAP.put("PRODUCT_ACTIVE", "Product Management");
    MENU_CATEGORY_MAP.put("PRODUCT_BY_CODE", "Product Management");
    MENU_CATEGORY_MAP.put("PRODUCT_UPDATE_STATUS", "Product Management");
    MENU_CATEGORY_MAP.put("PRODUCT_DELETE", "Product Management");

    // User Product
    MENU_CATEGORY_MAP.put("USER_PRODUCT_CREATE", "User Product");
    MENU_CATEGORY_MAP.put("USER_PRODUCT_GET", "User Product");
    MENU_CATEGORY_MAP.put("USER_PRODUCT_BY_USER", "User Product");
    MENU_CATEGORY_MAP.put("USER_PRODUCT_ACTIVE", "User Product");
    MENU_CATEGORY_MAP.put("USER_PRODUCT_LIST", "User Product");
    MENU_CATEGORY_MAP.put("USER_PRODUCT_UPDATE", "User Product");
    MENU_CATEGORY_MAP.put("USER_PRODUCT_DELETE", "User Product");

    // User Profile
    MENU_CATEGORY_MAP.put("PROFILE_CREATE", "User Profile");
    MENU_CATEGORY_MAP.put("PROFILE_ME", "User Profile");
    MENU_CATEGORY_MAP.put("PROFILE_LIST", "User Profile");
    MENU_CATEGORY_MAP.put("PROFILE_UPDATE", "User Profile");
    MENU_CATEGORY_MAP.put("PROFILE_DELETE", "User Profile");

    // Notification
    MENU_CATEGORY_MAP.put("NOTIFICATION_CREATE", "Notification");
    MENU_CATEGORY_MAP.put("NOTIFICATION_GET", "Notification");
    MENU_CATEGORY_MAP.put("NOTIFICATION_BY_USER", "Notification");
    MENU_CATEGORY_MAP.put("NOTIFICATION_UNREAD", "Notification");
    MENU_CATEGORY_MAP.put("NOTIFICATION_UNREAD_COUNT", "Notification");
    MENU_CATEGORY_MAP.put("NOTIFICATION_LIST", "Notification");
    MENU_CATEGORY_MAP.put("NOTIFICATION_MARK_READ", "Notification");
    MENU_CATEGORY_MAP.put("NOTIFICATION_DELETE", "Notification");

    // Dashboards (Unified Staff Dashboard)
    MENU_CATEGORY_MAP.put("STAFF_DASHBOARD", "Dashboards");
    MENU_CATEGORY_MAP.put("STAFF_QUEUE", "Dashboards");

    // RBAC Management
    MENU_CATEGORY_MAP.put("RBAC_ROLES_LIST", "RBAC Management");
    MENU_CATEGORY_MAP.put("RBAC_ROLE_ACCESS", "RBAC Management");
    MENU_CATEGORY_MAP.put("RBAC_CATEGORIES", "RBAC Management");
  }

  public List<RoleAccessSummaryDTO> getAllRolesWithSummary() {
    List<Role> roles = roleRepository.findAll();
    int totalMenus = (int) menuRepository.count();

    return roles.stream()
        .map(
            role -> {
              int assignedMenus = roleMenuRepository.findByRoleId(role.getId()).size();
              return new RoleAccessSummaryDTO(
                  role.getId(), role.getName(), totalMenus, assignedMenus);
            })
        .collect(Collectors.toList());
  }

  public RoleAccessDTO getRoleAccess(Long roleId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

    List<Menu> allMenus = menuRepository.findAll();
    Set<Long> assignedMenuIds =
        roleMenuRepository.findByRoleId(roleId).stream()
            .map(RoleMenu::getMenuId)
            .collect(Collectors.toSet());

    // Group menus by category
    Map<String, List<MenuItemDTO>> groupedMenus = new HashMap<>();

    for (Menu menu : allMenus) {
      String category = MENU_CATEGORY_MAP.getOrDefault(menu.getCode(), "Other");
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
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

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
    return MENU_CATEGORY_MAP.values().stream().distinct().sorted().collect(Collectors.toList());
  }
}
