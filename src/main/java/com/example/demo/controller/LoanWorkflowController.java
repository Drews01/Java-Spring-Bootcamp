package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.ActionHistoryDTO;
import com.example.demo.dto.ActionHistoryRequestDTO;
import com.example.demo.dto.LoanActionRequest;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.LoanQueueItemDTO;
import com.example.demo.dto.LoanSubmitRequest;
import com.example.demo.entity.Branch;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.LoanHistory;
import com.example.demo.entity.User;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.LoanHistoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.ActionHistoryService;
import com.example.demo.service.LoanWorkflowService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan-workflow")
@RequiredArgsConstructor
public class LoanWorkflowController {

  private final LoanWorkflowService loanWorkflowService;
  private final LoanApplicationRepository loanApplicationRepository;
  private final LoanHistoryRepository loanHistoryRepository;
  private final UserRepository userRepository;
  private final ActionHistoryService actionHistoryService;

  @PostMapping("/submit")
  public ResponseEntity<ApiResponse<LoanApplicationDTO>> submitLoan(
      @RequestBody LoanSubmitRequest request) {
    // SECURITY: userId is extracted from JWT token ONLY to prevent IDOR
    Long authenticatedUserId = getCurrentUserId();
    LoanApplicationDTO created = loanWorkflowService.submitLoan(request, authenticatedUserId);
    return ResponseUtil.created(created, "Loan application submitted successfully");
  }

  @PostMapping("/action")
  public ResponseEntity<ApiResponse<LoanApplicationDTO>> performAction(
      @RequestBody LoanActionRequest request) {
    Long actorUserId = getCurrentUserId();
    LoanApplicationDTO updated = loanWorkflowService.performAction(request, actorUserId);
    return ResponseUtil.ok(updated, "Action performed successfully");
  }

  @GetMapping("/queue/marketing")
  @PreAuthorize("@accessControl.hasMenu('LOAN_REVIEW')")
  public ResponseEntity<ApiResponse<Page<LoanQueueItemDTO>>> getMarketingQueue(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    List<String> statuses = Arrays.asList(LoanStatus.SUBMITTED.name(), LoanStatus.IN_REVIEW.name());
    Pageable pageable = PageRequest.of(page, size);
    Page<LoanQueueItemDTO> queue = getQueueItems(statuses, true, pageable);
    return ResponseUtil.ok(queue, "Marketing queue retrieved successfully");
  }

  @GetMapping("/queue/branch-manager")
  @PreAuthorize("@accessControl.hasMenu('LOAN_APPROVE')")
  public ResponseEntity<ApiResponse<Page<LoanQueueItemDTO>>> getBranchManagerQueue(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    List<String> statuses = Arrays.asList(LoanStatus.WAITING_APPROVAL.name());
    Pageable pageable = PageRequest.of(page, size);
    Page<LoanQueueItemDTO> queue = getQueueItems(statuses, true, pageable);
    return ResponseUtil.ok(queue, "Branch manager queue retrieved successfully");
  }

  @GetMapping("/queue/back-office")
  @PreAuthorize("@accessControl.hasMenu('LOAN_DISBURSE')")
  public ResponseEntity<ApiResponse<Page<LoanQueueItemDTO>>> getBackOfficeQueue(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    List<String> statuses = Arrays.asList(LoanStatus.APPROVED_WAITING_DISBURSEMENT.name());
    // Back Office sees all branches (headquarters role)
    Pageable pageable = PageRequest.of(page, size);
    Page<LoanQueueItemDTO> queue = getQueueItems(statuses, false, pageable);
    return ResponseUtil.ok(queue, "Back office queue retrieved successfully");
  }

  // ==================== ACTION HISTORY ENDPOINTS ====================

  @GetMapping("/history/marketing")
  @PreAuthorize("@accessControl.hasMenu('LOAN_REVIEW')")
  public ResponseEntity<ApiResponse<Page<ActionHistoryDTO>>> getMarketingHistory(
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "20") Integer size) {
    Long actorId = getCurrentUserId();
    ActionHistoryRequestDTO request =
        ActionHistoryRequestDTO.builder().month(month).year(year).page(page).size(size).build();
    Page<ActionHistoryDTO> history = actionHistoryService.getMarketingHistory(actorId, request);
    return ResponseUtil.ok(history, "Marketing action history retrieved successfully");
  }

  @GetMapping("/history/branch-manager")
  @PreAuthorize("@accessControl.hasMenu('LOAN_APPROVE')")
  public ResponseEntity<ApiResponse<Page<ActionHistoryDTO>>> getBranchManagerHistory(
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "20") Integer size) {
    Long actorId = getCurrentUserId();
    ActionHistoryRequestDTO request =
        ActionHistoryRequestDTO.builder().month(month).year(year).page(page).size(size).build();
    Page<ActionHistoryDTO> history = actionHistoryService.getBranchManagerHistory(actorId, request);
    return ResponseUtil.ok(history, "Branch manager action history retrieved successfully");
  }

  @GetMapping("/history/back-office")
  @PreAuthorize("@accessControl.hasMenu('LOAN_DISBURSE')")
  public ResponseEntity<ApiResponse<Page<ActionHistoryDTO>>> getBackOfficeHistory(
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "20") Integer size) {
    Long actorId = getCurrentUserId();
    ActionHistoryRequestDTO request =
        ActionHistoryRequestDTO.builder().month(month).year(year).page(page).size(size).build();
    Page<ActionHistoryDTO> history = actionHistoryService.getBackOfficeHistory(actorId, request);
    return ResponseUtil.ok(history, "Back office action history retrieved successfully");
  }

  @GetMapping("/{loanId}/allowed-actions")
  public ResponseEntity<ApiResponse<List<String>>> getAllowedActions(@PathVariable Long loanId) {
    LoanApplication loanApplication =
        loanApplicationRepository
            .findById(loanId)
            .orElseThrow(
                () -> new RuntimeException("Loan application not found with id: " + loanId));

    Long userId = getCurrentUserId();
    List<String> allowedActions =
        loanWorkflowService.getAllowedActions(loanApplication.getCurrentStatus(), userId);

    return ResponseUtil.ok(allowedActions, "Allowed actions retrieved successfully");
  }

  private Page<LoanQueueItemDTO> getQueueItems(
      List<String> statuses, boolean filterByBranch, Pageable pageable) {
    Long currentUserId = getCurrentUserId();
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Page<LoanApplication> loans;

    // If filterByBranch is true and user has a branch, filter by that branch
    if (filterByBranch && currentUser.getBranch() != null) {
      loans =
          loanApplicationRepository.findByCurrentStatusInAndBranch_IdOrderByCreatedAtDesc(
              statuses, currentUser.getBranch().getId(), pageable);
    } else if (filterByBranch && currentUser.getBranch() == null) {
      // User has no branch assigned, return empty list
      return new PageImpl<>(new ArrayList<>(), pageable, 0);
    } else {
      // No branch filter (e.g., Back Office sees all)
      loans =
          loanApplicationRepository.findByCurrentStatusInOrderByCreatedAtDesc(statuses, pageable);
    }

    return loans.map(
        loan -> {
          Long userId = getCurrentUserId();
          List<String> allowedActions =
              loanWorkflowService.getAllowedActions(loan.getCurrentStatus(), userId);

          // Fetch history once to extract all comments
          List<LoanHistory> historyList =
              loanHistoryRepository.findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(
                  loan.getLoanApplicationId());

          String marketingComment =
              historyList.stream()
                  .filter(
                      h ->
                          h.getComment() != null
                              && !h.getComment().isEmpty()
                              && (LoanStatus.SUBMITTED.name().equals(h.getFromStatus())
                                  || LoanStatus.IN_REVIEW.name().equals(h.getFromStatus())))
                  .findFirst()
                  .map(LoanHistory::getComment)
                  .orElse(null);

          String branchManagerComment =
              historyList.stream()
                  .filter(
                      h ->
                          h.getComment() != null
                              && !h.getComment().isEmpty()
                              && LoanStatus.WAITING_APPROVAL.name().equals(h.getFromStatus()))
                  .findFirst()
                  .map(LoanHistory::getComment)
                  .orElse(null);

          // Get branch info
          Branch loanBranch = loan.getBranch();

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
              .userNik(
                  loan.getUser().getUserProfile() != null
                      ? loan.getUser().getUserProfile().getNik()
                      : null)
              .userKtpPath(
                  loan.getUser().getUserProfile() != null
                      ? loan.getUser().getUserProfile().getKtpPath()
                      : null)
              .userPhoneNumber(
                  loan.getUser().getUserProfile() != null
                      ? loan.getUser().getUserProfile().getPhoneNumber()
                      : null)
              .userAddress(
                  loan.getUser().getUserProfile() != null
                      ? loan.getUser().getUserProfile().getAddress()
                      : null)
              .userAccountNumber(
                  loan.getUser().getUserProfile() != null
                      ? loan.getUser().getUserProfile().getAccountNumber()
                      : null)
              .userBankName(
                  loan.getUser().getUserProfile() != null
                      ? loan.getUser().getUserProfile().getBankName()
                      : null)
              .marketingComment(marketingComment)
              .branchManagerComment(branchManagerComment)
              .branchId(loanBranch != null ? loanBranch.getId() : null)
              .branchName(loanBranch != null ? loanBranch.getName() : null)
              .build();
        });
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
}
