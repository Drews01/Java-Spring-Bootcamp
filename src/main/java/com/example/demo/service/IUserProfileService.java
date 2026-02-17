package com.example.demo.service;

import com.example.demo.dto.UploadImageResponse;
import com.example.demo.dto.UserProfileDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * User Profile Service Interface.
 *
 * <p>Defines the contract for user profile management operations including:
 *
 * <ul>
 *   <li>User profile CRUD operations
 *   <li>KTP document upload
 *   <li>Profile completeness validation
 * </ul>
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 */
public interface IUserProfileService {

  /**
   * Creates a new user profile.
   *
   * @param userId the user ID
   * @param dto the user profile data
   * @return UserProfileDTO containing the created profile information
   */
  UserProfileDTO createUserProfile(Long userId, UserProfileDTO dto);

  /**
   * Retrieves a user profile by user ID.
   *
   * @param userId the user ID
   * @return UserProfileDTO containing the profile information
   */
  UserProfileDTO getUserProfile(Long userId);

  /**
   * Retrieves all user profiles.
   *
   * @return list of UserProfileDTO
   */
  List<UserProfileDTO> getAllUserProfiles();

  /**
   * Updates an existing user profile.
   *
   * @param userId the user ID
   * @param dto the updated profile data
   * @return UserProfileDTO containing the updated profile information
   */
  UserProfileDTO updateUserProfile(Long userId, UserProfileDTO dto);

  /**
   * Deletes a user profile.
   *
   * @param userId the user ID
   */
  void deleteUserProfile(Long userId);

  /**
   * Uploads a KTP document for a user.
   *
   * @param userId the user ID
   * @param file the KTP image file
   * @return UploadImageResponse containing the upload result
   */
  UploadImageResponse uploadKtp(Long userId, MultipartFile file);

  /**
   * Checks if a user profile is complete with all required fields.
   *
   * @param userId the user ID to check
   * @return true if profile is complete, false otherwise
   */
  boolean isProfileComplete(Long userId);
}
