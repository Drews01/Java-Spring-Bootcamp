package com.example.demo.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDTO {
  private Long loanApplicationId;
  private Long userId;
  private Long productId;
  private Double amount;
  private Integer tenureMonths;
  private Double interestRateApplied;
  private Double totalAmountToPay; // Total repayment amount (principal + interest)
  private String currentStatus;
  private String displayStatus;
  private String productName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
