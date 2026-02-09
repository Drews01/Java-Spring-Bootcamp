package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestConfig.class)
@TestPropertySource(properties = "app.rate-limit.enabled=true")
public class RateLimitIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private RateLimitingService rateLimitingService;

  @Autowired private com.example.demo.repository.UserRepository userRepository;
  @Autowired private com.example.demo.repository.RoleRepository roleRepository;
  @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    // Configure mock to track rate limiting state per IP, but we need to ensure the
    // mocks are reset or handled correctly
    // ... existing mock setup ...
    final java.util.Map<String, Integer> loginAttempts =
        new java.util.concurrent.ConcurrentHashMap<>();
    final java.util.Map<String, Integer> registerAttempts =
        new java.util.concurrent.ConcurrentHashMap<>();
    final java.util.Map<String, Integer> forgotPasswordAttempts =
        new java.util.concurrent.ConcurrentHashMap<>();

    when(rateLimitingService.tryConsume(anyString(), any(RateLimitingService.Endpoint.class)))
        .thenAnswer(
            invocation -> {
              String clientIp = invocation.getArgument(0);
              RateLimitingService.Endpoint endpoint = invocation.getArgument(1);
              String key = endpoint.name() + ":" + clientIp;

              // We need to match the mock behavior logic
              return switch (endpoint) {
                case LOGIN -> {
                  int attempts = loginAttempts.merge(key, 1, Integer::sum);
                  yield attempts <= 5; // Allow 5 attempts
                }
                case REGISTER -> {
                  int attempts = registerAttempts.merge(key, 1, Integer::sum);
                  yield attempts <= 3; // Allow 3 attempts
                }
                case FORGOT_PASSWORD -> {
                  int attempts = forgotPasswordAttempts.merge(key, 1, Integer::sum);
                  yield attempts <= 2; // Allow 2 attempts
                }
              };
            });

    when(rateLimitingService.getSecondsUntilRefill(
            anyString(), any(RateLimitingService.Endpoint.class)))
        .thenReturn(60L);
  }

  private void createUser(String email) {
    if (userRepository.existsByEmail(email)) return;

    com.example.demo.entity.Role userRole =
        roleRepository
            .findByName("USER")
            .orElseGet(
                () ->
                    roleRepository.save(
                        com.example.demo.entity.Role.builder().name("USER").build()));

    java.util.Set<com.example.demo.entity.Role> roles = new java.util.HashSet<>();
    roles.add(userRole);

    com.example.demo.entity.User user =
        com.example.demo.entity.User.builder()
            .username(email)
            .email(email)
            .password(passwordEncoder.encode("password"))
            .isActive(true)
            .roles(roles)
            .authProvider(com.example.demo.entity.AuthProvider.LOCAL)
            .build();

    userRepository.save(user);
  }

  @Test
  @DisplayName("Should return 429 after exceeding login rate limit")
  void login_ExceedingRateLimit_ShouldReturn429() throws Exception {
    AuthRequest loginRequest = new AuthRequest("testuser", "wrongpassword", null, null, null);
    String requestBody = objectMapper.writeValueAsString(loginRequest);

    // Make 5 allowed requests (they may fail auth, but rate limit should pass)
    for (int i = 0; i < 5; i++) {
      // We accept 401 Unauthorized as a "success" for rate limiting purposes (i.e.
      // not 429)
      mockMvc
          .perform(
              post("/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .header("X-Forwarded-For", "10.0.0.1"))
          .andExpect(status().isUnauthorized());
    }

    // 6th request should be rate limited
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Forwarded-For", "10.0.0.1"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(jsonPath("$.error").value("Too Many Requests"));
  }

  @Test
  @DisplayName("Should return 429 after exceeding register rate limit")
  void register_ExceedingRateLimit_ShouldReturn429() throws Exception {
    // Make 3 allowed requests (they may fail validation, but rate limit should
    // pass)
    for (int i = 0; i < 3; i++) {
      RegisterRequest registerRequest =
          new RegisterRequest("user" + i, "user" + i + "@example.com", "password123");
      mockMvc
          .perform(
              post("/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(registerRequest))
                  .header("X-Forwarded-For", "10.0.0.2"))
          .andExpect(status().isCreated());
    }

    // 4th request should be rate limited
    RegisterRequest registerRequest =
        new RegisterRequest("user4", "user4@example.com", "password123");
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
                .header("X-Forwarded-For", "10.0.0.2"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  @DisplayName("Should return 429 after exceeding forgot-password rate limit")
  void forgotPassword_ExceedingRateLimit_ShouldReturn429() throws Exception {
    createUser("test@example.com");

    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("test@example.com");
    String requestBody = objectMapper.writeValueAsString(request);

    // Make 2 allowed requests
    for (int i = 0; i < 2; i++) {
      mockMvc
          .perform(
              post("/auth/forgot-password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .header("X-Forwarded-For", "10.0.0.3"))
          .andExpect(status().isOk());
    }

    // 3rd request should be rate limited
    mockMvc
        .perform(
            post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Forwarded-For", "10.0.0.3"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  @DisplayName("Different IPs should have independent rate limits")
  void rateLimit_DifferentIPs_ShouldBeIndependent() throws Exception {
    AuthRequest loginRequest = new AuthRequest("testuser", "wrongpassword", null, null, null);
    String requestBody = objectMapper.writeValueAsString(loginRequest);

    // Exhaust rate limit for IP1
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(
          post("/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody)
              .header("X-Forwarded-For", "10.0.0.100"));
    }

    // IP1 should be rate limited
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Forwarded-For", "10.0.0.100"))
        .andExpect(status().isTooManyRequests());

    // IP2 should still work
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Forwarded-For", "10.0.0.101"))
        .andExpect(status().isUnauthorized()); // Auth fails but not rate limited
  }
}
