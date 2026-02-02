package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.LoanActionRequest;
import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.LoanSubmitRequest;
import com.example.demo.entity.Branch;
import com.example.demo.entity.LoanApplication;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class LoanWorkflowServiceTest {

  @Mock private LoanApplicationRepository loanApplicationRepository;
  @Mock private LoanHistoryRepository loanHistoryRepository;
  @Mock private UserRepository userRepository;
  @Mock private ProductRepository productRepository;
  @Mock private BranchRepository branchRepository;
  @Mock private NotificationService notificationService;
  @Mock private AccessControlService accessControl;
  @Mock private LoanEligibilityService loanEligibilityService;
  @Mock private UserProfileService userProfileService;
  @Mock private EmailService emailService;
  @Mock private LoanNotificationService loanNotificationService;

  @InjectMocks private LoanWorkflowService loanWorkflowService;

  private User user;
  private User manager;
  private Product product;
  private Branch branch;
  private LoanApplication loanApplication;

  @BeforeEach
  void setUp() {
    user = User.builder().id(1L).username("user").build();
    manager = User.builder().id(2L).username("manager").build();
    product = Product.builder().id(1L).name("Bronze").interestRate(10.0).build();
    branch = Branch.builder().id(1L).name("Main Branch").build();

    loanApplication =
        LoanApplication.builder()
            .loanApplicationId(100L)
            .user(user)
            .product(product)
            .currentStatus(LoanStatus.SUBMITTED.name())
            .amount(1000000.0)
            .build();
  }

  @Test
  void submitLoan_WithValidRequest_ShouldCreateLoan() {
    // Arrange
    LoanSubmitRequest request =
        LoanSubmitRequest.builder().amount(1000000.0).tenureMonths(12).branchId(1L).build();

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(loanEligibilityService.getCurrentTierProduct(user.getId())).thenReturn(product);
    when(userProfileService.isProfileComplete(user.getId())).thenReturn(true);
    when(loanApplicationRepository.hasActiveLoan(user.getId())).thenReturn(false);
    when(loanEligibilityService.canApplyForLoan(user.getId(), request.getAmount()))
        .thenReturn(true);
    when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
    when(loanApplicationRepository.save(any(LoanApplication.class)))
        .thenAnswer(i -> i.getArguments()[0]);

    // Act
    LoanApplicationDTO result = loanWorkflowService.submitLoan(request, user.getId());

    // Assert
    assertNotNull(result);
    assertEquals(LoanStatus.SUBMITTED.name(), result.getCurrentStatus());
    verify(loanEligibilityService).updateUsedAmount(eq(user.getId()), eq(request.getAmount()));
  }

  @Test
  void submitLoan_WhenProfileIncomplete_ShouldThrowException() {
    // Arrange
    LoanSubmitRequest request = LoanSubmitRequest.builder().amount(1000.0).build();
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(loanEligibilityService.getCurrentTierProduct(user.getId())).thenReturn(product);
    when(userProfileService.isProfileComplete(user.getId())).thenReturn(false);

    // Act & Assert
    assertThrows(
        BusinessException.class, () -> loanWorkflowService.submitLoan(request, user.getId()));
  }

  @Test
  void performAction_Comment_FromSubmitted_ShouldMoveToInReview() {
    // Arrange
    LoanActionRequest request = new LoanActionRequest();
    request.setLoanApplicationId(loanApplication.getLoanApplicationId());
    request.setAction(LoanAction.COMMENT.name());
    request.setComment("Reviewing");

    when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(loanApplication));
    when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
    when(accessControl.hasMenu("LOAN_REVIEW")).thenReturn(true);

    // Act
    LoanApplicationDTO result = loanWorkflowService.performAction(request, manager.getId());

    // Assert
    assertEquals(LoanStatus.IN_REVIEW.name(), result.getCurrentStatus());
    verify(loanNotificationService)
        .notifyLoanStatusChange(
            any(), eq(LoanStatus.SUBMITTED.name()), eq(LoanStatus.IN_REVIEW.name()));
  }

  @Test
  void performAction_ForwardToManager_FromInReview_ShouldMoveToWaitingApproval() {
    // Arrange
    loanApplication.setCurrentStatus(LoanStatus.IN_REVIEW.name());
    LoanActionRequest request = new LoanActionRequest();
    request.setLoanApplicationId(100L);
    request.setAction(LoanAction.FORWARD_TO_MANAGER.name());

    when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(loanApplication));
    when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
    when(accessControl.hasMenu("LOAN_REVIEW")).thenReturn(true);

    // Act
    LoanApplicationDTO result = loanWorkflowService.performAction(request, manager.getId());

    // Assert
    assertEquals(LoanStatus.WAITING_APPROVAL.name(), result.getCurrentStatus());
  }

  @Test
  void performAction_Approve_FromWaitingApproval_ShouldMoveToApprovedWaitingDisbursement() {
    // Arrange
    loanApplication.setCurrentStatus(LoanStatus.WAITING_APPROVAL.name());
    LoanActionRequest request = new LoanActionRequest();
    request.setLoanApplicationId(100L);
    request.setAction(LoanAction.APPROVE.name());

    when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(loanApplication));
    when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
    when(accessControl.hasMenu("LOAN_APPROVE")).thenReturn(true);

    // Act
    LoanApplicationDTO result = loanWorkflowService.performAction(request, manager.getId());

    // Assert
    assertEquals(LoanStatus.APPROVED_WAITING_DISBURSEMENT.name(), result.getCurrentStatus());
  }

  @Test
  void performAction_Reject_FromWaitingApproval_ShouldMoveToRejectedAndRecalculate() {
    // Arrange
    loanApplication.setCurrentStatus(LoanStatus.WAITING_APPROVAL.name());
    LoanActionRequest request = new LoanActionRequest();
    request.setLoanApplicationId(100L);
    request.setAction(LoanAction.REJECT.name());

    when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(loanApplication));
    when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
    // Check allow REJECT
    when(accessControl.hasMenu("LOAN_APPROVE")).thenReturn(true);

    // Act
    LoanApplicationDTO result = loanWorkflowService.performAction(request, manager.getId());

    // Assert
    assertEquals(LoanStatus.REJECTED.name(), result.getCurrentStatus());
    verify(loanEligibilityService).recalculateUsedAmount(user.getId());
  }

  @Test
  void performAction_Disburse_FromApprovedWaitingDisbursement_ShouldMoveToDisbursed() {
    // Arrange
    loanApplication.setCurrentStatus(LoanStatus.APPROVED_WAITING_DISBURSEMENT.name());
    LoanActionRequest request = new LoanActionRequest();
    request.setLoanApplicationId(100L);
    request.setAction(LoanAction.DISBURSE.name());

    when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(loanApplication));
    when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
    when(accessControl.hasMenu("LOAN_DISBURSE")).thenReturn(true);

    // Act
    LoanApplicationDTO result = loanWorkflowService.performAction(request, manager.getId());

    // Assert
    assertEquals(LoanStatus.DISBURSED.name(), result.getCurrentStatus());
  }

  @Test
  void getAllowedActions_ShouldReturnCorrectActionsForStatus() {
    // Arrange
    when(accessControl.hasMenu("LOAN_REVIEW")).thenReturn(true);

    // Act
    List<String> actions =
        loanWorkflowService.getAllowedActions(LoanStatus.SUBMITTED.name(), manager.getId());

    // Assert
    assertTrue(actions.contains(LoanAction.COMMENT.name()));
  }
}
