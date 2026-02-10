package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestConfig;
import com.example.demo.dto.AuthRequest;
import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for file upload flows including: - KTP image upload via UserProfileController -
 * File download via FileController - File validation and storage interactions
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestConfig.class)
@TestPropertySource(properties = "app.storage.type=local")
public class FileUploadIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private UserProfileRepository userProfileRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private StorageService storageService;

  @Value("${app.upload.dir:./uploads}")
  private String uploadDir;

  private static final String TEST_USERNAME = "testupload";
  private static final String TEST_PASSWORD = "testpass123";
  private static final String TEST_EMAIL = "testupload@example.com";

  @BeforeEach
  void setUp() {
    // Clean up test files before each test
    cleanupTestFiles();
  }

  private void cleanupTestFiles() {
    try {
      Path uploadPath = Paths.get(uploadDir);
      if (Files.exists(uploadPath)) {
        Files.list(uploadPath)
            .filter(
                path ->
                    path.getFileName().toString().startsWith("test_")
                        || path.getFileName().toString().contains("ktp"))
            .forEach(
                path -> {
                  try {
                    Files.deleteIfExists(path);
                  } catch (Exception e) {
                    // Ignore cleanup errors
                  }
                });
      }
    } catch (Exception e) {
      // Ignore cleanup errors
    }
  }

  private User createTestUser() {
    if (userRepository.existsByUsername(TEST_USERNAME)) {
      return userRepository.findByUsername(TEST_USERNAME).orElseThrow();
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

    User user =
        User.builder()
            .username(TEST_USERNAME)
            .email(TEST_EMAIL)
            .password(passwordEncoder.encode(TEST_PASSWORD))
            .isActive(true)
            .roles(roles)
            .authProvider(AuthProvider.LOCAL)
            .build();

    return userRepository.save(user);
  }

  private String getAuthToken() throws Exception {
    createTestUser();

    AuthRequest loginRequest = new AuthRequest(TEST_USERNAME, TEST_PASSWORD, null, null, null);

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
  @DisplayName("Should successfully upload valid JPEG image")
  void uploadKtp_ValidJpegImage_ShouldSucceed() throws Exception {
    User user = createTestUser();
    String token = getAuthToken();

    // Mock storage service to return a test URL
    String mockFileUrl = "http://localhost:8080/uploads/test_ktp_" + user.getId() + ".jpg";
    when(storageService.uploadFile(any(), eq("ktp"))).thenReturn(mockFileUrl);

    // Create a valid JPEG file (minimal JPEG header)
    byte[] jpegContent =
        new byte[] {
          (byte) 0xFF,
          (byte) 0xD8,
          (byte) 0xFF,
          (byte) 0xE0,
          0x00,
          0x10,
          0x4A,
          0x46,
          0x49,
          0x46,
          0x00,
          0x01,
          0x01,
          0x00,
          0x00,
          0x01,
          0x00,
          0x01,
          0x00,
          0x00,
          (byte) 0xFF,
          (byte) 0xD9
        };

    MockMultipartFile file =
        new MockMultipartFile("file", "test_ktp.jpg", MediaType.IMAGE_JPEG_VALUE, jpegContent);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(file)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fileName").value("test_ktp.jpg"))
        .andExpect(jsonPath("$.data.fileType").value(MediaType.IMAGE_JPEG_VALUE))
        .andExpect(jsonPath("$.data.fileDownloadUri").value(mockFileUrl))
        .andExpect(jsonPath("$.message").value("KTP image uploaded successfully"));
  }

  @Test
  @DisplayName("Should successfully upload valid PNG image")
  void uploadKtp_ValidPngImage_ShouldSucceed() throws Exception {
    User user = createTestUser();
    String token = getAuthToken();

    String mockFileUrl = "http://localhost:8080/uploads/test_ktp_" + user.getId() + ".png";
    when(storageService.uploadFile(any(), eq("ktp"))).thenReturn(mockFileUrl);

    // Create a valid PNG file (PNG signature)
    byte[] pngContent = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    MockMultipartFile file =
        new MockMultipartFile("file", "test_ktp.png", MediaType.IMAGE_PNG_VALUE, pngContent);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(file)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fileName").value("test_ktp.png"))
        .andExpect(jsonPath("$.data.fileType").value(MediaType.IMAGE_PNG_VALUE));
  }

  @Test
  @DisplayName("Should reject upload without authentication")
  void uploadKtp_NoAuthentication_ShouldReturn401() throws Exception {
    byte[] jpegContent = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};

    MockMultipartFile file =
        new MockMultipartFile("file", "test_ktp.jpg", MediaType.IMAGE_JPEG_VALUE, jpegContent);

    mockMvc
        .perform(multipart("/api/user-profiles/upload-ktp").file(file).with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should reject empty file upload")
  void uploadKtp_EmptyFile_ShouldReturn400() throws Exception {
    createTestUser();
    String token = getAuthToken();

    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(emptyFile)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should reject invalid file type")
  void uploadKtp_InvalidFileType_ShouldReturn400() throws Exception {
    createTestUser();
    String token = getAuthToken();

    // Create a text file disguised as JPG
    byte[] textContent = "This is not an image".getBytes();

    MockMultipartFile file =
        new MockMultipartFile("file", "fake.jpg", MediaType.IMAGE_JPEG_VALUE, textContent);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(file)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should reject file upload with executable content")
  void uploadKtp_ExecutableFile_ShouldReturn400() throws Exception {
    createTestUser();
    String token = getAuthToken();

    // Create content that looks like an executable/script
    byte[] executableContent = "#!/bin/bash\necho 'malicious'".getBytes();

    MockMultipartFile file =
        new MockMultipartFile("file", "script.jpg", MediaType.IMAGE_JPEG_VALUE, executableContent);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(file)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should serve existing uploaded file")
  void downloadFile_ExistingFile_ShouldSucceed() throws Exception {
    // Create uploads directory and a test file
    Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    String testFileName = "test_download_file.txt";
    Path testFilePath = uploadPath.resolve(testFileName);
    Files.write(testFilePath, "Test file content".getBytes());

    try {
      mockMvc
          .perform(get("/uploads/{fileName}", testFileName))
          .andExpect(status().isOk())
          .andExpect(
              result -> {
                String contentDisposition = result.getResponse().getHeader("Content-Disposition");
                assertThat(contentDisposition)
                    .contains("attachment; filename=\"" + testFileName + "\"");
              })
          .andExpect(
              result ->
                  assertThat(result.getResponse().getHeader("X-Content-Type-Options"))
                      .isEqualTo("nosniff"))
          .andExpect(
              result ->
                  assertThat(result.getResponse().getHeader("Content-Security-Policy"))
                      .isEqualTo("default-src 'none'"));
    } finally {
      // Cleanup
      Files.deleteIfExists(testFilePath);
    }
  }

  @Test
  @DisplayName("Should return 404 for non-existent file")
  void downloadFile_NonExistentFile_ShouldReturn404() throws Exception {
    mockMvc
        .perform(get("/uploads/{fileName}", "nonexistent_file_12345.txt"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should prevent path traversal attacks")
  void downloadFile_PathTraversalAttempt_ShouldReturn404() throws Exception {
    // Try to access files outside uploads directory
    mockMvc
        .perform(get("/uploads/{fileName}", "../../../etc/passwd"))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(get("/uploads/{fileName}", "..\\..\\windows\\system32\\config\\sam"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should update user profile with KTP path after upload")
  void uploadKtp_ProfileUpdate_ShouldPersistKtpPath() throws Exception {
    User user = createTestUser();
    String token = getAuthToken();

    String mockFileUrl = "http://localhost:8080/uploads/ktp_user_" + user.getId() + ".jpg";
    when(storageService.uploadFile(any(), eq("ktp"))).thenReturn(mockFileUrl);

    byte[] jpegContent =
        new byte[] {
          (byte) 0xFF,
          (byte) 0xD8,
          (byte) 0xFF,
          (byte) 0xE0,
          0x00,
          0x10,
          0x4A,
          0x46,
          0x49,
          0x46,
          0x00,
          0x01
        };

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "ktp_update_test.jpg", MediaType.IMAGE_JPEG_VALUE, jpegContent);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(file)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    // Verify profile was updated with KTP path
    var profileOpt = userProfileRepository.findById(user.getId());
    assertTrue(profileOpt.isPresent(), "User profile should exist after upload");
    assertThat(profileOpt.get().getKtpPath()).isEqualTo(mockFileUrl);
  }

  @Test
  @DisplayName("Should create new profile if not exists during KTP upload")
  void uploadKtp_NoExistingProfile_ShouldCreateProfile() throws Exception {
    User user = createTestUser();
    String token = getAuthToken();

    // Ensure no profile exists
    userProfileRepository.deleteById(user.getId());

    String mockFileUrl = "http://localhost:8080/uploads/ktp_new_profile_" + user.getId() + ".jpg";
    when(storageService.uploadFile(any(), eq("ktp"))).thenReturn(mockFileUrl);

    byte[] jpegContent =
        new byte[] {
          (byte) 0xFF,
          (byte) 0xD8,
          (byte) 0xFF,
          (byte) 0xE0,
          0x00,
          0x10,
          0x4A,
          0x46,
          0x49,
          0x46,
          0x00,
          0x01
        };

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "ktp_new_profile.jpg", MediaType.IMAGE_JPEG_VALUE, jpegContent);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(file)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    // Verify profile was created
    var profileOpt = userProfileRepository.findById(user.getId());
    assertTrue(profileOpt.isPresent(), "User profile should be created");
  }

  @Test
  @DisplayName("Should upload file via StorageService mock")
  void storageService_UploadFile_ShouldReturnUrl() throws Exception {
    User user = createTestUser();
    String token = getAuthToken();

    String expectedUrl = "http://localhost:8080/uploads/mock_storage_test.jpg";
    when(storageService.uploadFile(any(), eq("ktp"))).thenReturn(expectedUrl);

    byte[] jpegContent =
        new byte[] {
          (byte) 0xFF,
          (byte) 0xD8,
          (byte) 0xFF,
          (byte) 0xE0,
          0x00,
          0x10,
          0x4A,
          0x46,
          0x49,
          0x46,
          0x00,
          0x01
        };

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "mock_storage_test.jpg", MediaType.IMAGE_JPEG_VALUE, jpegContent);

    mockMvc
        .perform(
            multipart("/api/user-profiles/upload-ktp")
                .file(file)
                .with(csrf())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.fileDownloadUri").value(expectedUrl));
  }
}
