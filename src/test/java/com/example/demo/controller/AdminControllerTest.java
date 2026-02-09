package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.AdminLoanApplicationDTO;
import com.example.demo.security.JwtService;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.AdminLoanService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminLoanService adminLoanService;

  @MockBean(name = "accessControl")
  private AccessControlService accessControlService;

  @MockBean private JwtService jwtService;

  @MockBean private com.example.demo.service.TokenBlacklistService tokenBlacklistService;

  @MockBean
  private com.example.demo.security.DynamicAuthorizationManager dynamicAuthorizationManager;

  @BeforeEach
  void setUp() throws Exception {
    when(accessControlService.hasMenu(any())).thenReturn(true);

    // Ensure authorization manager grants access
    when(dynamicAuthorizationManager.check(any(), any()))
        .thenAnswer(
            invocation -> {
              System.out.println("DEBUG: DynamicAuthorizationManager mock called");
              return new org.springframework.security.authorization.AuthorizationDecision(true);
            });
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  @DisplayName("Should return dashboard welcome message")
  void getDashboard_ShouldReturnWelcomeMessage() throws Exception {
    mockMvc
        .perform(get("/api/admin/dashboard"))
        .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value("Welcome to the System Administration Dashboard."))
        .andExpect(jsonPath("$.message").value("Admin access verified"));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  @DisplayName("Should return system logs message")
  void getSystemLogs_ShouldReturnLogsMessage() throws Exception {
    mockMvc
        .perform(get("/api/admin/system-logs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value("System logs would be visible here."))
        .andExpect(jsonPath("$.message").value("Admin access verified"));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  @DisplayName("Should return paginated loan applications")
  void getAllLoanApplications_ShouldReturnPagedLoans() throws Exception {
    // Given
    AdminLoanApplicationDTO loanDTO = new AdminLoanApplicationDTO();
    loanDTO.setLoanApplicationId(1L);
    loanDTO.setAmount(1000.0);
    loanDTO.setUserName("John Doe");

    Page<AdminLoanApplicationDTO> page = new PageImpl<>(List.of(loanDTO));

    when(adminLoanService.getAllLoanApplications(any(Pageable.class))).thenReturn(page);

    // When & Then
    mockMvc
        .perform(
            get("/api/admin/loan-applications")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].loanApplicationId").value(1))
        .andExpect(jsonPath("$.data[0].userName").value("John Doe"))
        .andExpect(jsonPath("$.message").value("Loan applications retrieved successfully"));
  }
}
