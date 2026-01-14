package com.example.demo.service;

import com.example.demo.dto.UploadImageResponse;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final UserRepository userRepository;
  private final FileValidationService fileValidationService;

  @Transactional
  public UserProfileDTO createUserProfile(UserProfileDTO dto) {
    User user = userRepository
        .findById(dto.getUserId())
        .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

    UserProfile userProfile = UserProfile.builder()
        .user(user)
        .address(dto.getAddress())
        .nik(dto.getNik())
        .ktpPath(dto.getKtpPath())
        .phoneNumber(dto.getPhoneNumber())
        .accountNumber(dto.getAccountNumber())
        .bankName(dto.getBankName())
        .build();

    UserProfile saved = userProfileRepository.save(userProfile);
    return convertToDTO(saved);
  }

  @Transactional(readOnly = true)
  public UserProfileDTO getUserProfile(Long userId) {
    UserProfile userProfile = userProfileRepository
        .findById(userId)
        .orElseThrow(
            () -> new RuntimeException("UserProfile not found for user id: " + userId));
    return convertToDTO(userProfile);
  }

  @Transactional(readOnly = true)
  public List<UserProfileDTO> getAllUserProfiles() {
    return userProfileRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public UserProfileDTO updateUserProfile(Long userId, UserProfileDTO dto) {
    UserProfile userProfile = userProfileRepository
        .findById(userId)
        .orElseThrow(
            () -> new RuntimeException("UserProfile not found for user id: " + userId));

    userProfile.setAddress(dto.getAddress());
    userProfile.setNik(dto.getNik());
    if (dto.getKtpPath() != null) {
      userProfile.setKtpPath(dto.getKtpPath());
    }
    userProfile.setPhoneNumber(dto.getPhoneNumber());
    userProfile.setAccountNumber(dto.getAccountNumber());
    userProfile.setBankName(dto.getBankName());

    UserProfile updated = userProfileRepository.save(userProfile);
    return convertToDTO(updated);
  }

  @Transactional
  public void deleteUserProfile(Long userId) {
    userProfileRepository.deleteById(userId);
  }

  @Transactional
  public UploadImageResponse uploadKtp(Long userId, MultipartFile file) {
    if (file.isEmpty()) {
      throw new RuntimeException("Failed to store empty file");
    }

    // Validate File
    fileValidationService.validateFile(file);

    try {
      // Create uploads directory if not exists
      Path uploadLocation = Paths.get("uploads");
      if (!Files.exists(uploadLocation)) {
        Files.createDirectories(uploadLocation);
      }

      // Generate unique filename
      String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
      Path targetPath = uploadLocation.resolve(fileName);

      // Save file
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

      // Update UserProfile
      UserProfile userProfile = userProfileRepository
          .findById(userId)
          .orElseThrow(
              () -> new RuntimeException("UserProfile not found for user id: " + userId));

      userProfile.setKtpPath(targetPath.toString());
      userProfileRepository.save(userProfile);

      String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
          .path("/uploads/")
          .path(fileName)
          .toUriString();

      return UploadImageResponse.builder()
          .fileName(fileName)
          .fileDownloadUri(fileDownloadUri)
          .fileType(file.getContentType())
          .size(file.getSize())
          .build();

    } catch (IOException e) {
      throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
    }
  }

  /**
   * Check if user profile is complete with all required fields.
   *
   * @param userId the user ID to check
   * @return true if profile exists and all required fields are filled, false
   *         otherwise
   */
  @Transactional(readOnly = true)
  public boolean isProfileComplete(Long userId) {
    return userProfileRepository
        .findById(userId)
        .map(
            profile -> isNotBlank(profile.getAddress())
                && isNotBlank(profile.getNik())
                && isNotBlank(profile.getKtpPath())
                && isNotBlank(profile.getPhoneNumber())
                && isNotBlank(profile.getAccountNumber())
                && isNotBlank(profile.getBankName()))
        .orElse(false);
  }

  private boolean isNotBlank(String str) {
    return str != null && !str.trim().isEmpty();
  }

  private UserProfileDTO convertToDTO(UserProfile userProfile) {
    return UserProfileDTO.builder()
        .userId(userProfile.getUserId())
        .address(userProfile.getAddress())
        .nik(userProfile.getNik())
        .ktpPath(userProfile.getKtpPath())
        .phoneNumber(userProfile.getPhoneNumber())
        .accountNumber(userProfile.getAccountNumber())
        .bankName(userProfile.getBankName())
        .updatedAt(userProfile.getUpdatedAt())
        .build();
  }
}
