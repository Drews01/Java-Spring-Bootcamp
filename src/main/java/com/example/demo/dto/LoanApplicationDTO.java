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
  private String currentStatus;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
