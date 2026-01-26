package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Authentication request DTO for login. FCM fields are optional - if provided, the device token
 * will be registered for push notifications.
 */
public record AuthRequest(
    @NotBlank(message = "usernameOrEmail is required") String usernameOrEmail,
    @NotBlank(message = "password is required") String password,
    String fcmToken, // Optional: FCM device token for push notifications
    String deviceName, // Optional: Human-readable device name
    String platform // Optional: ANDROID, IOS, or WEB
    ) {}
