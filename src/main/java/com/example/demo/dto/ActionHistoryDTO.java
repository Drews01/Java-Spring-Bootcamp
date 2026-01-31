package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionHistoryDTO {
  private Long loanApplicationId;
  private String action; // e.g., "APPROVE", "REJECT", "COMMENT", "DISBURSE"
  private String actionDisplayName; // e.g., "Approved", "Rejected"
  private LocalDateTime actionDate;

  // Loan Info
  private String productName;
  private BigDecimal amount;
  private Integer tenureMonths;
  private Double latitude;
  private Double longitude;

  // Applicant Info
  private Long userId;
  private String username;
  private String userEmail;
  private String userNik;
  private String userPhoneNumber;
  private String userAddress;
  private String userAccountNumber;
  private String userBankName;
  private String userKtpPath;

  // Actor Info (who performed the action)
  private Long actorId;
  private String actorUsername;

  // Status at time of action
  private String previousStatus;
  private String resultingStatus;

  // Comment if any
  private String comment;
}
