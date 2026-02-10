package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link LoanApplicationRepository}.
 *
 * <p>Tests repository methods including custom queries for loan application management.
 */
@DataJpaTest
@ActiveProfiles("test")
class LoanApplicationRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private LoanApplicationRepository loanApplicationRepository;

  private User testUser;
  private Product testProduct;

  @BeforeEach
  void setUp() {
    // Create test user
    testUser =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .isActive(true)
            .build();
    entityManager.persist(testUser);

    // Create test product
    testProduct =
        Product.builder()
            .name("Bronze Tier")
            .code("BRONZE")
            .interestRate(12.0)
            .creditLimit(10000000.0)
            .interestRateType("FIXED")
            .minAmount(100000.0)
            .maxAmount(5000000.0)
            .minTenureMonths(1)
            .maxTenureMonths(12)
            .tierOrder(1)
            .deleted(false)
            .build();
    entityManager.persist(testProduct);

    entityManager.flush();
  }

  @Test
  @DisplayName("Should find loan applications by user ID")
  void findByUser_IdOrderByCreatedAtDesc_shouldReturnUserLoans() {
    // Given
    LoanApplication loan1 =
        LoanApplication.builder()
            .user(testUser)
            .product(testProduct)
            .amount(1000000.0)
            .tenureMonths(12)
            .interestRateApplied(12.0)
            .currentStatus("SUBMITTED")
            .isPaid(false)
            .build();
    entityManager.persist(loan1);

    LoanApplication loan2 =
        LoanApplication.builder()
            .user(testUser)
            .product(testProduct)
            .amount(2000000.0)
            .tenureMonths(24)
            .interestRateApplied(12.0)
            .currentStatus("IN_REVIEW")
            .isPaid(false)
            .build();
    entityManager.persist(loan2);

    entityManager.flush();

    // When
    List<LoanApplication> loans =
        loanApplicationRepository.findByUser_IdOrderByCreatedAtDesc(testUser.getId());

    // Then
    assertThat(loans).hasSize(2);
    assertThat(loans)
        .extracting(LoanApplication::getAmount)
        .containsExactlyInAnyOrder(2000000.0, 1000000.0);
  }

  @Test
  @DisplayName("Should find loan applications by status")
  void findByCurrentStatus_shouldReturnLoansWithStatus() {
    // Given
    LoanApplication submittedLoan =
        LoanApplication.builder()
            .user(testUser)
            .product(testProduct)
            .amount(1000000.0)
            .tenureMonths(12)
            .interestRateApplied(12.0)
            .currentStatus("SUBMITTED")
            .isPaid(false)
            .build();
    entityManager.persist(submittedLoan);

    LoanApplication reviewLoan =
        LoanApplication.builder()
            .user(testUser)
            .product(testProduct)
            .amount(2000000.0)
            .tenureMonths(24)
            .interestRateApplied(12.0)
            .currentStatus("IN_REVIEW")
            .isPaid(false)
            .build();
    entityManager.persist(reviewLoan);

    entityManager.flush();

    // When
    List<LoanApplication> submittedLoans =
        loanApplicationRepository.findByCurrentStatus("SUBMITTED");

    // Then
    assertThat(submittedLoans).hasSize(1);
    assertThat(submittedLoans.get(0).getAmount()).isEqualTo(1000000.0);
  }

  @Test
  @DisplayName("Should find loan application by ID")
  void findById_shouldReturnLoan() {
    // Given
    LoanApplication loan =
        LoanApplication.builder()
            .user(testUser)
            .product(testProduct)
            .amount(5000000.0)
            .tenureMonths(12)
            .interestRateApplied(12.0)
            .currentStatus("SUBMITTED")
            .isPaid(false)
            .build();
    entityManager.persist(loan);
    entityManager.flush();

    // When
    Optional<LoanApplication> found =
        loanApplicationRepository.findById(loan.getLoanApplicationId());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getAmount()).isEqualTo(5000000.0);
  }

  @Test
  @DisplayName("Should return empty when loan not found")
  void findById_shouldReturnEmpty_whenNotFound() {
    // When
    Optional<LoanApplication> found = loanApplicationRepository.findById(999L);

    // Then
    assertThat(found).isEmpty();
  }
}
