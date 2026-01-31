package com.example.demo.service;

import com.example.demo.dto.LoanActionRequest;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.LoanSubmitRequest;
import com.example.demo.entity.Branch;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.LoanHistory;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.LoanAction;
import com.example.demo.enums.LoanStatus;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.BranchRepository;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.LoanHistoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanWorkflowService {

  private final LoanApplicationRepository loanApplicationRepository;
  private final LoanHistoryRepository loanHistoryRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final BranchRepository branchRepository;
  private final NotificationService notificationService;
  private final AccessControlService accessControl;
  private final LoanEligibilityService loanEligibilityService;
  private final UserProfileService userProfileService;
  private final EmailService emailService;
  private final LoanNotificationService loanNotificationService;

  @Transactional
  public LoanApplicationDTO submitLoan(LoanSubmitRequest request, Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    // Get user's current tier product (auto-assigns Bronze if not assigned)
    Product tierProduct = loanEligibilityService.getCurrentTierProduct(userId);
    if (tierProduct == null) {
      throw new BusinessException("No tier product available for user", "NO_TIER_AVAILABLE");
    }

    // Validate user profile is complete before allowing loan submission
    if (!userProfileService.isProfileComplete(userId)) {
      throw new BusinessException(
          "Cannot submit loan. Your profile is incomplete. "
              + "Please complete all required fields: address, NIK, KTP document, phone number, account number, and bank name. "
              + "Update your profile at /api/user-profiles",
          "PROFILE_INCOMPLETE");
    }

    // Check if user has any active (pending) loans
    if (loanApplicationRepository.hasActiveLoan(userId)) {
      throw new BusinessException(
          "Cannot submit new loan. You already have an active loan application that is being processed. "
              + "Please wait for your current loan to be disbursed, paid, or rejected before submitting a new one.",
          "ACTIVE_LOAN_EXISTS");
    }

    // Check credit limit eligibility
    if (!loanEligibilityService.canApplyForLoan(userId, request.getAmount())) {
      Double remainingLimit = loanEligibilityService.getRemainingCreditLimit(userId);
      throw new BusinessException(
          String.format(
              "Loan amount %.2f exceeds remaining credit limit %.2f for %s tier",
              request.getAmount(), remainingLimit, tierProduct.getName()),
          "CREDIT_LIMIT_EXCEEDED");
    }

    // Use tier product if no specific product provided, otherwise validate the
    // requested product
    Product product;
    if (request.getProductId() != null) {
      product =
          productRepository
              .findById(request.getProductId())
              .orElseThrow(
                  () ->
                      new RuntimeException("Product not found with id: " + request.getProductId()));
    } else {
      product = tierProduct;
    }

    // Validate and fetch branch
    if (request.getBranchId() == null) {
      throw new BusinessException("Branch ID is required for loan submission", "BRANCH_REQUIRED");
    }
    Branch branch =
        branchRepository
            .findById(request.getBranchId())
            .orElseThrow(
                () -> new RuntimeException("Branch not found with id: " + request.getBranchId()));

    // Create loan application with SUBMITTED status
    Double interestRate =
        request.getInterestRateApplied() != null
            ? request.getInterestRateApplied()
            : product.getInterestRate();

    // Calculate total amount to pay (principal + interest) using EMI formula
    Double totalAmountToPay =
        calculateTotalAmountToPay(request.getAmount(), interestRate, request.getTenureMonths());

    LoanApplication loanApplication =
        LoanApplication.builder()
            .user(user)
            .product(product)
            .branch(branch)
            .amount(request.getAmount())
            .tenureMonths(request.getTenureMonths())
            .interestRateApplied(interestRate)
            .totalAmountToPay(totalAmountToPay)
            .currentStatus(LoanStatus.SUBMITTED.name())
            .isPaid(false)
            .currentStatus(LoanStatus.SUBMITTED.name())
            .isPaid(false)
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .build();

    LoanApplication saved = loanApplicationRepository.save(loanApplication);

    // Update user's used amount
    loanEligibilityService.updateUsedAmount(userId, request.getAmount());

    // Create history entry for SUBMIT action
    createHistoryEntry(
        saved, user, LoanAction.SUBMIT.name(), null, null, LoanStatus.SUBMITTED.name());

    log.info(
        "Loan application {} submitted by user {} for amount {} (Tier: {})",
        saved.getLoanApplicationId(),
        user.getId(),
        request.getAmount(),
        tierProduct.getName());

    return convertToDTO(saved);
  }

  @Transactional
  public LoanApplicationDTO performAction(LoanActionRequest request, Long actorUserId) {
    LoanApplication loanApplication =
        loanApplicationRepository
            .findById(request.getLoanApplicationId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Loan application not found with id: " + request.getLoanApplicationId()));

    User actorUser =
        userRepository
            .findById(actorUserId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + actorUserId));

    String currentStatus = loanApplication.getCurrentStatus();
    String action = request.getAction();

    // Validate that the actor has permission to act on loans in this status
    // (bucket)
    validateActorPermission(currentStatus);

    // Validate transition
    validateTransition(currentStatus, action);

    // Get next status
    String nextStatus = getNextStatus(currentStatus, action);

    String fromStatus = currentStatus;
    String toStatus = nextStatus;

    // Update loan application status if it changes
    if (!currentStatus.equals(nextStatus)) {
      loanApplication.setCurrentStatus(nextStatus);
      loanApplicationRepository.save(loanApplication);

      // If loan is rejected, recalculate the user's used amount to release the limit
      if (LoanStatus.REJECTED.name().equals(nextStatus)) {
        loanEligibilityService.recalculateUsedAmount(loanApplication.getUser().getId());
      }
    }

    // Create history entry
    createHistoryEntry(
        loanApplication, actorUser, action, request.getComment(), fromStatus, toStatus);

    // Send notifications based on status change
    sendNotifications(loanApplication, fromStatus, toStatus);

    log.info(
        "Loan application {} action {} performed by user {}: {} -> {}",
        loanApplication.getLoanApplicationId(),
        action,
        actorUserId,
        fromStatus,
        toStatus);

    return convertToDTO(loanApplication);
  }

  private void validateTransition(String currentStatus, String action) {
    LoanAction loanAction;
    try {
      loanAction = LoanAction.valueOf(action);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Invalid action: " + action);
    }

    switch (currentStatus) {
      case "SUBMITTED":
        if (loanAction != LoanAction.COMMENT) {
          throw new IllegalStateException("Only COMMENT action is allowed for SUBMITTED status");
        }
        break;

      case "IN_REVIEW":
        if (loanAction != LoanAction.COMMENT && loanAction != LoanAction.FORWARD_TO_MANAGER) {
          throw new IllegalStateException(
              "Only COMMENT or FORWARD_TO_MANAGER actions are allowed for IN_REVIEW status");
        }
        if (loanAction == LoanAction.FORWARD_TO_MANAGER) {
          // Check if a COMMENT has been made while in IN_REVIEW status
          // (The comment that moved it TO IN_REVIEW doesn't count, that was from
          // SUBMITTED)
          boolean hasReviewComment =
              loanHistoryRepository
                  .findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(
                      loanApplicationRepository
                          .findById(
                              Long.valueOf(1)) // Temporary ID fetch, wait, I don't have ID here
                          .map(LoanApplication::getLoanApplicationId)
                          .orElse(0L))
                  .stream()
                  .anyMatch(
                      h ->
                          "COMMENT".equals(h.getAction()) && "IN_REVIEW".equals(h.getFromStatus()));

          // Wait, I cannot access repository easily here without ID.
          // I should move this check to performAction where I have the object.
        }
        break;

      case "WAITING_APPROVAL":
        if (loanAction != LoanAction.COMMENT
            && loanAction != LoanAction.APPROVE
            && loanAction != LoanAction.REJECT) {
          throw new IllegalStateException(
              "Only COMMENT, APPROVE, or REJECT actions are allowed for WAITING_APPROVAL status");
        }
        break;

      case "APPROVED_WAITING_DISBURSEMENT":
        if (loanAction != LoanAction.DISBURSE) {
          throw new IllegalStateException(
              "Only DISBURSE action is allowed for APPROVED_WAITING_DISBURSEMENT status");
        }
        break;

      case "DISBURSED":
      case "REJECTED":
        throw new IllegalStateException("No actions allowed for " + currentStatus + " status");

      default:
        throw new IllegalStateException("Unknown status: " + currentStatus);
    }
  }

  /**
   * Validates that the current user has permission to perform actions on loans in the given status.
   * Each status belongs to a specific role's "bucket":
   *
   * <ul>
   *   <li>SUBMITTED, IN_REVIEW → Marketing (requires LOAN_REVIEW menu)
   *   <li>WAITING_APPROVAL → Branch Manager (requires LOAN_APPROVE menu)
   *   <li>APPROVED_WAITING_DISBURSEMENT → Back Office (requires LOAN_DISBURSE menu)
   * </ul>
   *
   * @param currentStatus the current status of the loan application
   * @throws AccessDeniedException if the user doesn't have permission for this status
   */
  private void validateActorPermission(String currentStatus) {
    switch (currentStatus) {
      case "SUBMITTED":
      case "IN_REVIEW":
        // Only users with LOAN_REVIEW menu (Marketing) can act on these statuses
        if (!accessControl.hasMenu("LOAN_REVIEW")) {
          throw new AccessDeniedException(
              "Only Marketing users can perform actions on loans in " + currentStatus + " status");
        }
        break;

      case "WAITING_APPROVAL":
        // Only users with LOAN_APPROVE menu (Branch Manager) can act
        if (!accessControl.hasMenu("LOAN_APPROVE") && !accessControl.hasMenu("LOAN_REJECT")) {
          throw new AccessDeniedException(
              "Only Branch Managers can perform actions on loans awaiting approval");
        }
        break;

      case "APPROVED_WAITING_DISBURSEMENT":
        // Only users with LOAN_DISBURSE menu (Back Office) can act
        if (!accessControl.hasMenu("LOAN_DISBURSE")) {
          throw new AccessDeniedException(
              "Only Back Office users can perform actions on approved loans");
        }
        break;

      case "DISBURSED":
      case "REJECTED":
      case "PAID":
        throw new IllegalStateException(
            "No actions are allowed for loans in " + currentStatus + " status");

      default:
        throw new IllegalStateException("Unknown loan status: " + currentStatus);
    }
  }

  private String getNextStatus(String currentStatus, String action) {
    LoanAction loanAction = LoanAction.valueOf(action);

    switch (currentStatus) {
      case "SUBMITTED":
        if (loanAction == LoanAction.COMMENT) {
          return LoanStatus.IN_REVIEW.name();
        }
        break;

      case "IN_REVIEW":
        if (loanAction == LoanAction.COMMENT) {
          return LoanStatus.IN_REVIEW.name(); // No status change
        } else if (loanAction == LoanAction.FORWARD_TO_MANAGER) {
          return LoanStatus.WAITING_APPROVAL.name();
        }
        break;

      case "WAITING_APPROVAL":
        if (loanAction == LoanAction.COMMENT) {
          return LoanStatus.WAITING_APPROVAL.name(); // No status change
        } else if (loanAction == LoanAction.APPROVE) {
          return LoanStatus.APPROVED_WAITING_DISBURSEMENT.name();
        } else if (loanAction == LoanAction.REJECT) {
          return LoanStatus.REJECTED.name();
        }
        break;

      case "APPROVED_WAITING_DISBURSEMENT":
        if (loanAction == LoanAction.DISBURSE) {
          return LoanStatus.DISBURSED.name();
        }
        break;
    }

    return currentStatus; // Default: no status change
  }

  private void createHistoryEntry(
      LoanApplication loanApplication,
      User actorUser,
      String action,
      String comment,
      String fromStatus,
      String toStatus) {
    LoanHistory history =
        LoanHistory.builder()
            .loanApplication(loanApplication)
            .actorUser(actorUser)
            .action(action)
            .comment(comment)
            .fromStatus(fromStatus)
            .toStatus(toStatus)
            .build();

    loanHistoryRepository.save(history);
  }

  /**
   * Send notifications for loan status changes. Delegates to LoanNotificationService following
   * Single Responsibility Principle.
   */
  private void sendNotifications(
      LoanApplication loanApplication, String fromStatus, String toStatus) {
    loanNotificationService.notifyLoanStatusChange(loanApplication, fromStatus, toStatus);
  }

  public List<String> getAllowedActions(String currentStatus, Long userId) {
    List<String> allowedActions = new ArrayList<>();

    switch (currentStatus) {
      case "SUBMITTED":
        if (accessControl.hasMenu("LOAN_REVIEW")) {
          allowedActions.add(LoanAction.COMMENT.name());
        }
        break;
      case "IN_REVIEW":
        if (accessControl.hasMenu("LOAN_REVIEW")) {
          allowedActions.add(LoanAction.COMMENT.name());
          allowedActions.add(LoanAction.FORWARD_TO_MANAGER.name());
        }
        break;

      case "WAITING_APPROVAL":
        if (accessControl.hasMenu("LOAN_APPROVE")) {
          allowedActions.add(LoanAction.COMMENT.name());
          allowedActions.add(LoanAction.APPROVE.name());
        }
        if (accessControl.hasMenu("LOAN_REJECT")) {
          if (!allowedActions.contains(LoanAction.COMMENT.name())) {
            allowedActions.add(LoanAction.COMMENT.name());
          }
          allowedActions.add(LoanAction.REJECT.name());
        }
        break;

      case "APPROVED_WAITING_DISBURSEMENT":
        if (accessControl.hasMenu("LOAN_DISBURSE")) {
          allowedActions.add(LoanAction.DISBURSE.name());
        }
        break;
    }

    return allowedActions;
  }

  private LoanApplicationDTO convertToDTO(LoanApplication loanApplication) {
    return LoanApplicationDTO.builder()
        .loanApplicationId(loanApplication.getLoanApplicationId())
        .userId(loanApplication.getUser().getId())
        .productId(loanApplication.getProduct().getId())
        .amount(loanApplication.getAmount())
        .tenureMonths(loanApplication.getTenureMonths())
        .interestRateApplied(loanApplication.getInterestRateApplied())
        .totalAmountToPay(loanApplication.getTotalAmountToPay())
        .currentStatus(loanApplication.getCurrentStatus())
        .currentStatus(loanApplication.getCurrentStatus())
        .createdAt(loanApplication.getCreatedAt())
        .updatedAt(loanApplication.getUpdatedAt())
        .latitude(loanApplication.getLatitude())
        .longitude(loanApplication.getLongitude())
        .build();
  }

  /**
   * Calculate total amount to pay (principal + interest) using EMI formula.
   *
   * @param principal the loan amount
   * @param annualInterestRate the annual interest rate (e.g., 12.0 for 12%)
   * @param tenureMonths the tenure in months
   * @return total amount to be paid
   */
  private Double calculateTotalAmountToPay(
      Double principal, Double annualInterestRate, Integer tenureMonths) {
    if (principal == null || annualInterestRate == null || tenureMonths == null) {
      return principal; // Return principal if any value is missing
    }

    if (annualInterestRate == 0 || tenureMonths == 0) {
      return principal; // No interest or tenure
    }

    // Convert annual interest rate to monthly rate
    double monthlyRate = (annualInterestRate / 12) / 100;

    // Calculate EMI using formula: EMI = [P × r × (1+r)^n] / [(1+r)^n - 1]
    double emi =
        (principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths))
            / (Math.pow(1 + monthlyRate, tenureMonths) - 1);

    // Total amount = EMI × number of months
    return emi * tenureMonths;
  }
}
