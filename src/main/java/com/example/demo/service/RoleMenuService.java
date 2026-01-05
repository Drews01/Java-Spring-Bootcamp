package com.example.demo.service;

import com.example.demo.entity.Menu;
import com.example.demo.entity.Role;
import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.RoleMenuId;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleMenuService {

  private final RoleMenuRepository roleMenuRepository;
  private final RoleRepository roleRepository;
  private final MenuRepository menuRepository;

  @Transactional
  public RoleMenu assignMenuToRole(Long roleId, Long menuId) {
    Role role =
        roleRepository
            .findById(roleId)
            .filter(r -> r.getDeleted() == null || !r.getDeleted())
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

    Menu menu =
        menuRepository
            .findById(menuId)
            .orElseThrow(() -> new RuntimeException("Menu not found with id: " + menuId));

    RoleMenu roleMenu =
        roleMenuRepository
            .findById(new RoleMenuId(roleId, menuId))
            .map(
                rm -> {
                  rm.setDeleted(false);
                  rm.setIsActive(true);
                  return rm;
                })
            .orElseGet(
                () ->
                    RoleMenu.builder()
                        .roleId(roleId)
                        .menuId(menuId)
                        .role(role)
                        .menu(menu)
                        .deleted(false)
                        .isActive(true)
                        .build());

    return roleMenuRepository.save(roleMenu);
  }

  @Transactional(readOnly = true)
  public List<RoleMenu> getMenusByRoleId(Long roleId) {
    return roleMenuRepository.findByRoleId(roleId);
  }

  @Transactional(readOnly = true)
  public List<RoleMenu> getRolesByMenuId(Long menuId) {
    return roleMenuRepository.findByMenuId(menuId);
  }

  @Transactional
  public void removeMenuFromRole(Long roleId, Long menuId) {
    RoleMenuId id = new RoleMenuId(roleId, menuId);
    RoleMenu roleMenu =
        roleMenuRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Mapping not found"));
    roleMenu.setDeleted(true);
    roleMenu.setIsActive(false);
    roleMenuRepository.save(roleMenu);
  }
}
