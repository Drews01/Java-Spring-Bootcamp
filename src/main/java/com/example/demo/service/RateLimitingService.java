package com.example.demo.service;

import com.example.demo.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing rate limiting using Bucket4j. Provides in-memory rate limiting buckets per
 * client IP and endpoint combination.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {

  private final RateLimitConfig rateLimitConfig;

  /** Cache of rate limit buckets keyed by "endpoint:clientIp" */
  private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

  /** Endpoint identifiers for rate limiting. */
  public enum Endpoint {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
  }

  /**
   * Attempts to consume one token from the rate limit bucket for the given client and endpoint.
   *
   * @param clientIp The client's IP address
   * @param endpoint The endpoint being accessed
   * @return true if request is allowed, false if rate limit exceeded
   */
  public boolean tryConsume(String clientIp, Endpoint endpoint) {
    String key = buildKey(clientIp, endpoint);
    Bucket bucket = bucketCache.computeIfAbsent(key, k -> createBucket(endpoint));
    boolean consumed = bucket.tryConsume(1);

    if (!consumed) {
      log.warn("Rate limit exceeded for {} on endpoint {}", clientIp, endpoint);
    }

    return consumed;
  }

  /**
   * Gets the remaining tokens in the bucket for the given client and endpoint.
   *
   * @param clientIp The client's IP address
   * @param endpoint The endpoint being accessed
   * @return Number of remaining tokens, or max attempts if bucket doesn't exist
   */
  public long getRemainingTokens(String clientIp, Endpoint endpoint) {
    String key = buildKey(clientIp, endpoint);
    Bucket bucket = bucketCache.get(key);
    if (bucket == null) {
      return getMaxAttempts(endpoint);
    }
    return bucket.getAvailableTokens();
  }

  /**
   * Gets the time in seconds until the next token refill.
   *
   * @param clientIp The client's IP address
   * @param endpoint The endpoint being accessed
   * @return Seconds until refill, or 0 if bucket has tokens
   */
  public long getSecondsUntilRefill(String clientIp, Endpoint endpoint) {
    String key = buildKey(clientIp, endpoint);
    Bucket bucket = bucketCache.get(key);
    if (bucket == null || bucket.getAvailableTokens() > 0) {
      return 0;
    }
    return getDurationSeconds(endpoint);
  }

  private String buildKey(String clientIp, Endpoint endpoint) {
    return endpoint.name() + ":" + clientIp;
  }

  private Bucket createBucket(Endpoint endpoint) {
    int maxAttempts = getMaxAttempts(endpoint);
    int durationSeconds = getDurationSeconds(endpoint);

    Bandwidth limit =
        Bandwidth.classic(
            maxAttempts, Refill.intervally(maxAttempts, Duration.ofSeconds(durationSeconds)));

    return Bucket.builder().addLimit(limit).build();
  }

  private int getMaxAttempts(Endpoint endpoint) {
    return switch (endpoint) {
      case LOGIN -> rateLimitConfig.getLogin().getMaxAttempts();
      case REGISTER -> rateLimitConfig.getRegister().getMaxAttempts();
      case FORGOT_PASSWORD -> rateLimitConfig.getForgotPassword().getMaxAttempts();
    };
  }

  private int getDurationSeconds(Endpoint endpoint) {
    return switch (endpoint) {
      case LOGIN -> rateLimitConfig.getLogin().getDurationSeconds();
      case REGISTER -> rateLimitConfig.getRegister().getDurationSeconds();
      case FORGOT_PASSWORD -> rateLimitConfig.getForgotPassword().getDurationSeconds();
    };
  }
}
