package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for sending push notification requests. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

  @NotNull(message = "userId is required") private Long userId;

  @NotBlank(message = "title is required")
  private String title;

  @NotBlank(message = "body is required")
  private String body;

  /** Optional data payload for the notification. */
  private Map<String, String> data;
}
