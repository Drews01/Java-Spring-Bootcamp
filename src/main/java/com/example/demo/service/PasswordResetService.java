package com.example.demo.service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

  private final StringRedisTemplate redisTemplate;

  private static final String RESET_TOKEN_PREFIX = "password_reset:token:";

  public String createPasswordResetToken(Long userId, int expiryHours) {
    String token = UUID.randomUUID().toString();
    String key = RESET_TOKEN_PREFIX + token;

    // Store token with userId as value and set TTL
    redisTemplate.opsForValue().set(key, String.valueOf(userId), Duration.ofHours(expiryHours));

    return token;
  }

  public Optional<Long> validateToken(String token) {
    String key = RESET_TOKEN_PREFIX + token;
    String userIdStr = redisTemplate.opsForValue().get(key);

    if (userIdStr != null) {
      return Optional.of(Long.parseLong(userIdStr));
    }
    return Optional.empty();
  }

  public void markTokenAsUsed(String token) {
    String key = RESET_TOKEN_PREFIX + token;
    redisTemplate.delete(key);
  }
}
