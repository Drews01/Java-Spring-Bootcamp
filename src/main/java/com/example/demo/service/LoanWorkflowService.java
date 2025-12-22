package com.example.demo.service;

import com.example.demo.dto.LoanActionRequest;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.LoanSubmitRequest;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.LoanHistory;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.LoanAction;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.LoanHistoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanWorkflowService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanHistoryRepository loanHistoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Transactional
    public LoanApplicationDTO submitLoan(LoanSubmitRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Create loan application with SUBMITTED status
        LoanApplication loanApplication = LoanApplication.builder()
                .user(user)
                .product(product)
                .amount(request.getAmount())
                .tenureMonths(request.getTenureMonths())
                .interestRateApplied(request.getInterestRateApplied())
                .currentStatus(LoanStatus.SUBMITTED.name())
                .build();

        LoanApplication saved = loanApplicationRepository.save(loanApplication);

        // Create history entry for SUBMIT action
        createHistoryEntry(saved, user, LoanAction.SUBMIT.name(), null, null, LoanStatus.SUBMITTED.name());

        log.info("Loan application {} submitted by user {}", saved.getLoanApplicationId(), user.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public LoanApplicationDTO performAction(LoanActionRequest request, Long actorUserId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanApplicationId())
                .orElseThrow(() -> new RuntimeException(
                        "Loan application not found with id: " + request.getLoanApplicationId()));

        User actorUser = userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + actorUserId));

        String currentStatus = loanApplication.getCurrentStatus();
        String action = request.getAction();

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
        }

        // Create history entry
        createHistoryEntry(loanApplication, actorUser, action, request.getComment(), fromStatus, toStatus);

        // Send notifications based on status change
        sendNotifications(loanApplication, fromStatus, toStatus);

        log.info("Loan application {} action {} performed by user {}: {} -> {}",
                loanApplication.getLoanApplicationId(), action, actorUserId, fromStatus, toStatus);

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
                break;

            case "WAITING_APPROVAL":
                if (loanAction != LoanAction.COMMENT && loanAction != LoanAction.APPROVE
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

    private void createHistoryEntry(LoanApplication loanApplication, User actorUser, String action,
            String comment, String fromStatus, String toStatus) {
        LoanHistory history = LoanHistory.builder()
                .loanApplication(loanApplication)
                .actorUser(actorUser)
                .action(action)
                .comment(comment)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .build();

        loanHistoryRepository.save(history);
    }

    private void sendNotifications(LoanApplication loanApplication, String fromStatus, String toStatus) {
        try {
            // SUBMITTED -> IN_REVIEW: Notify customer
            if ("SUBMITTED".equals(fromStatus) && "IN_REVIEW".equals(toStatus)) {
                notificationService.createNotification(
                        com.example.demo.dto.NotificationDTO.builder()
                                .userId(loanApplication.getUser().getId())
                                .relatedLoanApplicationId(loanApplication.getLoanApplicationId())
                                .notifType("IN_REVIEW")
                                .channel("IN_APP")
                                .message("Your loan application is being reviewed")
                                .build());
            }

            // IN_REVIEW -> WAITING_APPROVAL: Notify branch managers
            if ("IN_REVIEW".equals(fromStatus) && "WAITING_APPROVAL".equals(toStatus)) {
                List<User> branchManagers = userRepository.findByRoles_Name("BRANCH_MANAGER");
                for (User manager : branchManagers) {
                    notificationService.createNotification(
                            com.example.demo.dto.NotificationDTO.builder()
                                    .userId(manager.getId())
                                    .relatedLoanApplicationId(loanApplication.getLoanApplicationId())
                                    .notifType("APPROVAL_REQUIRED")
                                    .channel("IN_APP")
                                    .message("New loan application awaiting your approval")
                                    .build());
                }
            }

            // WAITING_APPROVAL -> APPROVED_WAITING_DISBURSEMENT: Notify customer + back
            // office
            if ("WAITING_APPROVAL".equals(fromStatus) && "APPROVED_WAITING_DISBURSEMENT".equals(toStatus)) {
                // Notify customer
                notificationService.createNotification(
                        com.example.demo.dto.NotificationDTO.builder()
                                .userId(loanApplication.getUser().getId())
                                .relatedLoanApplicationId(loanApplication.getLoanApplicationId())
                                .notifType("APPROVED")
                                .channel("IN_APP")
                                .message("Your loan application has been approved")
                                .build());

                // Notify back office users
                List<User> backOfficeUsers = userRepository.findByRoles_Name("BACK_OFFICE");
                for (User backOffice : backOfficeUsers) {
                    notificationService.createNotification(
                            com.example.demo.dto.NotificationDTO.builder()
                                    .userId(backOffice.getId())
                                    .relatedLoanApplicationId(loanApplication.getLoanApplicationId())
                                    .notifType("DISBURSEMENT_REQUIRED")
                                    .channel("IN_APP")
                                    .message("Approved loan awaiting disbursement")
                                    .build());
                }
            }

            // WAITING_APPROVAL -> REJECTED: Notify customer
            if ("WAITING_APPROVAL".equals(fromStatus) && "REJECTED".equals(toStatus)) {
                notificationService.createNotification(
                        com.example.demo.dto.NotificationDTO.builder()
                                .userId(loanApplication.getUser().getId())
                                .relatedLoanApplicationId(loanApplication.getLoanApplicationId())
                                .notifType("REJECTED")
                                .channel("IN_APP")
                                .message("Your loan application has been rejected")
                                .build());
            }

            // APPROVED_WAITING_DISBURSEMENT -> DISBURSED: Notify customer
            if ("APPROVED_WAITING_DISBURSEMENT".equals(fromStatus) && "DISBURSED".equals(toStatus)) {
                notificationService.createNotification(
                        com.example.demo.dto.NotificationDTO.builder()
                                .userId(loanApplication.getUser().getId())
                                .relatedLoanApplicationId(loanApplication.getLoanApplicationId())
                                .notifType("DISBURSED")
                                .channel("IN_APP")
                                .message("Your loan has been disbursed")
                                .build());
            }
        } catch (Exception e) {
            log.error("Error sending notifications for loan {}: {}", loanApplication.getLoanApplicationId(),
                    e.getMessage());
            // Don't fail the transaction if notification fails
        }
    }

    public List<String> getAllowedActions(String currentStatus, List<String> userRoles) {
        List<String> allowedActions = new ArrayList<>();

        switch (currentStatus) {
            case "SUBMITTED":
            case "IN_REVIEW":
                if (userRoles.contains("MARKETING")) {
                    allowedActions.add(LoanAction.COMMENT.name());
                    if ("IN_REVIEW".equals(currentStatus)) {
                        allowedActions.add(LoanAction.FORWARD_TO_MANAGER.name());
                    }
                }
                break;

            case "WAITING_APPROVAL":
                if (userRoles.contains("BRANCH_MANAGER")) {
                    allowedActions.add(LoanAction.COMMENT.name());
                    allowedActions.add(LoanAction.APPROVE.name());
                    allowedActions.add(LoanAction.REJECT.name());
                }
                break;

            case "APPROVED_WAITING_DISBURSEMENT":
                if (userRoles.contains("BACK_OFFICE")) {
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
                .currentStatus(loanApplication.getCurrentStatus())
                .createdAt(loanApplication.getCreatedAt())
                .updatedAt(loanApplication.getUpdatedAt())
                .build();
    }
}
