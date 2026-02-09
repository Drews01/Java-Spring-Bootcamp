package com.example.demo.service;

import com.example.demo.dto.RoleMenuDTO;
import com.example.demo.entity.Menu;
import com.example.demo.entity.Role;
import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.RoleMenuId;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.RoleRepository;
import java.util.List;
import java.util.stream.Collectors;
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
  public RoleMenuDTO assignMenuToRole(Long roleId, Long menuId) {
    Role role =
        roleRepository
            .findById(roleId)
            .filter(r -> r.getDeleted() == null || !r.getDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    Menu menu =
        menuRepository
            .findById(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuId));

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

    RoleMenu saved = roleMenuRepository.save(roleMenu);
    return RoleMenuDTO.fromEntity(saved);
  }

  @Transactional(readOnly = true)
  public List<RoleMenuDTO> getMenusByRoleId(Long roleId) {
    return roleMenuRepository.findByRoleId(roleId).stream()
        .map(RoleMenuDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<RoleMenuDTO> getRolesByMenuId(Long menuId) {
    return roleMenuRepository.findByMenuId(menuId).stream()
        .map(RoleMenuDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public void removeMenuFromRole(Long roleId, Long menuId) {
    RoleMenuId id = new RoleMenuId(roleId, menuId);
    RoleMenu roleMenu =
        roleMenuRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RoleMenu mapping not found"));
    roleMenu.setDeleted(true);
    roleMenu.setIsActive(false);
    roleMenuRepository.save(roleMenu);
  }
}
