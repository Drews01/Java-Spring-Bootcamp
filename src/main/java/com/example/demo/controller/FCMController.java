package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.dto.PushNotificationRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.FCMService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for FCM (Firebase Cloud Messaging) push notification operations. */
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Slf4j
public class FCMController {

  private final FCMService fcmService;
  private final UserRepository userRepository;

  /** Register device for push notifications. */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Void>> registerDevice(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam String fcmToken,
      @RequestParam(required = false) String deviceName,
      @RequestParam(required = false) String platform) {

    fcmService.saveDeviceToken(userDetails.getUser(), fcmToken, deviceName, platform);

    return ResponseEntity.ok(
        ApiResponse.<Void>success(null, "Device registered for push notifications"));
  }

  /** Unregister device from push notifications. */
  @DeleteMapping("/unregister")
  public ResponseEntity<ApiResponse<Void>> unregisterDevice(@RequestParam String fcmToken) {
    fcmService.removeDeviceToken(fcmToken);

    return ResponseEntity.ok(
        ApiResponse.<Void>success(null, "Device unregistered from push notifications"));
  }

  /** Send push notification to a user (ADMIN only). */
  @PostMapping("/send")
  public ResponseEntity<ApiResponse<Integer>> sendNotification(
      @Valid @RequestBody PushNotificationRequest request) {

    int sentCount =
        fcmService.sendPushNotification(
            request.getUserId(), request.getTitle(), request.getBody(), request.getData());

    return ResponseEntity.ok(
        ApiResponse.success(sentCount, "Push notification sent to " + sentCount + " device(s)"));
  }

  /** Test push notification (sends to current user). */
  @PostMapping("/test")
  public ResponseEntity<ApiResponse<Integer>> testNotification(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "Test Notification") String title,
      @RequestParam(defaultValue = "This is a test push notification") String body) {

    Long userId = userDetails.getUser().getId();
    Map<String, String> data =
        Map.of("type", "TEST", "timestamp", String.valueOf(System.currentTimeMillis()));

    int sentCount = fcmService.sendPushNotification(userId, title, body, data);

    if (sentCount > 0) {
      return ResponseEntity.ok(
          ApiResponse.success(sentCount, "Test notification sent to " + sentCount + " device(s)"));
    } else {
      return ResponseEntity.ok(
          ApiResponse.success(
              0,
              "No devices registered for push notifications. Please login with fcmToken to register."));
    }
  }
}
