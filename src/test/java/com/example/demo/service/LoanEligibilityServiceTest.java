package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.config.TestConfig;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProduct;
import com.example.demo.enums.LoanStatus;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserProductRepository;
import com.example.demo.repository.UserRepository;
import java.util.Arrays;
import java.util.Collections;
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
public class LoanEligibilityServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private ProductRepository productRepository;
  @Mock private UserProductRepository userProductRepository;
  @Mock private LoanApplicationRepository loanApplicationRepository;

  @InjectMocks private LoanEligibilityService loanEligibilityService;

  private User user;
  private Product productBronze;
  private Product productSilver;
  private UserProduct userProduct;

  @BeforeEach
  void setUp() {
    user = User.builder().id(1L).username("testuser").build();

    productBronze =
        Product.builder()
            .id(1L)
            .name("Bronze")
            .tierOrder(1)
            .creditLimit(1000000.0)
            .upgradeThreshold(5000000.0)
            .build();

    productSilver =
        Product.builder()
            .id(2L)
            .name("Silver")
            .tierOrder(2)
            .creditLimit(5000000.0)
            .upgradeThreshold(10000000.0)
            .build();

    userProduct =
        UserProduct.builder()
            .user(user)
            .product(productBronze)
            .status("ACTIVE")
            .currentUsedAmount(0.0)
            .totalPaidAmount(0.0)
            .build();
  }

  @Test
  void canApplyForLoan_WhenAmountWithinLimit_ShouldReturnTrue() {
    // Arrange
    when(userProductRepository.findActiveUserProductsByUserIdOrderByTier(user.getId()))
        .thenReturn(Collections.singletonList(userProduct));
    when(loanApplicationRepository.findTotalActiveLoanAmount(user.getId())).thenReturn(0.0);

    // Act
    boolean result = loanEligibilityService.canApplyForLoan(user.getId(), 500000.0);

    // Assert
    assertTrue(result);
  }

  @Test
  void canApplyForLoan_WhenAmountExceedsLimit_ShouldReturnFalse() {
    // Arrange
    when(userProductRepository.findActiveUserProductsByUserIdOrderByTier(user.getId()))
        .thenReturn(Collections.singletonList(userProduct));
    when(loanApplicationRepository.findTotalActiveLoanAmount(user.getId())).thenReturn(0.0);

    // Act
    boolean result = loanEligibilityService.canApplyForLoan(user.getId(), 1500000.0);

    // Assert
    assertFalse(result);
  }

  @Test
  void getRemainingCreditLimit_ShouldReturnCorrectValue() {
    // Arrange
    when(userProductRepository.findActiveUserProductsByUserIdOrderByTier(user.getId()))
        .thenReturn(Collections.singletonList(userProduct));
    when(loanApplicationRepository.findTotalActiveLoanAmount(user.getId())).thenReturn(200000.0);

    // Act
    Double remaining = loanEligibilityService.getRemainingCreditLimit(user.getId());

    // Assert
    assertEquals(800000.0, remaining);
  }

  @Test
  void getCurrentTierProduct_WhenUserHasProduct_ShouldReturnHighestTier() {
    // Arrange
    UserProduct silverProduct =
        UserProduct.builder().user(user).product(productSilver).status("ACTIVE").build();
    when(userProductRepository.findActiveUserProductsByUserIdOrderByTier(user.getId()))
        .thenReturn(Arrays.asList(silverProduct, userProduct)); // Mock returning ordered list

    // Act
    Product result = loanEligibilityService.getCurrentTierProduct(user.getId());

    // Assert
    assertEquals("Silver", result.getName());
  }

  @Test
  void assignDefaultProduct_ShouldAssignBronzeTier() {
    // Arrange
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(productRepository.findByTierOrderAndDeletedFalse(1))
        .thenReturn(Optional.of(productBronze));
    when(userProductRepository.findByUser_IdAndProduct_Id(user.getId(), productBronze.getId()))
        .thenReturn(Optional.empty());
    when(userProductRepository.save(any(UserProduct.class))).thenReturn(userProduct);

    // Act
    UserProduct result = loanEligibilityService.assignDefaultProduct(user.getId());

    // Assert
    assertNotNull(result);
    assertEquals("Bronze", result.getProduct().getName());
  }

  @Test
  void processLoanPayment_ShouldMarkLoanAsPaid_AndCheckUpgrade() {
    // Arrange
    Long loanId = 100L;
    LoanApplication loan =
        LoanApplication.builder()
            .loanApplicationId(loanId)
            .user(user)
            .product(productBronze)
            .amount(100000.0)
            .currentStatus(LoanStatus.DISBURSED.name())
            .isPaid(false)
            .build();

    when(loanApplicationRepository.findById(loanId)).thenReturn(Optional.of(loan));
    when(userProductRepository.findActiveUserProductsByUserIdOrderByTier(user.getId()))
        .thenReturn(Collections.singletonList(userProduct));
    // Check upgrade Logic calls
    when(loanApplicationRepository.findTotalUnpaidAmountByUserId(user.getId())).thenReturn(0.0);

    // Act
    loanEligibilityService.processLoanPayment(loanId);

    // Assert
    verify(loanApplicationRepository).save(loan);
    assertEquals(LoanStatus.PAID.name(), loan.getCurrentStatus());
    assertTrue(loan.getIsPaid());
    assertNotNull(loan.getPaidAt());
    assertEquals(100000.0, userProduct.getTotalPaidAmount());
  }

  @Test
  void checkAndUpgradeTier_WhenEligible_ShouldUpgrade() {
    // Arrange
    userProduct.setTotalPaidAmount(6000000.0); // Exceeds 5M threshold for Bronze->Silver
    when(userProductRepository.findActiveUserProductsByUserIdOrderByTier(user.getId()))
        .thenReturn(Collections.singletonList(userProduct));
    when(productRepository.findByTierOrderAndDeletedFalse(2))
        .thenReturn(Optional.of(productSilver));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    // Act
    loanEligibilityService.checkAndUpgradeTier(user.getId());

    // Assert
    verify(userProductRepository, times(2)).save(any(UserProduct.class)); // 1 for new, 1 for old
    assertEquals("UPGRADED", userProduct.getStatus());
  }

  @Test
  void updateUsedAmount_ShouldIncreaseUsedAmount() {
    // Arrange
    when(userProductRepository.findActiveUserProductsByUserIdOrderByTier(user.getId()))
        .thenReturn(Collections.singletonList(userProduct));

    // Act
    loanEligibilityService.updateUsedAmount(user.getId(), 50000.0);

    // Assert
    assertEquals(50000.0, userProduct.getCurrentUsedAmount());
    verify(userProductRepository).save(userProduct);
  }
}
