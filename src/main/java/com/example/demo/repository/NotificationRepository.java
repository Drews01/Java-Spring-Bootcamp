package com.example.demo.repository;

import com.example.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUser_IdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    List<Notification> findByRelatedLoanApplication_LoanApplicationId(Long loanApplicationId);

    List<Notification> findByChannel(String channel);

    Long countByUser_IdAndIsRead(Long userId, Boolean isRead);
}
