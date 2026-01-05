package com.example.demo.repository;

import com.example.demo.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

  List<Notification> findByUser_IdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

  List<Notification> findByRelatedLoanApplication_LoanApplicationId(Long loanApplicationId);

  List<Notification> findByChannel(String channel);

  Long countByUser_IdAndIsRead(Long userId, Boolean isRead);
}
