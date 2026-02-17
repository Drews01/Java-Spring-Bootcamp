package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify that service interfaces are properly wired with Spring dependency
 * injection.
 *
 * <p>This test validates Requirements 5.5: Service interfaces work correctly with dependency
 * injection.
 */
@SpringBootTest
@ActiveProfiles("test")
class ServiceInterfaceIntegrationTest {

  @Autowired(required = false)
  private IUserService userService;

  @Autowired(required = false)
  private IAuthService authService;

  @Autowired(required = false)
  private IOAuthService oAuthService;

  @Autowired(required = false)
  private ILoanWorkflowService loanWorkflowService;

  @Autowired(required = false)
  private IUserProfileService userProfileService;

  @Test
  void testUserServiceInterfaceInjection() {
    assertThat(userService)
        .as("IUserService should be injected successfully")
        .isNotNull()
        .as("IUserService should be implemented by UserService")
        .isInstanceOf(UserService.class);
  }

  @Test
  void testAuthServiceInterfaceInjection() {
    assertThat(authService)
        .as("IAuthService should be injected successfully")
        .isNotNull()
        .as("IAuthService should be implemented by AuthService")
        .isInstanceOf(AuthService.class);
  }

  @Test
  void testOAuthServiceInterfaceInjection() {
    assertThat(oAuthService)
        .as("IOAuthService should be injected successfully")
        .isNotNull()
        .as("IOAuthService should be implemented by OAuthService")
        .isInstanceOf(OAuthService.class);
  }

  @Test
  void testLoanWorkflowServiceInterfaceInjection() {
    assertThat(loanWorkflowService)
        .as("ILoanWorkflowService should be injected successfully")
        .isNotNull()
        .as("ILoanWorkflowService should be implemented by LoanWorkflowService")
        .isInstanceOf(LoanWorkflowService.class);
  }

  @Test
  void testUserProfileServiceInterfaceInjection() {
    assertThat(userProfileService)
        .as("IUserProfileService should be injected successfully")
        .isNotNull()
        .as("IUserProfileService should be implemented by UserProfileService")
        .isInstanceOf(UserProfileService.class);
  }
}
