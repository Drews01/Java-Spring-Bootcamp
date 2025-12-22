package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.LoanApplicationRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @Transactional
    public NotificationDTO createNotification(NotificationDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Notification.NotificationBuilder builder = Notification.builder()
                .user(user)
                .notifType(dto.getNotifType())
                .channel(dto.getChannel())
                .message(dto.getMessage())
                .isRead(dto.getIsRead() != null ? dto.getIsRead() : false);

        if (dto.getRelatedLoanApplicationId() != null) {
            LoanApplication loanApplication = loanApplicationRepository.findById(dto.getRelatedLoanApplicationId())
                    .orElseThrow(() -> new RuntimeException(
                            "LoanApplication not found with id: " + dto.getRelatedLoanApplicationId()));
            builder.relatedLoanApplication(loanApplication);
        }

        Notification notification = builder.build();
        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        return convertToDTO(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findByUser_IdAndIsReadOrderByCreatedAtDesc(userId, false).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUser_IdAndIsRead(userId, false);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getId())
                .relatedLoanApplicationId(notification.getRelatedLoanApplication() != null
                        ? notification.getRelatedLoanApplication().getLoanApplicationId()
                        : null)
                .notifType(notification.getNotifType())
                .channel(notification.getChannel())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
