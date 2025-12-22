package com.example.demo.repository;

import com.example.demo.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findByTokenHash(String tokenHash);

    List<PasswordReset> findByUser_Id(Long userId);

    Optional<PasswordReset> findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
            String tokenHash, LocalDateTime currentTime);

    void deleteByExpiresAtBefore(LocalDateTime expiryDate);
}
