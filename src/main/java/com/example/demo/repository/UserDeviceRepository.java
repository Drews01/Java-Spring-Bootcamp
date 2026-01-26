package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Repository for managing FCM device tokens. */
@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

  /** Find all active devices for a user. */
  List<UserDevice> findByUserAndIsActiveTrue(User user);

  /** Find all devices for a user by user ID. */
  @Query("SELECT ud FROM UserDevice ud WHERE ud.user.id = :userId AND ud.isActive = true")
  List<UserDevice> findActiveDevicesByUserId(Long userId);

  /** Find device by FCM token. */
  Optional<UserDevice> findByFcmToken(String fcmToken);

  /** Check if a token exists for a user. */
  Optional<UserDevice> findByUserAndFcmToken(User user, String fcmToken);

  /** Delete device by FCM token. */
  @Modifying
  @Query("DELETE FROM UserDevice ud WHERE ud.fcmToken = :fcmToken")
  void deleteByFcmToken(String fcmToken);

  /** Deactivate device by FCM token (soft delete). */
  @Modifying
  @Query("UPDATE UserDevice ud SET ud.isActive = false WHERE ud.fcmToken = :fcmToken")
  void deactivateByFcmToken(String fcmToken);

  /** Get all FCM tokens for a list of user IDs. */
  @Query(
      "SELECT ud.fcmToken FROM UserDevice ud WHERE ud.user.id IN :userIds AND ud.isActive = true")
  List<String> findFcmTokensByUserIds(List<Long> userIds);

  /** Get all FCM tokens for a single user. */
  @Query("SELECT ud.fcmToken FROM UserDevice ud WHERE ud.user.id = :userId AND ud.isActive = true")
  List<String> findFcmTokensByUserId(Long userId);
}
