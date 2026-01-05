package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSubmitRequest {
  private Long userId;
  private Long productId;
  private Double amount;
  private Integer tenureMonths;
  private Double interestRateApplied;
}
