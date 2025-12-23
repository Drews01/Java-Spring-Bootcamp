package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.MenuDTO;
import com.example.demo.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuDTO>> createMenu(@RequestBody MenuDTO dto) {
        MenuDTO created = menuService.createMenu(dto);
        return ResponseUtil.created(created, "Menu created successfully");
    }

    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuDTO>> getMenu(@PathVariable Long menuId) {
        MenuDTO menu = menuService.getMenu(menuId);
        return ResponseUtil.ok(menu, "Menu retrieved successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuDTO>>> getAllMenus() {
        List<MenuDTO> menus = menuService.getAllMenus();
        return ResponseUtil.ok(menus, "Menus retrieved successfully");
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuDTO>> updateMenu(
            @PathVariable Long menuId,
            @RequestBody MenuDTO dto) {
        MenuDTO updated = menuService.updateMenu(menuId, dto);
        return ResponseUtil.ok(updated, "Menu updated successfully");
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseUtil.okMessage("Menu deleted successfully");
    }
}
