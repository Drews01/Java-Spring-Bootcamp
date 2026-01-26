package com.example.demo.service.notification;

import com.example.demo.service.FCMService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Push notification channel implementation using Firebase Cloud Messaging. Follows SOLID
 * Open/Closed Principle - can add new channels without modifying existing ones.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationChannel implements NotificationChannel {

  private static final String CHANNEL_TYPE = "PUSH";

  private final FCMService fcmService;

  @Override
  public void send(Long userId, String title, String body, Map<String, String> data) {
    try {
      int sentCount = fcmService.sendPushNotification(userId, title, body, data);
      if (sentCount > 0) {
        log.debug("Push notification sent to user {} ({} devices)", userId, sentCount);
      } else {
        log.debug("No devices registered for push notification to user {}", userId);
      }
    } catch (Exception e) {
      log.error("Failed to send push notification to user {}: {}", userId, e.getMessage());
    }
  }

  @Override
  public boolean supports(String channelType) {
    return CHANNEL_TYPE.equalsIgnoreCase(channelType);
  }

  @Override
  public String getChannelType() {
    return CHANNEL_TYPE;
  }
}
