package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.LoanActionRequest;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.LoanQueueItemDTO;
import com.example.demo.dto.LoanSubmitRequest;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.LoanWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loan-workflow")
@RequiredArgsConstructor
public class LoanWorkflowController {

    private final LoanWorkflowService loanWorkflowService;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> submitLoan(@RequestBody LoanSubmitRequest request) {
        LoanApplicationDTO created = loanWorkflowService.submitLoan(request);
        return ResponseUtil.created(created, "Loan application submitted successfully");
    }

    @PostMapping("/action")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> performAction(@RequestBody LoanActionRequest request) {
        Long actorUserId = getCurrentUserId();
        LoanApplicationDTO updated = loanWorkflowService.performAction(request, actorUserId);
        return ResponseUtil.ok(updated, "Action performed successfully");
    }

    @GetMapping("/queue/marketing")
    @PreAuthorize("hasRole('MARKETING')")
    public ResponseEntity<ApiResponse<List<LoanQueueItemDTO>>> getMarketingQueue() {
        List<String> statuses = Arrays.asList(
                LoanStatus.SUBMITTED.name(),
                LoanStatus.IN_REVIEW.name());
        List<LoanQueueItemDTO> queue = getQueueItems(statuses);
        return ResponseUtil.ok(queue, "Marketing queue retrieved successfully");
    }

    @GetMapping("/queue/branch-manager")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LoanQueueItemDTO>>> getBranchManagerQueue() {
        List<String> statuses = Arrays.asList(LoanStatus.WAITING_APPROVAL.name());
        List<LoanQueueItemDTO> queue = getQueueItems(statuses);
        return ResponseUtil.ok(queue, "Branch manager queue retrieved successfully");
    }

    @GetMapping("/queue/back-office")
    @PreAuthorize("hasRole('BACK_OFFICE')")
    public ResponseEntity<ApiResponse<List<LoanQueueItemDTO>>> getBackOfficeQueue() {
        List<String> statuses = Arrays.asList(LoanStatus.APPROVED_WAITING_DISBURSEMENT.name());
        List<LoanQueueItemDTO> queue = getQueueItems(statuses);
        return ResponseUtil.ok(queue, "Back office queue retrieved successfully");
    }

    @GetMapping("/{loanId}/allowed-actions")
    public ResponseEntity<ApiResponse<List<String>>> getAllowedActions(@PathVariable Long loanId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan application not found with id: " + loanId));

        List<String> userRoles = getCurrentUserRoles();
        List<String> allowedActions = loanWorkflowService.getAllowedActions(
                loanApplication.getCurrentStatus(),
                userRoles);

        return ResponseUtil.ok(allowedActions, "Allowed actions retrieved successfully");
    }

    private List<LoanQueueItemDTO> getQueueItems(List<String> statuses) {
        List<LoanApplication> loans = loanApplicationRepository.findByCurrentStatusInOrderByCreatedAtDesc(statuses);
        List<String> userRoles = getCurrentUserRoles();

        return loans.stream()
                .map(loan -> {
                    List<String> allowedActions = loanWorkflowService.getAllowedActions(
                            loan.getCurrentStatus(),
                            userRoles);

                    return LoanQueueItemDTO.builder()
                            .loanApplicationId(loan.getLoanApplicationId())
                            .userId(loan.getUser().getId())
                            .username(loan.getUser().getUsername())
                            .userEmail(loan.getUser().getEmail())
                            .productId(loan.getProduct().getId())
                            .productName(loan.getProduct().getName())
                            .amount(loan.getAmount())
                            .tenureMonths(loan.getTenureMonths())
                            .interestRateApplied(loan.getInterestRateApplied())
                            .currentStatus(loan.getCurrentStatus())
                            .createdAt(loan.getCreatedAt())
                            .updatedAt(loan.getUpdatedAt())
                            .allowedActions(allowedActions)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }

        // Fallback for testing - return first user
        return userRepository.findAll().stream()
                .findFirst()
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("No users found"));
    }

    private List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
        }

        // Fallback for testing - return all roles
        return Arrays.asList("MARKETING", "BRANCH_MANAGER", "BACK_OFFICE");
    }
}
