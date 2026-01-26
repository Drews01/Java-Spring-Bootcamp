package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.entity.LoanApplication;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.notification.NotificationChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service dedicated to loan-related notifications. Follows SOLID Single Responsibility Principle -
 * handles only loan notification logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanNotificationService {

  private final NotificationService notificationService;
  private final UserRepository userRepository;
  private final List<NotificationChannel> notificationChannels;
  private final EmailService emailService;

  // Indonesian status messages mapping
  private static final Map<String, StatusMessage> STATUS_MESSAGES = new HashMap<>();

  static {
    STATUS_MESSAGES.put(
        "SUBMITTED_TO_IN_REVIEW",
        new StatusMessage(
            "Sedang Di-review Marketing",
            "Pengajuan pinjaman Anda sedang di-review oleh tim Marketing"));
    STATUS_MESSAGES.put(
        "IN_REVIEW_TO_WAITING_APPROVAL",
        new StatusMessage(
            "Sedang Di-review Manager", "Pinjaman Anda sedang ditinjau oleh Branch Manager"));
    STATUS_MESSAGES.put(
        "WAITING_APPROVAL_TO_APPROVED_WAITING_DISBURSEMENT",
        new StatusMessage(
            "Pinjaman Disetujui", "Selamat! Pinjaman Anda telah disetujui, menunggu pencairan"));
    STATUS_MESSAGES.put(
        "WAITING_APPROVAL_TO_REJECTED",
        new StatusMessage("Pinjaman Ditolak", "Maaf, pengajuan pinjaman Anda ditolak"));
    STATUS_MESSAGES.put(
        "APPROVED_WAITING_DISBURSEMENT_TO_DISBURSED",
        new StatusMessage("Pinjaman Dicairkan", "Dana pinjaman Anda telah dicairkan"));
  }

  /**
   * Notify user about loan status change via multiple channels (IN_APP + PUSH).
   *
   * @param loanApplication The loan application
   * @param fromStatus Previous status
   * @param toStatus New status
   */
  public void notifyLoanStatusChange(
      LoanApplication loanApplication, String fromStatus, String toStatus) {
    try {
      String transitionKey = fromStatus + "_TO_" + toStatus;
      StatusMessage message = STATUS_MESSAGES.get(transitionKey);

      if (message == null) {
        log.debug("No notification message configured for transition: {}", transitionKey);
        return;
      }

      Long userId = loanApplication.getUser().getId();
      Long loanId = loanApplication.getLoanApplicationId();

      // Create data payload for push notification
      Map<String, String> data = new HashMap<>();
      data.put("loanApplicationId", String.valueOf(loanId));
      data.put("loanId", String.valueOf(loanId)); // For compatibility
      data.put("fromStatus", fromStatus);
      data.put("toStatus", toStatus);
      data.put("status", toStatus); // For compatibility
      data.put("type", "LOAN_STATUS_CHANGE");

      // Send IN_APP notification (database record)
      createInAppNotification(userId, loanId, toStatus, message);

      // Send PUSH notification via all PUSH channels
      sendPushNotification(userId, message.title(), message.body(), data);

      // Handle special cases (staff notifications)
      notifyStaffIfNeeded(loanApplication, fromStatus, toStatus);

      // Send email for disbursement
      if ("APPROVED_WAITING_DISBURSEMENT".equals(fromStatus) && "DISBURSED".equals(toStatus)) {
        sendDisbursementEmail(loanApplication);
      }

    } catch (Exception e) {
      log.error(
          "Error sending loan notifications for loan {}: {}",
          loanApplication.getLoanApplicationId(),
          e.getMessage());
    }
  }

  private void createInAppNotification(
      Long userId, Long loanId, String notifType, StatusMessage message) {
    notificationService.createNotification(
        NotificationDTO.builder()
            .userId(userId)
            .relatedLoanApplicationId(loanId)
            .notifType(notifType)
            .channel("IN_APP")
            .message(message.body())
            .build());
  }

  private void sendPushNotification(
      Long userId, String title, String body, Map<String, String> data) {
    for (NotificationChannel channel : notificationChannels) {
      if (channel.supports("PUSH")) {
        channel.send(userId, title, body, data);
      }
    }
  }

  private void notifyStaffIfNeeded(
      LoanApplication loanApplication, String fromStatus, String toStatus) {
    Long loanId = loanApplication.getLoanApplicationId();

    // IN_REVIEW -> WAITING_APPROVAL: Notify branch managers
    if ("IN_REVIEW".equals(fromStatus) && "WAITING_APPROVAL".equals(toStatus)) {
      List<User> branchManagers = userRepository.findByRoles_Name("BRANCH_MANAGER");
      for (User manager : branchManagers) {
        notificationService.createNotification(
            NotificationDTO.builder()
                .userId(manager.getId())
                .relatedLoanApplicationId(loanId)
                .notifType("APPROVAL_REQUIRED")
                .channel("IN_APP")
                .message("Ada pengajuan pinjaman baru menunggu persetujuan Anda")
                .build());

        // Send push to staff
        Map<String, String> data = new HashMap<>();
        data.put("loanApplicationId", String.valueOf(loanId));
        data.put("type", "STAFF_NOTIFICATION");
        sendPushNotification(
            manager.getId(),
            "Persetujuan Diperlukan",
            "Ada pengajuan pinjaman baru menunggu persetujuan Anda",
            data);
      }
    }

    // WAITING_APPROVAL -> APPROVED_WAITING_DISBURSEMENT: Notify back office
    if ("WAITING_APPROVAL".equals(fromStatus) && "APPROVED_WAITING_DISBURSEMENT".equals(toStatus)) {
      List<User> backOfficeUsers = userRepository.findByRoles_Name("BACK_OFFICE");
      for (User backOffice : backOfficeUsers) {
        notificationService.createNotification(
            NotificationDTO.builder()
                .userId(backOffice.getId())
                .relatedLoanApplicationId(loanId)
                .notifType("DISBURSEMENT_REQUIRED")
                .channel("IN_APP")
                .message("Pinjaman yang disetujui menunggu pencairan")
                .build());

        // Send push to staff
        Map<String, String> data = new HashMap<>();
        data.put("loanApplicationId", String.valueOf(loanId));
        data.put("type", "STAFF_NOTIFICATION");
        sendPushNotification(
            backOffice.getId(),
            "Pencairan Diperlukan",
            "Pinjaman yang disetujui menunggu pencairan",
            data);
      }
    }
  }

  private void sendDisbursementEmail(LoanApplication loanApplication) {
    try {
      User user = loanApplication.getUser();
      emailService.sendLoanDisbursementEmail(
          user.getEmail(),
          user.getUsername(),
          loanApplication.getLoanApplicationId(),
          loanApplication.getAmount());
      log.info(
          "Disbursement email sent to {} for loan {}",
          user.getEmail(),
          loanApplication.getLoanApplicationId());
    } catch (Exception emailError) {
      log.error(
          "Failed to send disbursement email for loan {}: {}",
          loanApplication.getLoanApplicationId(),
          emailError.getMessage());
    }
  }

  /** Simple record to hold status message title and body. */
  private record StatusMessage(String title, String body) {}
}
