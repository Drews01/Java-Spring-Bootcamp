package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.NotificationDTO;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
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
        return ResponseUtil.created(created, "Notification created successfully");
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotification(@PathVariable Long notificationId) {
        NotificationDTO notification = notificationService.getNotification(notificationId);
        return ResponseUtil.ok(notification, "Notification retrieved successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByUserId(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseUtil.ok(notifications, "Notifications retrieved successfully");
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotificationsByUserId(
            @PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByUserId(userId);
        return ResponseUtil.ok(notifications, "Unread notifications retrieved successfully");
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(@PathVariable Long userId) {
        Long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseUtil.ok(count, "Unread notification count retrieved successfully"); // ResponseUtil.ok(T data)
                                                                                           // uses default message if
                                                                                           // not provided, but here
                                                                                           // we provide message
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseUtil.ok(notifications, "Notifications retrieved successfully");
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(@PathVariable Long notificationId) {
        NotificationDTO updated = notificationService.markAsRead(notificationId);
        return ResponseUtil.ok(updated, "Notification marked as read");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseUtil.okMessage("Notification deleted successfully");
    }
}
