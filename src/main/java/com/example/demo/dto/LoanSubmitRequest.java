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
  // userId removed - MUST be extracted from JWT token to prevent IDOR
  private Long productId; // Optional - uses tier product if not specified
  private Double amount;
  private Integer tenureMonths;
  private Double interestRateApplied; // Optional - uses product's rate if not specified
  private Long branchId; // Required - the branch this loan application belongs to
  private Double latitude;
  private Double longitude;
}
