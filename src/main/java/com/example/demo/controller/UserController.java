package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.BaseController;
import com.example.demo.base.ResponseUtil;
import com.example.demo.constants.ApiMessage;
import com.example.demo.dto.AdminCreateUserRequest;
import com.example.demo.dto.UpdateUserRolesRequest;
import com.example.demo.dto.UpdateUserStatusRequest;
import com.example.demo.dto.UserListDTO;
import com.example.demo.entity.User;
import com.example.demo.service.IUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

  private final IUserService userService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<UserListDTO>>> getAllUsers() {
    List<UserListDTO> users = userService.getAllUsers();
    return ResponseUtil.ok(users, ApiMessage.USERS_FETCHED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserListDTO>> getUserById(@PathVariable Long id) {
    UserListDTO user = userService.getUserById(id);
    return ResponseUtil.ok(user, ApiMessage.USER_FETCHED);
  }

  @PostMapping
  public ResponseEntity<ApiResponse<UserListDTO>> createUser(@RequestBody User user) {
    UserListDTO createdUser = userService.createUser(user);
    return ResponseUtil.created(createdUser, ApiMessage.USER_CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserListDTO>> updateUser(
      @PathVariable Long id, @RequestBody User user) {
    UserListDTO updatedUser = userService.updateUser(id, user);
    return ResponseUtil.ok(updatedUser, ApiMessage.USER_UPDATED);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseUtil.okMessage(ApiMessage.USER_DELETED);
  }

  // ============= ADMIN-ONLY ENDPOINTS =============

  @GetMapping("/admin/list")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<UserListDTO>>> getAllUsersForAdmin(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<UserListDTO> users = userService.getAllUsersForAdmin(pageable);
    return ResponseUtil.ok(users, ApiMessage.USERS_FETCHED);
  }

  @PostMapping("/admin/create")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserListDTO>> createUserByAdmin(
      @Valid @RequestBody AdminCreateUserRequest request) {
    UserListDTO createdUser = userService.createUserByAdmin(request);
    return ResponseUtil.created(createdUser, ApiMessage.USER_CREATED);
  }

  @PatchMapping("/admin/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserListDTO>> updateUserStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
    Long currentAdminId = getCurrentUserId();
    UserListDTO updatedUser =
        userService.setUserActiveStatus(id, request.isActive(), currentAdminId);
    return ResponseUtil.ok(updatedUser, ApiMessage.USER_STATUS_UPDATED);
  }

  @PutMapping("/admin/{id}/roles")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserListDTO>> updateUserRoles(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
    Long currentAdminId = getCurrentUserId();
    UserListDTO updatedUser = userService.updateUserRoles(id, request.roleNames(), currentAdminId);
    return ResponseUtil.ok(updatedUser, ApiMessage.USER_ROLES_UPDATED);
  }
}
