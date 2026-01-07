package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.AdminCreateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UpdateUserStatusRequest;
import com.example.demo.dto.UserListDTO;
import com.example.demo.entity.User;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
    List<User> users = userService.getAllUsers();
    return ResponseUtil.ok(users, "Users fetched successfully");
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
    User user = userService.getUserById(id);
    return ResponseUtil.ok(user, "User fetched successfully");
  }

  @PostMapping
  public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
    User createdUser = userService.createUser(user);
    return ResponseUtil.created(createdUser, "User created successfully");
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<User>> updateUser(
      @PathVariable Long id, @RequestBody User user) {
    User updatedUser = userService.updateUser(id, user);
    return ResponseUtil.ok(updatedUser, "User updated successfully");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseUtil.okMessage("User deleted successfully");
  }

  // ============= ADMIN-ONLY ENDPOINTS =============

  @GetMapping("/admin/list")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<UserListDTO>>> getAllUsersForAdmin() {
    List<UserListDTO> users = userService.getAllUsersForAdmin();
    return ResponseUtil.ok(users, "Users list fetched successfully for admin");
  }

  @PostMapping("/admin/create")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserListDTO>> createUserByAdmin(
      @Valid @RequestBody AdminCreateUserRequest request) {
    UserListDTO createdUser = userService.createUserByAdmin(request);
    return ResponseUtil.created(
        createdUser, "User created successfully. Password reset email sent.");
  }

  @PatchMapping("/admin/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserListDTO>> updateUserStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
    Long currentAdminId = getCurrentUserId();
    UserListDTO updatedUser =
        userService.setUserActiveStatus(id, request.isActive(), currentAdminId);
    return ResponseUtil.ok(updatedUser, "User status updated successfully");
  }

  @PutMapping("/admin/{id}/roles")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserListDTO>> updateUserRoles(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
    Long currentAdminId = getCurrentUserId();
    UserListDTO updatedUser = userService.updateUserRoles(id, request.roleNames(), currentAdminId);
    return ResponseUtil.ok(updatedUser, "User roles updated successfully");
  }

  /** Get current authenticated user's ID from security context */
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      return userDetails.getId();
    }
    throw new RuntimeException("Unable to get current user ID from security context");
  }
}
