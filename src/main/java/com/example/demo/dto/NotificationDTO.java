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
public class NotificationDTO {
  private Long notificationId;
  private Long userId;
  private Long relatedLoanApplicationId;
  private String notifType;
  private String channel;
  private String message;
  private Boolean isRead;
  private LocalDateTime createdAt;
}
