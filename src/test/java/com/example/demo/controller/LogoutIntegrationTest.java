package com.example.demo.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestConfig;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.springframework.context.annotation.Import(TestConfig.class)
public class LogoutIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @BeforeEach
  public void setup() {
    // Clean up test user if exists from previous runs
    userRepository.findByUsername("logouttest").ifPresent(userRepository::delete);
  }

  @Test
  public void testLogoutFlow() throws Exception {
    // 1. Register a user
    RegisterRequest registerRequest =
        new RegisterRequest("logouttest", "logouttest@example.com", "password");
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    // 2. Login to get token
    AuthRequest loginRequest = new AuthRequest("logouttest", "password", null, null, null);
    String responseContent =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String token = objectMapper.readTree(responseContent).path("data").path("token").asText();

    // 3. Logout with valid token (include CSRF token for POST request)
    mockMvc
        .perform(post("/auth/logout").header("Authorization", "Bearer " + token).with(csrf()))
        .andExpect(status().isOk());

    // 4. Access protected endpoint AGAIN with SAME token -> Should be 401
    // Unauthorized
    // (token is now blacklisted)
    mockMvc
        .perform(post("/auth/logout").header("Authorization", "Bearer " + token).with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}
