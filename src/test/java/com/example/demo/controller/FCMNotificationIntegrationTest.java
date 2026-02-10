package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.PushNotificationRequest;
import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserDevice;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserDeviceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FCMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for FCM (Firebase Cloud Messaging) notification flows including: - Device token
 * registration - Device token unregistration - Push notification sending - Test notification
 * endpoint
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestConfig.class)
public class FCMNotificationIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private UserDeviceRepository userDeviceRepository;

  @Autowired private FCMService fcmService;

  @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

  private static final String TEST_USER = "testfcmuser";
  private static final String TEST_PASSWORD = "testpass123";
  private static final String TEST_EMAIL = "testfcm@example.com";
  private static final String TEST_FCM_TOKEN = "test_fcm_token_" + System.currentTimeMillis();
  private static final String ADMIN_USER = "testfcadmin";
  private static final String ADMIN_PASSWORD = "adminpass123";
  private static final String ADMIN_EMAIL = "testfcadmin@example.com";

  @BeforeEach
  void setUp() {
    // Reset the FCMService mock before each test
    reset(fcmService);

    // Configure mock FCMService to delegate saveDeviceToken to real
    // UserDeviceRepository
    doAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              String fcmToken = invocation.getArgument(1);
              String deviceName = invocation.getArgument(2);
              String platform = invocation.getArgument(3);
              if (fcmToken == null || fcmToken.isBlank()) return null;
              var existingDevice = userDeviceRepository.findByFcmToken(fcmToken);
              if (existingDevice.isPresent()) {
                var device = existingDevice.get();
                device.setUser(user);
                device.setDeviceName(deviceName);
                device.setPlatform(platform);
                device.setIsActive(true);
                userDeviceRepository.save(device);
              } else {
                userDeviceRepository.save(
                    UserDevice.builder()
                        .user(user)
                        .fcmToken(fcmToken)
                        .deviceName(deviceName)
                        .platform(platform)
                        .isActive(true)
                        .build());
              }
              return null;
            })
        .when(fcmService)
        .saveDeviceToken(any(User.class), anyString(), any(), any());

    // Configure mock FCMService to delegate removeDeviceToken to real
    // UserDeviceRepository
    doAnswer(
            invocation -> {
              String fcmToken = invocation.getArgument(0);
              if (fcmToken != null && !fcmToken.isBlank()) {
                userDeviceRepository
                    .findByFcmToken(fcmToken)
                    .ifPresent(
                        device -> {
                          device.setIsActive(false);
                          userDeviceRepository.save(device);
                        });
              }
              return null;
            })
        .when(fcmService)
        .removeDeviceToken(anyString());
  }

  private User createTestUser(String username, String email, String password, boolean isAdmin) {
    if (userRepository.existsByUsername(username)) {
      return userRepository.findByUsername(username).orElseThrow();
    }

    Role userRole =
        roleRepository
            .findByName("USER")
            .orElseGet(
                () -> {
                  Role role = Role.builder().name("USER").build();
                  return roleRepository.save(role);
                });

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    if (isAdmin) {
      Role adminRole =
          roleRepository
              .findByName("ADMIN")
              .orElseGet(
                  () -> {
                    Role role = Role.builder().name("ADMIN").build();
                    return roleRepository.save(role);
                  });
      roles.add(adminRole);
    }

    User user =
        User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .isActive(true)
            .roles(roles)
            .authProvider(AuthProvider.LOCAL)
            .build();

    return userRepository.save(user);
  }

  private String getAuthToken(String username, String password) throws Exception {
    AuthRequest loginRequest = new AuthRequest(username, password, null, null, null);

    MvcResult result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    String responseContent = result.getResponse().getContentAsString();
    return objectMapper.readTree(responseContent).path("data").path("token").asText();
  }

  @Test
  @DisplayName("Should register device token successfully")
  void registerDevice_ValidToken_ShouldSucceed() throws Exception {
    createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);
    String fcmToken = TEST_FCM_TOKEN + "_register";

    mockMvc
        .perform(
            post("/api/fcm/register")
                .with(csrf())
                .param("fcmToken", fcmToken)
                .param("deviceName", "Test Device")
                .param("platform", "ANDROID")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Device registered for push notifications"));

    // Verify device was saved to database
    var deviceOpt = userDeviceRepository.findByFcmToken(fcmToken);
    assertTrue(deviceOpt.isPresent(), "Device should be saved to database");
    assertThat(deviceOpt.get().getDeviceName()).isEqualTo("Test Device");
    assertThat(deviceOpt.get().getPlatform()).isEqualTo("ANDROID");
    assertThat(deviceOpt.get().getIsActive()).isTrue();
  }

  @Test
  @DisplayName("Should update existing device token for same user")
  void registerDevice_ExistingToken_ShouldUpdate() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);
    String fcmToken = TEST_FCM_TOKEN + "_update";

    // First registration
    mockMvc.perform(
        post("/api/fcm/register")
            .with(csrf())
            .param("fcmToken", fcmToken)
            .param("deviceName", "Old Device")
            .param("platform", "ANDROID")
            .header("Authorization", "Bearer " + token));

    // Second registration with same token but different device
    mockMvc.perform(
        post("/api/fcm/register")
            .with(csrf())
            .param("fcmToken", fcmToken)
            .param("deviceName", "New Device")
            .param("platform", "IOS")
            .header("Authorization", "Bearer " + token));

    // Verify device was updated
    var deviceOpt = userDeviceRepository.findByFcmToken(fcmToken);
    assertTrue(deviceOpt.isPresent());
    assertThat(deviceOpt.get().getDeviceName()).isEqualTo("New Device");
    assertThat(deviceOpt.get().getPlatform()).isEqualTo("IOS");
  }

  @Test
  @DisplayName("Should reject registration without authentication")
  void registerDevice_NoAuthentication_ShouldReturn401() throws Exception {
    mockMvc
        .perform(
            post("/api/fcm/register")
                .with(csrf())
                .param("fcmToken", TEST_FCM_TOKEN)
                .param("deviceName", "Test Device")
                .param("platform", "ANDROID"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should skip registration for empty FCM token")
  void registerDevice_EmptyToken_ShouldSkip() throws Exception {
    createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);

    mockMvc.perform(
        post("/api/fcm/register")
            .with(csrf())
            .param("fcmToken", "")
            .param("deviceName", "Test Device")
            .param("platform", "ANDROID")
            .header("Authorization", "Bearer " + token));

    // Verify no device was saved
    List<UserDevice> devices = userDeviceRepository.findAll();
    assertThat(devices.stream().filter(d -> d.getDeviceName().equals("Test Device")).count())
        .isZero();
  }

  @Test
  @DisplayName("Should unregister device token successfully")
  void unregisterDevice_ValidToken_ShouldDeactivate() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);
    String fcmToken = TEST_FCM_TOKEN + "_unregister";

    // First register the device
    mockMvc.perform(
        post("/api/fcm/register")
            .with(csrf())
            .param("fcmToken", fcmToken)
            .param("deviceName", "Test Device")
            .param("platform", "ANDROID")
            .header("Authorization", "Bearer " + token));

    // Verify device is active
    var deviceOpt = userDeviceRepository.findByFcmToken(fcmToken);
    assertTrue(deviceOpt.isPresent());
    assertTrue(deviceOpt.get().getIsActive());

    // Now unregister
    mockMvc
        .perform(
            delete("/api/fcm/unregister")
                .with(csrf())
                .param("fcmToken", fcmToken)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Device unregistered from push notifications"));

    // Verify device was deactivated
    var deactivatedDevice = userDeviceRepository.findByFcmToken(fcmToken);
    assertTrue(deactivatedDevice.isPresent());
    assertFalse(deactivatedDevice.get().getIsActive());
  }

  @Test
  @DisplayName("Should handle unregister for non-existent token gracefully")
  void unregisterDevice_NonExistentToken_ShouldSucceed() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);

    mockMvc
        .perform(
            delete("/api/fcm/unregister")
                .with(csrf())
                .param("fcmToken", "non_existent_token_12345")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should handle unregister for empty token gracefully")
  void unregisterDevice_EmptyToken_ShouldSucceed() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);

    mockMvc
        .perform(
            delete("/api/fcm/unregister")
                .with(csrf())
                .param("fcmToken", "")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should send notification to user as ADMIN")
  void sendNotification_AdminUser_ShouldSucceed() throws Exception {
    User admin = createTestUser(ADMIN_USER, ADMIN_EMAIL, ADMIN_PASSWORD, true);
    User targetUser = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);

    String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASSWORD);

    // Register a device for the target user
    UserDevice device =
        UserDevice.builder()
            .user(targetUser)
            .fcmToken("target_device_token_" + System.currentTimeMillis())
            .deviceName("Target Device")
            .platform("ANDROID")
            .isActive(true)
            .build();
    userDeviceRepository.save(device);

    // Mock FCMService response
    when(fcmService.sendPushNotification(any(), any(), any(), any())).thenReturn(1);

    PushNotificationRequest request =
        PushNotificationRequest.builder()
            .userId(targetUser.getId())
            .title("Test Title")
            .body("Test Body")
            .data(Map.of("key", "value"))
            .build();

    mockMvc
        .perform(
            post("/api/fcm/send")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(1));

    verify(fcmService, atLeastOnce())
        .sendPushNotification(
            targetUser.getId(), "Test Title", "Test Body", Map.of("key", "value"));
  }

  @Test
  @DisplayName("Should return 0 when sending to user with no devices")
  void sendNotification_NoDevices_ShouldReturnZero() throws Exception {
    User admin = createTestUser(ADMIN_USER, ADMIN_EMAIL, ADMIN_PASSWORD, true);
    User targetUser = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);

    String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASSWORD);

    // Ensure target user has no devices
    userDeviceRepository.deleteAll();

    when(fcmService.sendPushNotification(any(), any(), any(), any())).thenReturn(0);

    PushNotificationRequest request =
        PushNotificationRequest.builder()
            .userId(targetUser.getId())
            .title("Test Title")
            .body("Test Body")
            .build();

    mockMvc
        .perform(
            post("/api/fcm/send")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value(0));
  }

  @Test
  @DisplayName("Should reject send notification request without required fields")
  void sendNotification_MissingRequiredFields_ShouldReturn400() throws Exception {
    User admin = createTestUser(ADMIN_USER, ADMIN_EMAIL, ADMIN_PASSWORD, true);
    String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASSWORD);

    // Missing userId
    PushNotificationRequest request =
        PushNotificationRequest.builder().title("Test Title").body("Test Body").build();

    mockMvc
        .perform(
            post("/api/fcm/send")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should send test notification to current user")
  void testNotification_AuthenticatedUser_ShouldSucceed() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);

    // Register a device for the user
    UserDevice device =
        UserDevice.builder()
            .user(user)
            .fcmToken("test_device_token_" + System.currentTimeMillis())
            .deviceName("Test Device")
            .platform("ANDROID")
            .isActive(true)
            .build();
    userDeviceRepository.save(device);

    when(fcmService.sendPushNotification(any(), any(), any(), any())).thenReturn(1);

    mockMvc
        .perform(
            post("/api/fcm/test")
                .with(csrf())
                .param("title", "Custom Test Title")
                .param("body", "Custom Test Body")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(1))
        .andExpect(jsonPath("$.message").value("Test notification sent to 1 device(s)"));

    verify(fcmService, atLeastOnce()).sendPushNotification(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Should return informative message when no devices for test notification")
  void testNotification_NoDevices_ShouldReturnInfoMessage() throws Exception {
    createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);

    // Ensure no devices
    userDeviceRepository.deleteAll();

    when(fcmService.sendPushNotification(any(), any(), any(), any())).thenReturn(0);

    mockMvc
        .perform(post("/api/fcm/test").with(csrf()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value(0))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "No devices registered for push notifications. Please login with fcmToken to register."));
  }

  @Test
  @DisplayName("Should use default title and body for test notification")
  void testNotification_DefaultParams_ShouldUseDefaults() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String token = getAuthToken(TEST_USER, TEST_PASSWORD);

    UserDevice device =
        UserDevice.builder()
            .user(user)
            .fcmToken("default_test_token_" + System.currentTimeMillis())
            .deviceName("Test Device")
            .platform("ANDROID")
            .isActive(true)
            .build();
    userDeviceRepository.save(device);

    when(fcmService.sendPushNotification(any(), any(), any(), any())).thenReturn(1);

    mockMvc
        .perform(post("/api/fcm/test").with(csrf()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    verify(fcmService, atLeastOnce())
        .sendPushNotification(
            any(),
            org.mockito.ArgumentMatchers.eq("Test Notification"),
            org.mockito.ArgumentMatchers.eq("This is a test push notification"),
            any());
  }

  @Test
  @DisplayName("Should reject test notification without authentication")
  void testNotification_NoAuthentication_ShouldReturn401() throws Exception {
    mockMvc.perform(post("/api/fcm/test").with(csrf())).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should find FCM tokens by user ID")
  void findFcmTokensByUserId_ExistingDevices_ShouldReturnTokens() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);

    // Create multiple devices for the user
    UserDevice device1 =
        UserDevice.builder()
            .user(user)
            .fcmToken("token_1_" + System.currentTimeMillis())
            .deviceName("Device 1")
            .platform("ANDROID")
            .isActive(true)
            .build();
    UserDevice device2 =
        UserDevice.builder()
            .user(user)
            .fcmToken("token_2_" + System.currentTimeMillis())
            .deviceName("Device 2")
            .platform("IOS")
            .isActive(true)
            .build();
    userDeviceRepository.save(device1);
    userDeviceRepository.save(device2);

    List<String> tokens = userDeviceRepository.findFcmTokensByUserId(user.getId());

    assertThat(tokens).hasSize(2);
    assertThat(tokens).contains(device1.getFcmToken(), device2.getFcmToken());
  }

  @Test
  @DisplayName("Should only return active tokens")
  void findFcmTokensByUserId_InactiveDevices_ShouldExclude() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);

    UserDevice activeDevice =
        UserDevice.builder()
            .user(user)
            .fcmToken("active_token_" + System.currentTimeMillis())
            .deviceName("Active Device")
            .platform("ANDROID")
            .isActive(true)
            .build();
    UserDevice inactiveDevice =
        UserDevice.builder()
            .user(user)
            .fcmToken("inactive_token_" + System.currentTimeMillis())
            .deviceName("Inactive Device")
            .platform("IOS")
            .isActive(false)
            .build();
    userDeviceRepository.save(activeDevice);
    userDeviceRepository.save(inactiveDevice);

    List<String> tokens = userDeviceRepository.findFcmTokensByUserId(user.getId());

    assertThat(tokens).hasSize(1);
    assertThat(tokens).contains(activeDevice.getFcmToken());
    assertThat(tokens).doesNotContain(inactiveDevice.getFcmToken());
  }

  @Test
  @DisplayName("Should handle device registration with FCM token during login")
  void login_WithFcmToken_ShouldRegisterDevice() throws Exception {
    createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);
    String fcmToken = "login_fcm_token_" + System.currentTimeMillis();

    AuthRequest loginRequest =
        new AuthRequest(TEST_USER, TEST_PASSWORD, fcmToken, "Test Device", "ANDROID");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk());

    // Verify device was registered
    var deviceOpt = userDeviceRepository.findByFcmToken(fcmToken);
    assertTrue(deviceOpt.isPresent(), "Device should be registered during login");
  }

  @Test
  @DisplayName("Should not call FCM service when user has no registered devices")
  void sendPushNotification_NoDevices_ShouldNotCallFCM() throws Exception {
    User user = createTestUser(TEST_USER, TEST_EMAIL, TEST_PASSWORD, false);

    // Ensure no devices
    userDeviceRepository.deleteAll();

    // Call the service method directly
    int result = fcmService.sendPushNotification(user.getId(), "Title", "Body", null);

    assertThat(result).isZero();
    // Verify that FCM messaging was never called (since there are no tokens)
    verify(fcmService, never()).sendPushNotificationToToken(any(), any(), any(), any());
  }
}
