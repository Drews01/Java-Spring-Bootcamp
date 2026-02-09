package com.example.demo.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for Security Configuration.
 *
 * <p>Tests endpoint security rules including public and protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Should allow access to public endpoints without authentication")
  void publicEndpoints_shouldBeAccessible() throws Exception {
    mockMvc.perform(get("/api/csrf-token")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should allow access to auth endpoints without authentication")
  void authEndpoints_shouldBeAccessible() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usernameOrEmail\":\"test\",\"password\":\"test\"}"))
        .andExpect(status().isUnauthorized()); // 401 because user doesn't exist
  }

  @Test
  @DisplayName("Should require authentication for protected endpoints")
  void protectedEndpoints_shouldRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/users").with(csrf())).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should require authentication for loan endpoints")
  void loanEndpoints_shouldRequireAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/loan-applications").with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should require authentication for admin endpoints")
  void adminEndpoints_shouldRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/admin/dashboard").with(csrf())).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should require CSRF token for state-changing requests")
  void postWithoutCsrf_shouldBeForbidden() throws Exception {
    // Use /auth/logout which requires CSRF (not exempted like /auth/login)
    mockMvc
        .perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
