package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.config.RateLimitConfig;
import com.example.demo.service.RateLimitingService.Endpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitingServiceTest {

  private RateLimitingService rateLimitingService;
  private RateLimitConfig rateLimitConfig;

  @BeforeEach
  void setUp() {
    rateLimitConfig = new RateLimitConfig();
    rateLimitConfig.setLogin(new RateLimitConfig.EndpointLimit(5, 60));
    rateLimitConfig.setRegister(new RateLimitConfig.EndpointLimit(3, 60));
    rateLimitConfig.setForgotPassword(new RateLimitConfig.EndpointLimit(2, 60));

    rateLimitingService = new RateLimitingService(rateLimitConfig);
  }

  @Test
  @DisplayName("Should allow requests within rate limit")
  void tryConsume_WithinLimit_ShouldReturnTrue() {
    String clientIp = "192.168.1.1";

    // All 5 login attempts should succeed
    for (int i = 0; i < 5; i++) {
      assertTrue(
          rateLimitingService.tryConsume(clientIp, Endpoint.LOGIN),
          "Attempt " + (i + 1) + " should succeed");
    }
  }

  @Test
  @DisplayName("Should block requests exceeding login rate limit")
  void tryConsume_ExceedingLoginLimit_ShouldReturnFalse() {
    String clientIp = "192.168.1.2";

    // Use all 5 allowed attempts
    for (int i = 0; i < 5; i++) {
      assertTrue(rateLimitingService.tryConsume(clientIp, Endpoint.LOGIN));
    }

    // 6th attempt should fail
    assertFalse(
        rateLimitingService.tryConsume(clientIp, Endpoint.LOGIN),
        "Should be blocked after exceeding limit");
  }

  @Test
  @DisplayName("Should block requests exceeding register rate limit")
  void tryConsume_ExceedingRegisterLimit_ShouldReturnFalse() {
    String clientIp = "192.168.1.3";

    // Use all 3 allowed attempts
    for (int i = 0; i < 3; i++) {
      assertTrue(rateLimitingService.tryConsume(clientIp, Endpoint.REGISTER));
    }

    // 4th attempt should fail
    assertFalse(
        rateLimitingService.tryConsume(clientIp, Endpoint.REGISTER),
        "Should be blocked after exceeding limit");
  }

  @Test
  @DisplayName("Should block requests exceeding forgot-password rate limit")
  void tryConsume_ExceedingForgotPasswordLimit_ShouldReturnFalse() {
    String clientIp = "192.168.1.4";

    // Use all 2 allowed attempts
    for (int i = 0; i < 2; i++) {
      assertTrue(rateLimitingService.tryConsume(clientIp, Endpoint.FORGOT_PASSWORD));
    }

    // 3rd attempt should fail
    assertFalse(
        rateLimitingService.tryConsume(clientIp, Endpoint.FORGOT_PASSWORD),
        "Should be blocked after exceeding limit");
  }

  @Test
  @DisplayName("Should track rate limits per IP independently")
  void tryConsume_DifferentIPs_ShouldHaveIndependentLimits() {
    String clientIp1 = "192.168.1.10";
    String clientIp2 = "192.168.1.11";

    // Exhaust limit for IP1
    for (int i = 0; i < 5; i++) {
      rateLimitingService.tryConsume(clientIp1, Endpoint.LOGIN);
    }

    // IP2 should still have full limit
    assertTrue(
        rateLimitingService.tryConsume(clientIp2, Endpoint.LOGIN),
        "Different IP should have independent limit");
  }

  @Test
  @DisplayName("Should track rate limits per endpoint independently")
  void tryConsume_DifferentEndpoints_ShouldHaveIndependentLimits() {
    String clientIp = "192.168.1.20";

    // Exhaust login limit
    for (int i = 0; i < 5; i++) {
      rateLimitingService.tryConsume(clientIp, Endpoint.LOGIN);
    }

    // Other endpoints should still work
    assertTrue(
        rateLimitingService.tryConsume(clientIp, Endpoint.REGISTER),
        "Different endpoint should have independent limit");
    assertTrue(
        rateLimitingService.tryConsume(clientIp, Endpoint.FORGOT_PASSWORD),
        "Different endpoint should have independent limit");
  }

  @Test
  @DisplayName("Should return correct remaining tokens")
  void getRemainingTokens_ShouldReturnCorrectCount() {
    String clientIp = "192.168.1.30";

    // Initially should have max tokens
    assertEquals(5, rateLimitingService.getRemainingTokens(clientIp, Endpoint.LOGIN));

    // After consuming some
    rateLimitingService.tryConsume(clientIp, Endpoint.LOGIN);
    rateLimitingService.tryConsume(clientIp, Endpoint.LOGIN);

    assertEquals(3, rateLimitingService.getRemainingTokens(clientIp, Endpoint.LOGIN));
  }
}
