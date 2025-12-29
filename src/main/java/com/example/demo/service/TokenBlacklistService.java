package com.example.demo.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service to handle blacklisting of JWT tokens using Redis.
 * Used for secure logout.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Blacklist a token with a specific expiration time.
     * 
     * @param token      The JWT token to blacklist (without "Bearer " prefix)
     * @param ttlSeconds Time to live in seconds (until token actual expiration)
     */
    public void blacklistToken(String token, long ttlSeconds) {
        // Use a prefix to distinguish blacklisted tokens in Redis
        String key = "blacklist:token:" + token;
        redisTemplate.opsForValue().set(key, "true", ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * Check if a token is blacklisted.
     * 
     * @param token The JWT token to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
