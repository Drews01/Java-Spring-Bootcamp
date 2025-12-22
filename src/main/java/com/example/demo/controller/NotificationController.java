package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.dto.NotificationDTO;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDTO>> createNotification(@RequestBody NotificationDTO dto) {
        NotificationDTO created = notificationService.createNotification(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Notification created successfully"));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotification(@PathVariable Long notificationId) {
        NotificationDTO notification = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByUserId(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotificationsByUserId(
            @PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Unread notifications retrieved successfully"));
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(@PathVariable Long userId) {
        Long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread notification count retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(@PathVariable Long notificationId) {
        NotificationDTO updated = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification marked as read"));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }
}
