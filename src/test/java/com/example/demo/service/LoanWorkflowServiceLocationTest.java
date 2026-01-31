package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.LoanApplicationDTO;
import com.example.demo.dto.LoanSubmitRequest;
import com.example.demo.entity.Branch;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.BranchRepository;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.LoanHistoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanWorkflowServiceLocationTest {

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

  private LoanWorkflowService loanWorkflowService;

  @BeforeEach
  void setUp() {
    loanWorkflowService =
        new LoanWorkflowService(
            loanApplicationRepository,
            loanHistoryRepository,
            userRepository,
            productRepository,
            branchRepository,
            notificationService,
            accessControl,
            loanEligibilityService,
            userProfileService,
            emailService,
            loanNotificationService);
  }

  @Test
  void submitLoan_ShouldSaveLocationData() {
    // Arrange
    Long userId = 1L;
    LoanSubmitRequest request =
        LoanSubmitRequest.builder()
            .amount(1000000.0)
            .tenureMonths(12)
            .branchId(1L)
            .latitude(-6.2088)
            .longitude(106.8456)
            .build();

    User user = User.builder().id(userId).build();
    Product product = Product.builder().id(1L).interestRate(10.0).build();
    Branch branch = Branch.builder().id(1L).build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(loanEligibilityService.getCurrentTierProduct(userId)).thenReturn(product);
    when(userProfileService.isProfileComplete(userId)).thenReturn(true);
    when(loanApplicationRepository.hasActiveLoan(userId)).thenReturn(false);
    when(loanEligibilityService.canApplyForLoan(userId, request.getAmount())).thenReturn(true);
    when(branchRepository.findById(request.getBranchId())).thenReturn(Optional.of(branch));
    when(loanApplicationRepository.save(any(LoanApplication.class)))
        .thenAnswer(
            invocation -> {
              LoanApplication saved = invocation.getArgument(0);
              saved.setLoanApplicationId(101L);
              return saved;
            });

    // Act
    LoanApplicationDTO result = loanWorkflowService.submitLoan(request, userId);

    // Assert
    ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
    verify(loanApplicationRepository).save(captor.capture());
    LoanApplication capturedLoan = captor.getValue();

    assertEquals(-6.2088, capturedLoan.getLatitude());
    assertEquals(106.8456, capturedLoan.getLongitude());

    // Also verify DTO result
    assertEquals(-6.2088, result.getLatitude());
    assertEquals(106.8456, result.getLongitude());
  }
}
