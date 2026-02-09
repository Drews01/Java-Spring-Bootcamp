package com.example.demo.service;

import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProduct;
import com.example.demo.enums.LoanStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserProductRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanEligibilityService {

  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final UserProductRepository userProductRepository;
  private final LoanApplicationRepository loanApplicationRepository;

  /**
   * Check if user can apply for a loan with the specified amount.
   *
   * @param userId the user ID
   * @param amount the requested loan amount
   * @return true if user can apply, false if limit would be exceeded
   */
  @Transactional(readOnly = true)
  public boolean canApplyForLoan(Long userId, Double amount) {
    Double remainingLimit = getRemainingCreditLimit(userId);
    return amount <= remainingLimit;
  }

  /**
   * Get user's remaining credit limit based on their current tier product.
   *
   * @param userId the user ID
   * @return remaining credit limit
   */
  @Transactional(readOnly = true)
  public Double getRemainingCreditLimit(Long userId) {
    Product currentProduct = getCurrentTierProduct(userId);
    if (currentProduct == null || currentProduct.getCreditLimit() == null) {
      return 0.0;
    }

    Double totalUsed = loanApplicationRepository.findTotalActiveLoanAmount(userId);
    if (totalUsed == null) {
      totalUsed = 0.0;
    }

    return Math.max(0, currentProduct.getCreditLimit() - totalUsed);
  }

  /**
   * Get user's current tier product. If user has no assigned product, assign Bronze.
   *
   * @param userId the user ID
   * @return the user's current tier product
   */
  @Transactional
  public Product getCurrentTierProduct(Long userId) {
    List<UserProduct> userProducts =
        userProductRepository.findActiveUserProductsByUserIdOrderByTier(userId);

    if (userProducts.isEmpty()) {
      // Auto-assign Bronze product
      UserProduct assigned = assignDefaultProduct(userId);
      return assigned != null ? assigned.getProduct() : null;
    }

    // Return highest tier product (list is ordered DESC by tierOrder)
    return userProducts.get(0).getProduct();
  }

  /**
   * Assign the default Bronze product to a new user.
   *
   * @param userId the user ID
   * @return the created UserProduct
   */
  @Transactional
  public UserProduct assignDefaultProduct(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    // Get Bronze product (tier order 1)
    Optional<Product> bronzeProduct = productRepository.findByTierOrderAndDeletedFalse(1);
    if (bronzeProduct.isEmpty()) {
      log.warn("Bronze product not found, cannot assign default product to user {}", userId);
      return null;
    }

    // Check if user already has this product
    Optional<UserProduct> existing =
        userProductRepository.findByUser_IdAndProduct_Id(userId, bronzeProduct.get().getId());
    if (existing.isPresent()) {
      return existing.get();
    }

    UserProduct userProduct =
        UserProduct.builder()
            .user(user)
            .product(bronzeProduct.get())
            .status("ACTIVE")
            .currentUsedAmount(0.0)
            .totalPaidAmount(0.0)
            .build();

    UserProduct saved = userProductRepository.save(userProduct);
    log.info("Assigned Bronze product to user {}", userId);
    return saved;
  }

  /**
   * Process a loan payment - update loan as paid and reset available limit.
   *
   * @param loanApplicationId the loan application ID
   */
  @Transactional
  public void processLoanPayment(Long loanApplicationId) {
    LoanApplication loan =
        loanApplicationRepository
            .findById(loanApplicationId)
            .orElseThrow(
                () -> new RuntimeException("Loan not found with id: " + loanApplicationId));

    if (!LoanStatus.DISBURSED.name().equals(loan.getCurrentStatus())) {
      throw new IllegalStateException("Only DISBURSED loans can be marked as paid");
    }

    if (Boolean.TRUE.equals(loan.getIsPaid())) {
      throw new IllegalStateException("Loan is already paid");
    }

    // Mark loan as paid
    loan.setIsPaid(true);
    loan.setPaidAt(LocalDateTime.now());
    loan.setCurrentStatus(LoanStatus.PAID.name());
    loanApplicationRepository.save(loan);

    // Update user's totalPaidAmount
    Long userId = loan.getUser().getId();
    List<UserProduct> userProducts =
        userProductRepository.findActiveUserProductsByUserIdOrderByTier(userId);
    if (!userProducts.isEmpty()) {
      UserProduct userProduct = userProducts.get(0);
      Double currentPaid = userProduct.getTotalPaidAmount();
      if (currentPaid == null) currentPaid = 0.0;
      userProduct.setTotalPaidAmount(currentPaid + loan.getAmount());

      // Recalculate current used amount
      Double unpaid = loanApplicationRepository.findTotalUnpaidAmountByUserId(userId);
      userProduct.setCurrentUsedAmount(unpaid != null ? unpaid : 0.0);

      userProductRepository.save(userProduct);
    }

    log.info("Loan {} marked as paid for user {}", loanApplicationId, userId);

    // Check for tier upgrade
    checkAndUpgradeTier(userId);
  }

  /**
   * Check if user is eligible for tier upgrade based on total paid amount.
   *
   * @param userId the user ID
   */
  @Transactional
  public void checkAndUpgradeTier(Long userId) {
    List<UserProduct> userProducts =
        userProductRepository.findActiveUserProductsByUserIdOrderByTier(userId);
    if (userProducts.isEmpty()) {
      return;
    }

    UserProduct currentUserProduct = userProducts.get(0);
    Product currentProduct = currentUserProduct.getProduct();

    if (currentProduct.getUpgradeThreshold() == null) {
      // No upgrade available (e.g., already at Gold)
      return;
    }

    // Use the value already stored in user_products table (single source of truth)
    Double totalPaid = currentUserProduct.getTotalPaidAmount();
    if (totalPaid == null) totalPaid = 0.0;

    if (totalPaid >= currentProduct.getUpgradeThreshold()) {
      // Find next tier product
      Integer nextTier = currentProduct.getTierOrder() + 1;
      Optional<Product> nextProduct = productRepository.findByTierOrderAndDeletedFalse(nextTier);

      if (nextProduct.isPresent()) {
        User user =
            userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new UserProduct for upgraded tier
        UserProduct upgradedProduct =
            UserProduct.builder()
                .user(user)
                .product(nextProduct.get())
                .status("ACTIVE")
                .currentUsedAmount(currentUserProduct.getCurrentUsedAmount())
                .totalPaidAmount(currentUserProduct.getTotalPaidAmount())
                .build();

        userProductRepository.save(upgradedProduct);

        // Deactivate old tier
        currentUserProduct.setStatus("UPGRADED");
        userProductRepository.save(currentUserProduct);

        log.info(
            "User {} upgraded from {} to {}",
            userId,
            currentProduct.getName(),
            nextProduct.get().getName());
      }
    }
  }

  /**
   * Recalculate and update user's used amount based on all active (non-rejected/non-paid) loans.
   * This is used to ensure the credit limit is correctly restored after a loan rejection.
   *
   * @param userId the user ID
   */
  @Transactional
  public void recalculateUsedAmount(Long userId) {
    Double totalActive = loanApplicationRepository.findTotalActiveLoanAmount(userId);

    List<UserProduct> userProducts =
        userProductRepository.findActiveUserProductsByUserIdOrderByTier(userId);
    if (!userProducts.isEmpty()) {
      UserProduct userProduct = userProducts.get(0);
      userProduct.setCurrentUsedAmount(totalActive);
      userProductRepository.save(userProduct);
      log.info("Recalculated used amount for user {}: {}", userId, totalActive);
    }
  }

  /**
   * Update user's used amount after a loan is submitted/approved.
   *
   * @param userId the user ID
   * @param amount the loan amount to add to used limit
   */
  @Transactional
  public void updateUsedAmount(Long userId, Double amount) {
    List<UserProduct> userProducts =
        userProductRepository.findActiveUserProductsByUserIdOrderByTier(userId);
    if (!userProducts.isEmpty()) {
      UserProduct userProduct = userProducts.get(0);
      Double currentUsed = userProduct.getCurrentUsedAmount();
      if (currentUsed == null) currentUsed = 0.0;
      userProduct.setCurrentUsedAmount(currentUsed + amount);
      userProductRepository.save(userProduct);
    }
  }

  /**
   * Get loan eligibility details for a user.
   *
   * @param userId the user ID
   * @return eligibility details as a simple object
   */
  @Transactional(readOnly = true)
  public LoanEligibilityDetails getEligibilityDetails(Long userId) {
    Product tierProduct = getCurrentTierProduct(userId);
    Double remaining = getRemainingCreditLimit(userId);
    Double totalPaid = loanApplicationRepository.findTotalPaidAmountByUserId(userId);
    Double totalUnpaid = loanApplicationRepository.findTotalUnpaidAmountByUserId(userId);

    return LoanEligibilityDetails.builder()
        .userId(userId)
        .currentTier(tierProduct != null ? tierProduct.getName() : "None")
        .tierOrder(tierProduct != null ? tierProduct.getTierOrder() : 0)
        .creditLimit(tierProduct != null ? tierProduct.getCreditLimit() : 0.0)
        .remainingLimit(remaining)
        .totalPaidLoans(totalPaid != null ? totalPaid : 0.0)
        .totalUnpaidLoans(totalUnpaid != null ? totalUnpaid : 0.0)
        .upgradeThreshold(tierProduct != null ? tierProduct.getUpgradeThreshold() : null)
        .build();
  }

  @lombok.Builder
  @lombok.Data
  public static class LoanEligibilityDetails {
    private Long userId;
    private String currentTier;
    private Integer tierOrder;
    private Double creditLimit;
    private Double remainingLimit;
    private Double totalPaidLoans;
    private Double totalUnpaidLoans;
    private Double upgradeThreshold;
  }
}
