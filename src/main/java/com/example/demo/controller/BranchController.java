package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.BranchDTO;
import com.example.demo.dto.CreateBranchRequest;
import com.example.demo.dto.UpdateBranchRequest;
import com.example.demo.dto.UserBranchDTO;
import com.example.demo.service.BranchService;
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
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BranchController {

  private final BranchService branchService;

  // ============= BRANCH CRUD =============

  /** Get all branches (including inactive) */
  @GetMapping
  public ResponseEntity<ApiResponse<Page<BranchDTO>>> getAllBranches(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<BranchDTO> branches = branchService.getAllBranches(pageable);
    return ResponseUtil.ok(branches, "Branches fetched successfully");
  }

  /** Get only active branches */
  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<BranchDTO>>> getActiveBranches() {
    List<BranchDTO> branches = branchService.getActiveBranches();
    return ResponseUtil.ok(branches, "Active branches fetched successfully");
  }

  /** Get branch by ID */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<BranchDTO>> getBranchById(@PathVariable Long id) {
    BranchDTO branch = branchService.getBranchById(id);
    return ResponseUtil.ok(branch, "Branch fetched successfully");
  }

  /** Create a new branch */
  @PostMapping
  public ResponseEntity<ApiResponse<BranchDTO>> createBranch(
      @Valid @RequestBody CreateBranchRequest request) {
    BranchDTO branch = branchService.createBranch(request);
    return ResponseUtil.created(branch, "Branch created successfully");
  }

  /** Update an existing branch */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(
      @PathVariable Long id, @Valid @RequestBody UpdateBranchRequest request) {
    BranchDTO branch = branchService.updateBranch(id, request);
    return ResponseUtil.ok(branch, "Branch updated successfully");
  }

  /** Deactivate a branch (soft delete) */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deactivateBranch(@PathVariable Long id) {
    branchService.deactivateBranch(id);
    return ResponseUtil.okMessage("Branch deactivated successfully");
  }

  // ============= USER ASSIGNMENT =============

  /** Get users assigned to a specific branch */
  @GetMapping("/{branchId}/users")
  public ResponseEntity<ApiResponse<List<UserBranchDTO>>> getUsersByBranch(
      @PathVariable Long branchId) {
    List<UserBranchDTO> users = branchService.getUsersByBranch(branchId);
    return ResponseUtil.ok(users, "Users fetched successfully for branch");
  }

  /** Assign a user to a branch */
  @PostMapping("/{branchId}/users/{userId}")
  public ResponseEntity<ApiResponse<UserBranchDTO>> assignUserToBranch(
      @PathVariable Long branchId, @PathVariable Long userId) {
    UserBranchDTO userBranch = branchService.assignUserToBranch(userId, branchId);
    return ResponseUtil.ok(userBranch, "User assigned to branch successfully");
  }

  /** Unassign a user from a branch */
  @DeleteMapping("/{branchId}/users/{userId}")
  public ResponseEntity<ApiResponse<UserBranchDTO>> unassignUserFromBranch(
      @PathVariable Long branchId, @PathVariable Long userId) {
    UserBranchDTO userBranch = branchService.unassignUserFromBranch(userId);
    return ResponseUtil.ok(userBranch, "User unassigned from branch successfully");
  }

  /** Get all users that can be assigned to branches (MARKETING/BRANCH_MANAGER roles) */
  @GetMapping("/users/assignable")
  public ResponseEntity<ApiResponse<List<UserBranchDTO>>> getAssignableUsers() {
    List<UserBranchDTO> users = branchService.getAssignableUsers();
    return ResponseUtil.ok(users, "Assignable users fetched successfully");
  }
}
