package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
