package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.LoanHistory;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for {@link LoanHistoryRepository}.
 *
 * <p>Tests repository methods for loan history tracking and audit trail.
 */
@DataJpaTest
@ActiveProfiles("test")
class LoanHistoryRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private LoanHistoryRepository loanHistoryRepository;

  private User testUser;
  private Product testProduct;
  private LoanApplication testLoan;

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

    // Create test loan
    testLoan =
        LoanApplication.builder()
            .user(testUser)
            .product(testProduct)
            .amount(5000000.0)
            .tenureMonths(12)
            .interestRateApplied(12.0)
            .currentStatus("SUBMITTED")
            .isPaid(false)
            .build();
    entityManager.persist(testLoan);

    entityManager.flush();
  }

  @Test
  @DisplayName("Should find loan history by loan application ID ordered by created at desc")
  void findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc_shouldReturnHistory() {
    // Given
    LoanHistory history1 =
        LoanHistory.builder()
            .loanApplication(testLoan)
            .actorUser(testUser)
            .action("SUBMIT")
            .fromStatus(null)
            .toStatus("SUBMITTED")
            .build();
    entityManager.persist(history1);

    LoanHistory history2 =
        LoanHistory.builder()
            .loanApplication(testLoan)
            .actorUser(testUser)
            .action("COMMENT")
            .fromStatus("SUBMITTED")
            .toStatus("IN_REVIEW")
            .comment("Initial review started")
            .build();
    entityManager.persist(history2);

    entityManager.flush();

    // When
    List<LoanHistory> histories =
        loanHistoryRepository.findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(
            testLoan.getLoanApplicationId());

    // Then
    assertThat(histories).hasSize(2);
    assertThat(histories).extracting(LoanHistory::getAction).containsExactly("COMMENT", "SUBMIT");
  }

  @Test
  @DisplayName("Should return empty list when no history exists for loan")
  void findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc_shouldReturnEmpty() {
    // When
    List<LoanHistory> histories =
        loanHistoryRepository.findByLoanApplication_LoanApplicationIdOrderByCreatedAtDesc(999L);

    // Then
    assertThat(histories).isEmpty();
  }

  @Test
  @DisplayName("Should save loan history entry")
  void save_shouldPersistHistory() {
    // Given
    LoanHistory history =
        LoanHistory.builder()
            .loanApplication(testLoan)
            .actorUser(testUser)
            .action("APPROVE")
            .fromStatus("WAITING_APPROVAL")
            .toStatus("APPROVED_WAITING_DISBURSEMENT")
            .comment("Loan approved by branch manager")
            .build();

    // When
    LoanHistory saved = loanHistoryRepository.save(history);

    // Then
    assertThat(saved.getLoanHistoryId()).isNotNull();
    assertThat(saved.getAction()).isEqualTo("APPROVE");
    assertThat(saved.getComment()).isEqualTo("Loan approved by branch manager");
  }

  @Test
  @DisplayName("Should find history entries by actor user")
  void findByActorUser_Id_shouldReturnHistory() {
    // Given
    LoanHistory history =
        LoanHistory.builder()
            .loanApplication(testLoan)
            .actorUser(testUser)
            .action("SUBMIT")
            .fromStatus(null)
            .toStatus("SUBMITTED")
            .build();
    entityManager.persist(history);
    entityManager.flush();

    // When
    List<LoanHistory> histories = loanHistoryRepository.findByActorUser_Id(testUser.getId());

    // Then
    assertThat(histories).hasSize(1);
    assertThat(histories.get(0).getActorUser().getUsername()).isEqualTo("testuser");
  }
}
