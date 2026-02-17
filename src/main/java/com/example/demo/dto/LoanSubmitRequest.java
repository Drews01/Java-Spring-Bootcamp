package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

  @NotNull(message = "amount is required") @Positive(message = "amount must be positive") private Double amount;

  @NotNull(message = "tenureMonths is required") @Min(value = 1, message = "tenureMonths must be at least 1")
  private Integer tenureMonths;

  @Positive(message = "interestRateApplied must be positive") private Double interestRateApplied; // Optional - uses product's rate if not specified

  @NotNull(message = "branchId is required") private Long branchId; // Required - the branch this loan application belongs to

  private Double latitude;
  private Double longitude;
}
