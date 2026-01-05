package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  public void setup() {
    // Ensure we start with a clean state or at least known state
    userRepository.deleteAll();

    // Register a user for login testing
    try {
      RegisterRequest registerRequest =
          new RegisterRequest("testuser", "test@example.com", "password123");

      mockMvc
          .perform(
              post("/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(registerRequest)))
          .andExpect(status().isCreated());
    } catch (Exception e) {
      throw new RuntimeException("Failed to register user for setup", e);
    }
  }

  @Test
  public void testLoginSuccess() throws Exception {
    AuthRequest loginRequest = new AuthRequest("testuser", "password123");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.token").exists());
  }

  @Test
  public void testLoginFailure_WrongPassword() throws Exception {
    AuthRequest loginRequest = new AuthRequest("testuser", "wrongpassword");

    // Expect 401 Unauthorized or 403 Forbidden depending on implementation
    // Commonly 401 or 403. Let's assume generic error status for now, usually 401.
    // If your app returns a specific error for bad credentials, adjust this.
    // For Spring Security defaults or common custom handling, 401/403/500 might
    // occur if exceptions aren't handled well.
    // Given previous context, let's just check it's NOT OK.

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized()); // Or isForbidden()
  }
}
