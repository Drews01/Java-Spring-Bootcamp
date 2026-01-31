package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.UserDevice;
import com.example.demo.repository.UserDeviceRepository;
import com.example.demo.repository.UserRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Firebase Cloud Messaging (FCM) operations. Handles device token management and push
 * notification delivery.
 */
@Service
@Slf4j
public class FCMService {

  private final FirebaseMessaging firebaseMessaging;
  private final UserDeviceRepository userDeviceRepository;
  private final UserRepository userRepository;

  @Autowired
  public FCMService(
      @Autowired(required = false) FirebaseMessaging firebaseMessaging,
      UserDeviceRepository userDeviceRepository,
      UserRepository userRepository) {
    this.firebaseMessaging = firebaseMessaging;
    this.userDeviceRepository = userDeviceRepository;
    this.userRepository = userRepository;
  }

  /**
   * Save or update FCM device token for a user.
   *
   * @param user The user to associate the token with
   * @param fcmToken The FCM device token
   * @param deviceName Optional device name
   * @param platform Platform type (ANDROID, IOS, WEB)
   */
  @Transactional
  public void saveDeviceToken(User user, String fcmToken, String deviceName, String platform) {
    log.info("Attempting to save device token for user: {}", user.getUsername());
    if (fcmToken == null || fcmToken.isBlank()) {
      log.debug("FCM token is empty, skipping registration");
      return;
    }

    // Check if token already exists
    Optional<UserDevice> existingDevice = userDeviceRepository.findByFcmToken(fcmToken);

    if (existingDevice.isPresent()) {
      UserDevice device = existingDevice.get();
      // Update existing device
      device.setUser(user);
      device.setDeviceName(deviceName);
      device.setPlatform(platform);
      device.setLastUsedAt(LocalDateTime.now());
      device.setIsActive(true);
      userDeviceRepository.save(device);
      log.info("Updated FCM token for user: {}", user.getUsername());
    } else {
      // Create new device entry
      UserDevice newDevice =
          UserDevice.builder()
              .user(user)
              .fcmToken(fcmToken)
              .deviceName(deviceName)
              .platform(platform)
              .isActive(true)
              .build();
      userDeviceRepository.save(newDevice);
      log.info("Registered new FCM token for user: {}", user.getUsername());
    }
  }

  /**
   * Remove FCM device token (on logout/uninstall).
   *
   * @param fcmToken The FCM token to remove
   */
  @Transactional
  public void removeDeviceToken(String fcmToken) {
    if (fcmToken == null || fcmToken.isBlank()) {
      return;
    }
    userDeviceRepository.deactivateByFcmToken(fcmToken);
    log.info(
        "Deactivated FCM token: {}...", fcmToken.substring(0, Math.min(20, fcmToken.length())));
  }

  /**
   * Send push notification to a specific user (all their devices).
   *
   * @param userId Target user ID
   * @param title Notification title
   * @param body Notification body
   * @param data Optional data payload
   * @return Number of successfully sent notifications
   */
  @Transactional
  public int sendPushNotification(
      Long userId, String title, String body, Map<String, String> data) {
    List<String> tokens = userDeviceRepository.findFcmTokensByUserId(userId);

    if (tokens.isEmpty()) {
      log.debug("No FCM tokens found for user: {}", userId);
      return 0;
    }

    return sendToMultipleTokens(tokens, title, body, data);
  }

  /**
   * Send push notification to a specific FCM token.
   *
   * @param token FCM device token
   * @param title Notification title
   * @param body Notification body
   * @param data Optional data payload
   * @return true if successful
   */
  @Transactional
  public boolean sendPushNotificationToToken(
      String token, String title, String body, Map<String, String> data) {
    try {
      Message.Builder messageBuilder =
          Message.builder()
              .setToken(token)
              .setNotification(Notification.builder().setTitle(title).setBody(body).build());

      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      String response = firebaseMessaging.send(messageBuilder.build());
      log.info("Push notification sent successfully. Response: {}", response);
      return true;
    } catch (FirebaseMessagingException e) {
      log.error("Failed to send push notification to token: {}", e.getMessage());
      handleInvalidToken(token, e);
      return false;
    }
  }

  /**
   * Send push notification to multiple users.
   *
   * @param userIds List of user IDs
   * @param title Notification title
   * @param body Notification body
   * @param data Optional data payload
   * @return Number of successfully sent notifications
   */
  @Transactional
  public int sendPushNotificationToUsers(
      List<Long> userIds, String title, String body, Map<String, String> data) {
    List<String> tokens = userDeviceRepository.findFcmTokensByUserIds(userIds);

    if (tokens.isEmpty()) {
      log.debug("No FCM tokens found for users: {}", userIds);
      return 0;
    }

    return sendToMultipleTokens(tokens, title, body, data);
  }

  /**
   * Send push notification to multiple FCM tokens.
   *
   * @param tokens List of FCM tokens
   * @param title Notification title
   * @param body Notification body
   * @param data Optional data payload
   * @return Number of successfully sent notifications
   */
  private int sendToMultipleTokens(
      List<String> tokens, String title, String body, Map<String, String> data) {
    if (tokens.isEmpty()) {
      return 0;
    }

    try {
      MulticastMessage.Builder messageBuilder =
          MulticastMessage.builder()
              .addAllTokens(tokens)
              .setNotification(Notification.builder().setTitle(title).setBody(body).build());

      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      BatchResponse response = firebaseMessaging.sendEachForMulticast(messageBuilder.build());
      int successCount = response.getSuccessCount();
      int failureCount = response.getFailureCount();

      log.info("Push notifications sent. Success: {}, Failure: {}", successCount, failureCount);

      // Handle failed tokens
      if (failureCount > 0) {
        handleFailedTokens(tokens, response);
      }

      return successCount;
    } catch (FirebaseMessagingException e) {
      log.error("Failed to send multicast push notification: {}", e.getMessage());
      return 0;
    }
  }

  /** Handle failed tokens by deactivating invalid ones. */
  private void handleFailedTokens(List<String> tokens, BatchResponse response) {
    for (int i = 0; i < response.getResponses().size(); i++) {
      if (!response.getResponses().get(i).isSuccessful()) {
        FirebaseMessagingException exception = response.getResponses().get(i).getException();
        if (exception != null && isInvalidTokenError(exception)) {
          String token = tokens.get(i);
          log.warn(
              "Deactivating invalid FCM token: {}...",
              token.substring(0, Math.min(20, token.length())));
          userDeviceRepository.deactivateByFcmToken(token);
        }
      }
    }
  }

  /** Handle invalid token by deactivating it. */
  private void handleInvalidToken(String token, FirebaseMessagingException e) {
    if (isInvalidTokenError(e)) {
      log.warn("Deactivating invalid FCM token");
      userDeviceRepository.deactivateByFcmToken(token);
    }
  }

  /** Check if the error indicates an invalid token. */
  private boolean isInvalidTokenError(FirebaseMessagingException e) {
    String errorCode = e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : "";
    return errorCode.equals("UNREGISTERED") || errorCode.equals("INVALID_ARGUMENT");
  }
}
