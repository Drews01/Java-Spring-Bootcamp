package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanActionRequest {
  @NotNull(message = "loanApplicationId is required") private Long loanApplicationId;

  @NotBlank(message = "action is required")
  @Size(max = 50, message = "action must not exceed 50 characters")
  private String action;

  @Size(max = 500, message = "comment must not exceed 500 characters")
  private String comment;
}
