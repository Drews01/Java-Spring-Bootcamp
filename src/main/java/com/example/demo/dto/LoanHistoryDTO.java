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
public class LoanHistoryDTO {
  private Long loanHistoryId;
  private Long loanApplicationId;
  private Long actorUserId;
  private String action;
  private String comment;
  private String fromStatus;
  private String toStatus;
  private LocalDateTime createdAt;
}
