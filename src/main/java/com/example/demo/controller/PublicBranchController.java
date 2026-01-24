package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.BranchDropdownDTO;
import com.example.demo.service.BranchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public Branch Controller for authenticated users. Provides branch data for dropdown selection in
 * mobile/web apps. No specific role required - accessible to all authenticated users.
 */
@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class PublicBranchController {

  private final BranchService branchService;

  /**
   * Get active branches for dropdown selection. Returns lightweight DTOs (id + name only) for
   * mobile optimization.
   *
   * @return List of active branches
   */
  @GetMapping("/dropdown")
  public ResponseEntity<ApiResponse<List<BranchDropdownDTO>>> getBranchesForDropdown() {
    List<BranchDropdownDTO> branches =
        branchService.getActiveBranches().stream()
            .map(b -> new BranchDropdownDTO(b.getId(), b.getName()))
            .toList();
    return ResponseUtil.ok(branches, "Branches fetched successfully");
  }
}
