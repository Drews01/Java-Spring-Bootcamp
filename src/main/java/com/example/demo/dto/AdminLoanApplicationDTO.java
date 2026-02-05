package com.example.demo.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin view of loan applications. Includes comprehensive information about the loan, user,
 * profile, status/bucket, and branch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoanApplicationDTO {

  // Loan Application Info
  private Long loanApplicationId;
  private Double amount;
  private Integer tenureMonths;
  private Double interestRateApplied;
  private Double totalAmountToPay;
  private String productName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // User Info
  private Long userId;
  private String userName;
  private String userEmail;

  // Profile Info (from UserProfile)
  private Long profileId; // Same as userId since UserProfile uses @MapsId
  private String nik;
  private String phoneNumber;
  private String bankName;
  private String accountNumber;

  // Status Info
  private String currentStatus; // Raw status (e.g., "SUBMITTED", "IN_REVIEW")
  private String displayStatus; // Human-readable (e.g., "Submitted", "In Review")
  private String currentBucket; // "MARKETING", "BRANCH_MANAGER", "BACK_OFFICE", "COMPLETED"

  // Branch Info
  private Long branchId;
  private String branchName;

  // Location Info
  private Double latitude;
  private Double longitude;
}
