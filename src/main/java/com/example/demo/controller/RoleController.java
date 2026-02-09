package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.RoleDTO;
import com.example.demo.entity.Role;
import com.example.demo.service.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

  private final RoleService roleService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
    List<RoleDTO> roles = roleService.getAllRoles();
    return ResponseUtil.ok(roles, "Roles retrieved successfully");
  }

  @PostMapping
  public ResponseEntity<ApiResponse<RoleDTO>> createRole(@RequestBody Role role) {
    RoleDTO createdRole = roleService.createRole(role);
    return ResponseUtil.created(createdRole, "Role created successfully");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
    roleService.deleteRole(id);
    return ResponseUtil.okMessage("Role deleted successfully");
  }
}
