package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.AuthRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RbacIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private Role marketingRole;
  private Role managerRole;
  private Role adminRole;
  private Role userRole;

  @BeforeEach
  public void setup() {
    // Create roles if they don't exist
    userRole =
        roleRepository
            .findByName("USER")
            .orElseGet(
                () -> {
                  Role role = Role.builder().name("USER").build();
                  return roleRepository.save(role);
                });

    marketingRole =
        roleRepository
            .findByName("MARKETING")
            .orElseGet(
                () -> {
                  Role role = Role.builder().name("MARKETING").build();
                  return roleRepository.save(role);
                });

    managerRole =
        roleRepository
            .findByName("BRANCH_MANAGER")
            .orElseGet(
                () -> {
                  Role role = Role.builder().name("BRANCH_MANAGER").build();
                  return roleRepository.save(role);
                });

    adminRole =
        roleRepository
            .findByName("ADMIN")
            .orElseGet(
                () -> {
                  Role role = Role.builder().name("ADMIN").build();
                  return roleRepository.save(role);
                });

    // Create test users if they don't exist
    if (!userRepository.existsByUsername("marketing")) {
      Set<Role> roles = new HashSet<>();
      roles.add(userRole);
      roles.add(marketingRole);
      User marketingUser =
          User.builder()
              .username("marketing")
              .email("marketing@test.com")
              .password(passwordEncoder.encode("pass123"))
              .isActive(true)
              .roles(roles)
              .build();
      userRepository.save(marketingUser);
    }

    if (!userRepository.existsByUsername("manager")) {
      Set<Role> roles = new HashSet<>();
      roles.add(userRole);
      roles.add(managerRole);
      User managerUser =
          User.builder()
              .username("manager")
              .email("manager@test.com")
              .password(passwordEncoder.encode("pass123"))
              .isActive(true)
              .roles(roles)
              .build();
      userRepository.save(managerUser);
    }

    if (!userRepository.existsByUsername("admin")) {
      Set<Role> roles = new HashSet<>();
      roles.add(userRole);
      roles.add(adminRole);
      User adminUser =
          User.builder()
              .username("admin")
              .email("admin@test.com")
              .password(passwordEncoder.encode("admin123"))
              .isActive(true)
              .roles(roles)
              .build();
      userRepository.save(adminUser);
    }
  }

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
    AuthRequest loginRequest = new AuthRequest(username, password, null, null, null);
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
