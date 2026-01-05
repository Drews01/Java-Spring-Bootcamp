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
public class UserProductDTO {
  private Long userProductId;
  private Long userId;
  private Long productId;
  private String status;
  private LocalDateTime createdAt;
}
