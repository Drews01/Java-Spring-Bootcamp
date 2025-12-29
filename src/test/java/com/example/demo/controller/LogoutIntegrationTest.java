package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class LogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        // Roles initialized in ApplicationRunner or data.sql, but we can ensure user
        // exists
    }

    @Test
    public void testLogoutFlow() throws Exception {
        // 1. Register a user
        RegisterRequest registerRequest = new RegisterRequest("testuser", "test@example.com", "password");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Login to get token
        AuthRequest loginRequest = new AuthRequest("testuser", "password");
        String responseContent = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper().readTree(responseContent).path("data").path("token").asText();

        // 3. Access a protected endpoint (e.g., getting user profile if available, or
        // just ANY protected endpoint)
        // Since we don't have a simple GET endpoint handy in the prompt, let's assume
        // one or try to access something we know is protected.
        // Actually, we can just try to access a non-existent endpoint that is
        // protected, 404 is still authenticated.
        // Better: let's try calling /auth/logout (it accepts the token) -> 200 OK.

        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 4. Access protected endpoint AGAIN with SAME token -> Should be 401
        // Unauthorized
        // We can use /auth/logout again, it's protected now!
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
