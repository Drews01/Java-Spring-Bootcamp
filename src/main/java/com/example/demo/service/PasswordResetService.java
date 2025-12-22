package com.example.demo.service;

import com.example.demo.entity.PasswordReset;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetRepository passwordResetRepository;
    private final UserRepository userRepository;

    @Transactional
    public PasswordReset createPasswordResetToken(Long userId, int expiryHours) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String token = UUID.randomUUID().toString();

        PasswordReset passwordReset = PasswordReset.builder()
                .user(user)
                .tokenHash(token)
                .expiresAt(LocalDateTime.now().plusHours(expiryHours))
                .build();

        return passwordResetRepository.save(passwordReset);
    }

    @Transactional(readOnly = true)
    public Optional<PasswordReset> validateToken(String token) {
        return passwordResetRepository.findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                token, LocalDateTime.now());
    }

    @Transactional
    public void markTokenAsUsed(String token) {
        PasswordReset passwordReset = passwordResetRepository.findByTokenHash(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        passwordReset.setUsedAt(LocalDateTime.now());
        passwordResetRepository.save(passwordReset);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
