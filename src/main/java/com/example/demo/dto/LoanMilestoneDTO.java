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
public class LoanMilestoneDTO {

  private String name;
  private String status; // COMPLETED, CURRENT, PENDING
  private LocalDateTime timestamp;
  private Integer order;
}
