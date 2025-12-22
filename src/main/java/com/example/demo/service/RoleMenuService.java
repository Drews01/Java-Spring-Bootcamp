package com.example.demo.service;

import com.example.demo.entity.Menu;
import com.example.demo.entity.Role;
import com.example.demo.entity.RoleMenu;
import com.example.demo.entity.RoleMenuId;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.RoleMenuRepository;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public RoleMenu assignMenuToRole(Long roleId, Long menuId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + menuId));

        RoleMenu roleMenu = RoleMenu.builder()
                .roleId(roleId)
                .menuId(menuId)
                .role(role)
                .menu(menu)
                .build();

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
        roleMenuRepository.deleteById(id);
    }
}
