package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the current user's product tier and credit limit information. Used by the
 * /api/user-products/my-tier endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTierLimitDTO {

  /** Product tier name (e.g., "Bronze Loan Product", "Silver Loan Product", "Gold Loan Product") */
  private String tierName;

  /** Product tier code (e.g., "TIER-BRONZE", "TIER-SILVER", "TIER-GOLD") */
  private String tierCode;

  /** Tier order for comparison (1=Bronze, 2=Silver, 3=Gold) */
  private Integer tierOrder;

  /** Maximum credit limit for this tier */
  private Double creditLimit;

  /** Current outstanding loan amount being used */
  private Double currentUsedAmount;

  /** Remaining available credit (creditLimit - currentUsedAmount) */
  private Double availableCredit;

  /** Cumulative total amount paid across all loans (for tier upgrade tracking) */
  private Double totalPaidAmount;

  /** Total paid amount required to upgrade from this tier (null for highest tier) */
  private Double upgradeThreshold;

  /** Remaining amount to pay before eligible for upgrade (null if already at highest tier) */
  private Double remainingToUpgrade;

  /** Interest rate for this tier product */
  private Double interestRate;

  /** User's subscription status to this product (ACTIVE/INACTIVE) */
  private String status;
}
