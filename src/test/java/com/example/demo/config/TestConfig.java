package com.example.demo.config;

import static org.mockito.Mockito.mock;

import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class TestConfig {

  @Bean
  @Primary
  public FirebaseMessaging firebaseMessaging() {
    return mock(FirebaseMessaging.class);
  }

  @Bean
  @Primary
  public JavaMailSender javaMailSender() {
    return mock(JavaMailSender.class);
  }

  @Bean
  @Primary
  public com.example.demo.service.RefreshTokenService refreshTokenService() {
    return mock(com.example.demo.service.RefreshTokenService.class);
  }

  @Bean
  @Primary
  public com.example.demo.service.TokenBlacklistService tokenBlacklistService() {
    return new StubTokenBlacklistService();
  }

  @Bean
  @Primary
  public com.example.demo.service.PasswordResetService passwordResetService() {
    return mock(com.example.demo.service.PasswordResetService.class);
  }

  @Bean
  @Primary
  public com.example.demo.service.FCMService fcmService() {
    return mock(com.example.demo.service.FCMService.class);
  }

  @Bean
  @Primary
  public com.example.demo.service.EmailService emailService() {
    return mock(com.example.demo.service.EmailService.class);
  }

  static class StubTokenBlacklistService extends com.example.demo.service.TokenBlacklistService {
    private final java.util.Set<String> blacklisted = new java.util.HashSet<>();

    public StubTokenBlacklistService() {
      super(null);
    }

    @Override
    public void blacklistToken(String token, long ttlSeconds) {
      blacklisted.add(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
      return blacklisted.contains(token);
    }
  }
}
