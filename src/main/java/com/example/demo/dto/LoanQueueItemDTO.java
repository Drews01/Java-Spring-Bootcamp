package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanQueueItemDTO {
  private Long loanApplicationId;
  private Long userId;
  private String username;
  private String userEmail;
  private Long productId;
  private String productName;
  private Double amount;
  private Integer tenureMonths;
  private Double interestRateApplied;
  private String currentStatus;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<String> allowedActions;

  // New fields from UserProfile
  private String userNik;
  private String userKtpPath;
  private String userPhoneNumber;
  private String userAddress;
  private String userAccountNumber;
  private String userBankName;

  private String marketingComment;
  private String branchManagerComment;

  // Branch information
  private Long branchId;
  private String branchName;
}
