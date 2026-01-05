package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.AuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class RbacIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void testMarketingAccessToMarketingQueue() throws Exception {
    String token = getLoginToken("marketing", "pass123");

    mockMvc
        .perform(
            get("/api/loan-workflow/queue/marketing").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  public void testMarketingDeniedFromManagerQueue() throws Exception {
    String token = getLoginToken("marketing", "pass123");

    mockMvc
        .perform(
            get("/api/loan-workflow/queue/branch-manager")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testManagerAccessToManagerQueue() throws Exception {
    String token = getLoginToken("manager", "pass123");

    mockMvc
        .perform(
            get("/api/loan-workflow/queue/branch-manager")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  public void testAdminAccessToEverything() throws Exception {
    String token = getLoginToken("admin", "admin123");

    mockMvc
        .perform(
            get("/api/loan-workflow/queue/marketing").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/loan-workflow/queue/branch-manager")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  private String getLoginToken(String username, String password) throws Exception {
    AuthRequest loginRequest = new AuthRequest(username, password);
    String responseContent =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return objectMapper.readTree(responseContent).path("data").path("token").asText();
  }
}
