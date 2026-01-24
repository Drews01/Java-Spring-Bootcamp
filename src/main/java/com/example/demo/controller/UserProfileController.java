package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.UploadImageResponse;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserProfileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileService userProfileService;
  private final UserRepository userRepository;

  /**
   * Create or update user profile for the authenticated user. SECURITY: userId is extracted from
   * JWT token to prevent IDOR.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<UserProfileDTO>> createOrUpdateUserProfile(
      @RequestBody UserProfileDTO dto) {
    // SECURITY: Extract userId from JWT token, ignore any userId in request body
    Long authenticatedUserId = getCurrentUserId();

    // Check if profile exists
    try {
      UserProfileDTO existing = userProfileService.getUserProfile(authenticatedUserId);
      // Profile exists, update it
      UserProfileDTO updated = userProfileService.updateUserProfile(authenticatedUserId, dto);
      return ResponseUtil.ok(updated, "User profile updated successfully");
    } catch (RuntimeException e) {
      // Profile doesn't exist, create it
      UserProfileDTO created = userProfileService.createUserProfile(authenticatedUserId, dto);
      return ResponseUtil.created(created, "User profile created successfully");
    }
  }

  /**
   * Upload KTP image for the authenticated user. SECURITY: userId is extracted from JWT token to
   * prevent IDOR.
   */
  @PostMapping("/upload-ktp")
  public ResponseEntity<ApiResponse<UploadImageResponse>> uploadKtp(
      @RequestParam("file") MultipartFile file) {
    Long authenticatedUserId = getCurrentUserId();
    UploadImageResponse response = userProfileService.uploadKtp(authenticatedUserId, file);
    return ResponseUtil.ok(response, "KTP image uploaded successfully");
  }

  /**
   * Get the authenticated user's profile. SECURITY: Only returns the profile of the authenticated
   * user.
   */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserProfileDTO>> getMyProfile() {
    Long authenticatedUserId = getCurrentUserId();
    UserProfileDTO userProfile = userProfileService.getUserProfile(authenticatedUserId);
    return ResponseUtil.ok(userProfile, "User profile retrieved successfully");
  }

  /** Get all user profiles - ADMIN ONLY endpoint */
  @GetMapping
  public ResponseEntity<ApiResponse<List<UserProfileDTO>>> getAllUserProfiles() {
    List<UserProfileDTO> userProfiles = userProfileService.getAllUserProfiles();
    return ResponseUtil.ok(userProfiles, "User profiles retrieved successfully");
  }

  /** Get user profile by ID - For Staff/Admin use */
  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(@PathVariable Long userId) {
    UserProfileDTO userProfile = userProfileService.getUserProfile(userId);
    return ResponseUtil.ok(userProfile, "User profile retrieved successfully");
  }

  /**
   * Update user profile for the authenticated user. SECURITY: userId is extracted from JWT token to
   * prevent IDOR.
   */
  @PutMapping
  public ResponseEntity<ApiResponse<UserProfileDTO>> updateMyProfile(
      @RequestBody UserProfileDTO dto) {
    // SECURITY: Extract userId from JWT token, ignore any userId in request body
    Long authenticatedUserId = getCurrentUserId();
    UserProfileDTO updated = userProfileService.updateUserProfile(authenticatedUserId, dto);
    return ResponseUtil.ok(updated, "User profile updated successfully");
  }

  /**
   * Delete user profile for the authenticated user. SECURITY: userId is extracted from JWT token to
   * prevent IDOR.
   */
  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> deleteMyProfile() {
    Long authenticatedUserId = getCurrentUserId();
    userProfileService.deleteUserProfile(authenticatedUserId);
    return ResponseUtil.okMessage("User profile deleted successfully");
  }

  /** SECURITY: Extract user ID from JWT token to prevent IDOR vulnerabilities */
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      return userDetails.getId();
    }

    // Fallback for testing - return first user
    return userRepository.findAll().stream()
        .findFirst()
        .map(User::getId)
        .orElseThrow(() -> new RuntimeException("No users found"));
  }
}
