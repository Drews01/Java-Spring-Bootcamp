package com.example.demo.service;

import com.example.demo.dto.UserProductDTO;
import com.example.demo.dto.UserTierLimitDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProduct;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserProductRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProductService {

  private final UserProductRepository userProductRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final LoanEligibilityService loanEligibilityService;

  @Transactional
  public UserProductDTO createUserProduct(UserProductDTO dto) {
    User user =
        userRepository
            .findById(dto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

    Product product =
        productRepository
            .findById(dto.getProductId())
            .orElseThrow(
                () -> new RuntimeException("Product not found with id: " + dto.getProductId()));

    // Check if already exists
    userProductRepository
        .findByUser_IdAndProduct_Id(dto.getUserId(), dto.getProductId())
        .ifPresent(
            up -> {
              throw new RuntimeException(
                  "UserProduct already exists for user "
                      + dto.getUserId()
                      + " and product "
                      + dto.getProductId());
            });

    UserProduct userProduct =
        UserProduct.builder()
            .user(user)
            .product(product)
            .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
            .build();

    UserProduct saved = userProductRepository.save(userProduct);
    return convertToDTO(saved);
  }

  @Transactional(readOnly = true)
  public UserProductDTO getUserProduct(Long userProductId) {
    UserProduct userProduct =
        userProductRepository
            .findById(userProductId)
            .orElseThrow(
                () -> new RuntimeException("UserProduct not found with id: " + userProductId));
    return convertToDTO(userProduct);
  }

  @Transactional(readOnly = true)
  public List<UserProductDTO> getUserProductsByUserId(Long userId) {
    return userProductRepository.findByUser_Id(userId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UserProductDTO> getActiveUserProductsByUserId(Long userId) {
    return userProductRepository.findByUser_IdAndStatus(userId, "ACTIVE").stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UserProductDTO> getAllUserProducts() {
    return userProductRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public UserProductDTO updateUserProduct(Long userProductId, UserProductDTO dto) {
    UserProduct userProduct =
        userProductRepository
            .findById(userProductId)
            .orElseThrow(
                () -> new RuntimeException("UserProduct not found with id: " + userProductId));

    userProduct.setStatus(dto.getStatus());

    UserProduct updated = userProductRepository.save(userProduct);
    return convertToDTO(updated);
  }

  @Transactional
  public void deleteUserProduct(Long userProductId) {
    userProductRepository.deleteById(userProductId);
  }

  private UserProductDTO convertToDTO(UserProduct userProduct) {
    return UserProductDTO.builder()
        .userProductId(userProduct.getUserProductId())
        .userId(userProduct.getUser().getId())
        .productId(userProduct.getProduct().getId())
        .status(userProduct.getStatus())
        .createdAt(userProduct.getCreatedAt())
        .build();
  }

  /**
   * Get the current user's highest active tier and credit limit information. Returns null if the
   * user has no active product subscription.
   *
   * @param userId the user ID
   * @return UserTierLimitDTO with tier and limit info, or null if no active product
   */
  @Transactional
  public UserTierLimitDTO getCurrentUserTierAndLimits(Long userId) {
    // Self-healing: Recalculate used amount
    loanEligibilityService.recalculateUsedAmount(userId);

    // Find user's active products ordered by tier (highest first)
    List<UserProduct> activeProducts =
        userProductRepository.findActiveUserProductsByUserIdOrderByTier(userId);

    if (activeProducts.isEmpty()) {
      return null; // No active product subscription
    }

    // Get the highest tier product (first in list since ordered DESC by tierOrder)
    UserProduct userProduct = activeProducts.get(0);
    Product product = userProduct.getProduct();

    // Calculate derived fields
    Double currentUsed =
        userProduct.getCurrentUsedAmount() != null ? userProduct.getCurrentUsedAmount() : 0.0;
    Double creditLimit =
        product.getCreditLimit() != null ? product.getCreditLimit() : product.getMaxAmount();
    Double availableCredit = creditLimit - currentUsed;

    Double totalPaid =
        userProduct.getTotalPaidAmount() != null ? userProduct.getTotalPaidAmount() : 0.0;
    Double upgradeThreshold = product.getUpgradeThreshold();
    Double remainingToUpgrade = null;

    if (upgradeThreshold != null) {
      remainingToUpgrade = Math.max(0, upgradeThreshold - totalPaid);
    }

    return UserTierLimitDTO.builder()
        .tierName(product.getName())
        .tierCode(product.getCode())
        .tierOrder(product.getTierOrder())
        .creditLimit(creditLimit)
        .currentUsedAmount(currentUsed)
        .availableCredit(availableCredit)
        .totalPaidAmount(totalPaid)
        .upgradeThreshold(upgradeThreshold)
        .remainingToUpgrade(remainingToUpgrade)
        .interestRate(product.getInterestRate())
        .status(userProduct.getStatus())
        .build();
  }
}
