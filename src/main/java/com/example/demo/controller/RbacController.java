package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.BulkRoleMenuUpdateRequest;
import com.example.demo.dto.RoleAccessDTO;
import com.example.demo.dto.RoleAccessSummaryDTO;
import com.example.demo.service.RbacService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RbacController {

  private final RbacService rbacService;

  @GetMapping("/roles")
  public ResponseEntity<ApiResponse<Page<RoleAccessSummaryDTO>>> getAllRolesWithSummary(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<RoleAccessSummaryDTO> roles = rbacService.getAllRolesWithSummary(pageable);
    return ResponseUtil.ok(roles, "Roles retrieved successfully");
  }

  @GetMapping("/roles/{roleId}/access")
  public ResponseEntity<ApiResponse<RoleAccessDTO>> getRoleAccess(@PathVariable Long roleId) {
    RoleAccessDTO roleAccess = rbacService.getRoleAccess(roleId);
    return ResponseUtil.ok(roleAccess, "Role access retrieved successfully");
  }

  @PutMapping("/roles/{roleId}/access")
  public ResponseEntity<ApiResponse<RoleAccessDTO>> updateRoleAccess(
      @PathVariable Long roleId, @Valid @RequestBody BulkRoleMenuUpdateRequest request) {
    RoleAccessDTO roleAccess = rbacService.updateRoleAccess(roleId, request);
    return ResponseUtil.ok(roleAccess, "Role access updated successfully");
  }

  @GetMapping("/categories")
  public ResponseEntity<ApiResponse<List<String>>> getCategories() {
    List<String> categories = rbacService.getCategories();
    return ResponseUtil.ok(categories, "Categories retrieved successfully");
  }
}
