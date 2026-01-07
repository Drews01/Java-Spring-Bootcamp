package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.StaffDashboardDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.repository.LoanApplicationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unified Staff Dashboard Controller for Marketing, Branch Manager, and Back Office roles. Provides
 * a single dashboard endpoint that returns role-specific data.
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffDashboardController {

  private final LoanApplicationRepository loanApplicationRepository;
  private final com.example.demo.service.DashboardService dashboardService;

  /** Executive/Analytics Dashboard. Returns high-level metrics and trends. */
  @GetMapping("/dashboard/analytics")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE') or @accessControl.hasMenu('STAFF_DASHBOARD')")
  public ResponseEntity<ApiResponse<com.example.demo.dto.dashboard.ExecutiveDashboardDTO>>
      getAnalytics(
          @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year) {
    return ResponseUtil.ok(
        dashboardService.getExecutiveDashboard(year), "Analytics data loaded successfully");
  }

  /**
   * Unified dashboard for all staff roles (Marketing, Branch Manager, Back Office). Returns role
   * information and allowed actions based on the authenticated user's role.
   */
  @GetMapping("/dashboard")
  @PreAuthorize(
      "hasAnyRole('MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE') or @accessControl.hasMenu('STAFF_DASHBOARD')")
  public ResponseEntity<ApiResponse<StaffDashboardDTO>> getDashboard(Authentication auth) {
    String username = auth.getName();
    List<String> roles =
        auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    // Determine primary role for the user
    String primaryRole = determinePrimaryRole(roles);
    String queueName = getQueueNameForRole(primaryRole);

    StaffDashboardDTO dashboard =
        new StaffDashboardDTO(
            username,
            primaryRole,
            roles,
            queueName,
            getAllowedActionsForRole(primaryRole),
            "Welcome to the Staff Dashboard");

    return ResponseUtil.ok(dashboard, "Staff dashboard loaded successfully");
  }

  /**
   * Get the workflow queue for the current user's role. Returns loans in the queue that the user
   * can act on.
   */
  @GetMapping("/queue")
  @PreAuthorize(
      "hasAnyRole('MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE') or @accessControl.hasMenu('STAFF_QUEUE')")
  public ResponseEntity<ApiResponse<List<LoanApplicationDTO>>> getQueue(Authentication auth) {
    List<String> roles =
        auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    String primaryRole = determinePrimaryRole(roles);
    String queueName = getQueueNameForRole(primaryRole);

    // Get queue based on role - fetch loans by current status
    List<String> statusesForRole = getStatusesForRole(primaryRole);
    List<LoanApplication> loans =
        loanApplicationRepository.findByCurrentStatusInOrderByCreatedAtDesc(statusesForRole);

    List<LoanApplicationDTO> queueData =
        loans.stream()
            .map(
                loan ->
                    LoanApplicationDTO.builder()
                        .loanApplicationId(loan.getLoanApplicationId())
                        .userId(loan.getUser().getId())
                        .productId(loan.getProduct().getId())
                        .amount(loan.getAmount())
                        .tenureMonths(loan.getTenureMonths())
                        .interestRateApplied(loan.getInterestRateApplied())
                        .totalAmountToPay(loan.getTotalAmountToPay())
                        .currentStatus(loan.getCurrentStatus())
                        .createdAt(loan.getCreatedAt())
                        .updatedAt(loan.getUpdatedAt())
                        .build())
            .toList();

    return ResponseUtil.ok(queueData, "Queue for " + queueName + " retrieved successfully");
  }

  private String determinePrimaryRole(List<String> roles) {
    // Priority: BACK_OFFICE > BRANCH_MANAGER > MARKETING
    if (roles.contains("ROLE_BACK_OFFICE") || roles.contains("BACK_OFFICE")) {
      return "BACK_OFFICE";
    } else if (roles.contains("ROLE_BRANCH_MANAGER") || roles.contains("BRANCH_MANAGER")) {
      return "BRANCH_MANAGER";
    } else if (roles.contains("ROLE_MARKETING") || roles.contains("MARKETING")) {
      return "MARKETING";
    }
    return "UNKNOWN";
  }

  private String getQueueNameForRole(String role) {
    return switch (role) {
      case "MARKETING" -> "Marketing Queue";
      case "BRANCH_MANAGER" -> "Branch Manager Queue";
      case "BACK_OFFICE" -> "Back Office Queue";
      default -> "Unknown Queue";
    };
  }

  private List<String> getStatusesForRole(String role) {
    return switch (role) {
      case "MARKETING" -> List.of("SUBMITTED", "IN_REVIEW");
      case "BRANCH_MANAGER" -> List.of("RECOMMENDED", "PENDING_APPROVAL");
      case "BACK_OFFICE" -> List.of("APPROVED", "PENDING_DISBURSEMENT");
      default -> List.of();
    };
  }

  private List<String> getAllowedActionsForRole(String role) {
    return switch (role) {
      case "MARKETING" -> List.of("REVIEW", "RECOMMEND", "RETURN");
      case "BRANCH_MANAGER" -> List.of("APPROVE", "REJECT", "RETURN");
      case "BACK_OFFICE" -> List.of("DISBURSE", "RETURN");
      default -> List.of();
    };
  }
}
