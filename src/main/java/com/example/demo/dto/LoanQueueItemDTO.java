package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
}
