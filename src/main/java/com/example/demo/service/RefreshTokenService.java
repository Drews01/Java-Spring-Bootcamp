package com.example.demo.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for managing refresh tokens in Redis. Implements secure refresh token storage with
 * TTL-based expiration.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final StringRedisTemplate redisTemplate;

  private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
  private static final String USER_TOKENS_PREFIX = "user_refresh_tokens:";

  /**
   * Generate and store a refresh token for a user.
   *
   * @param userId the user ID
   * @param ttlSeconds time-to-live in seconds
   * @return the generated refresh token
   */
  public String createRefreshToken(Long userId, long ttlSeconds) {
    String refreshToken = UUID.randomUUID().toString();
    String key = REFRESH_TOKEN_PREFIX + refreshToken;

    // Store token -> userId mapping
    redisTemplate.opsForValue().set(key, userId.toString(), ttlSeconds, TimeUnit.SECONDS);

    // Track all tokens for this user (for bulk revocation)
    String userTokensKey = USER_TOKENS_PREFIX + userId;
    redisTemplate.opsForSet().add(userTokensKey, refreshToken);
    redisTemplate.expire(userTokensKey, ttlSeconds, TimeUnit.SECONDS);

    return refreshToken;
  }

  /**
   * Validate a refresh token and return the associated user ID.
   *
   * @param refreshToken the refresh token to validate
   * @return Optional containing userId if valid, empty otherwise
   */
  public Optional<Long> validateRefreshToken(String refreshToken) {
    String key = REFRESH_TOKEN_PREFIX + refreshToken;
    String userId = redisTemplate.opsForValue().get(key);

    if (userId != null) {
      return Optional.of(Long.parseLong(userId));
    }
    return Optional.empty();
  }

  /**
   * Revoke a single refresh token.
   *
   * @param refreshToken the token to revoke
   */
  public void revokeRefreshToken(String refreshToken) {
    String key = REFRESH_TOKEN_PREFIX + refreshToken;
    String userId = redisTemplate.opsForValue().get(key);

    // Delete the token
    redisTemplate.delete(key);

    // Remove from user's token set
    if (userId != null) {
      String userTokensKey = USER_TOKENS_PREFIX + userId;
      redisTemplate.opsForSet().remove(userTokensKey, refreshToken);
    }
  }

  /**
   * Revoke all refresh tokens for a user. Used on password reset to invalidate all sessions.
   *
   * @param userId the user ID
   */
  public void revokeAllUserTokens(Long userId) {
    String userTokensKey = USER_TOKENS_PREFIX + userId;
    Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);

    if (tokens != null) {
      for (String token : tokens) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
      }
    }

    // Delete the user's token set
    redisTemplate.delete(userTokensKey);
  }
}
