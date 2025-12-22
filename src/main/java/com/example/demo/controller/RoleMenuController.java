package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.entity.RoleMenu;
import com.example.demo.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role-menus")
@RequiredArgsConstructor
public class RoleMenuController {

    private final RoleMenuService roleMenuService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleMenu>> assignMenuToRole(
            @RequestParam Long roleId,
            @RequestParam Long menuId) {
        RoleMenu created = roleMenuService.assignMenuToRole(roleId, menuId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Menu assigned to role successfully"));
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getMenusByRoleId(@PathVariable Long roleId) {
        List<RoleMenu> roleMenus = roleMenuService.getMenusByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(roleMenus, "Menus retrieved successfully"));
    }

    @GetMapping("/menu/{menuId}")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getRolesByMenuId(@PathVariable Long menuId) {
        List<RoleMenu> roleMenus = roleMenuService.getRolesByMenuId(menuId);
        return ResponseEntity.ok(ApiResponse.success(roleMenus, "Roles retrieved successfully"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeMenuFromRole(
            @RequestParam Long roleId,
            @RequestParam Long menuId) {
        roleMenuService.removeMenuFromRole(roleId, menuId);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu removed from role successfully"));
    }
}
