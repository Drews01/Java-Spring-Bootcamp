package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.AdminLoanApplicationDTO;
import com.example.demo.service.AdminLoanService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminLoanService adminLoanService;

  @GetMapping("/dashboard")
  @PreAuthorize("@accessControl.hasMenu('ADMIN_MODULE')")
  public ResponseEntity<ApiResponse<String>> getDashboard() {
    return ResponseUtil.ok(
        "Welcome to the System Administration Dashboard.", "Admin access verified");
  }

  @GetMapping("/system-logs")
  @PreAuthorize("@accessControl.hasMenu('ADMIN_MODULE')")
  public ResponseEntity<ApiResponse<String>> getSystemLogs() {
    return ResponseUtil.ok("System logs would be visible here.", "Admin access verified");
  }

  /**
   * Get all loan applications with full details for admin view. Includes profile ID, current
   * status, and which bucket (MARKETING, BRANCH_MANAGER, BACK_OFFICE, COMPLETED) each application
   * is in.
   *
   * @param page page number (0-indexed, default 0)
   * @param size page size (default 20)
   * @return paginated list of loan applications with admin-relevant information
   */
  @GetMapping("/loan-applications")
  @PreAuthorize("@accessControl.hasMenu('ADMIN_MODULE')")
  public ResponseEntity<ApiResponse<List<AdminLoanApplicationDTO>>> getAllLoanApplications(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    Page<AdminLoanApplicationDTO> loans =
        adminLoanService.getAllLoanApplications(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return ResponseUtil.ok(loans.getContent(), "Loan applications retrieved successfully");
  }
}
