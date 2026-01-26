package com.example.demo.service.notification;

import java.util.Map;

/**
 * Interface for notification channels. Follows SOLID Interface Segregation Principle.
 *
 * <p>Each implementation handles a specific notification delivery method (IN_APP, EMAIL, PUSH).
 */
public interface NotificationChannel {

  /**
   * Send a notification to a user.
   *
   * @param userId Target user ID
   * @param title Notification title
   * @param body Notification body/message
   * @param data Optional additional data payload
   */
  void send(Long userId, String title, String body, Map<String, String> data);

  /**
   * Check if this channel supports the given channel type.
   *
   * @param channelType Channel type string (e.g., "PUSH", "IN_APP", "EMAIL")
   * @return true if this channel handles the specified type
   */
  boolean supports(String channelType);

  /**
   * Get the channel type identifier.
   *
   * @return Channel type string
   */
  String getChannelType();
}
