package com.example.demo.service;

import com.example.demo.dto.UploadImageResponse;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final UserRepository userRepository;
  private final FileValidationService fileValidationService;
  private final LoanEligibilityService loanEligibilityService;
  private final StorageService storageService;

  @Transactional
  public UserProfileDTO createUserProfile(Long userId, UserProfileDTO dto) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

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

    // Auto-assign Bronze tier if profile is complete
    if (isProfileComplete(userId)) {
      loanEligibilityService.assignDefaultProduct(userId);
    }

    return convertToDTO(saved);
  }

  @Transactional(readOnly = true)
  public UserProfileDTO getUserProfile(Long userId) {
    UserProfile userProfile = userProfileRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("UserProfile not found for user id: " + userId));
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
            () -> new ResourceNotFoundException("UserProfile not found for user id: " + userId));

    userProfile.setAddress(dto.getAddress());
    userProfile.setNik(dto.getNik());
    if (dto.getKtpPath() != null) {
      userProfile.setKtpPath(dto.getKtpPath());
    }
    userProfile.setPhoneNumber(dto.getPhoneNumber());
    userProfile.setAccountNumber(dto.getAccountNumber());
    userProfile.setBankName(dto.getBankName());

    UserProfile updated = userProfileRepository.save(userProfile);

    // Auto-assign Bronze tier if profile becomes complete after update
    if (isProfileComplete(userId)) {
      loanEligibilityService.assignDefaultProduct(userId);
    }

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

    // Upload to storage (local or R2 depending on configuration)
    String fileUrl = storageService.uploadFile(file, "ktp");

    // Update UserProfile
    UserProfile userProfile = userProfileRepository.findById(userId)
        .orElseGet(() -> {
          User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
          return userProfileRepository.save(UserProfile.builder().user(user).build());
        });

    userProfile.setKtpPath(fileUrl);
    userProfileRepository.save(userProfile);

    return UploadImageResponse.builder()
        .fileName(file.getOriginalFilename())
        .fileDownloadUri(fileUrl)
        .fileType(file.getContentType())
        .size(file.getSize())
        .build();
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
    // ktpPath now stores the full URL (R2 public URL or local URL)
    String ktpUrl = userProfile.getKtpPath();

    return UserProfileDTO.builder()
        .username(userProfile.getUser().getUsername())
        .email(userProfile.getUser().getEmail())
        .address(userProfile.getAddress())
        .nik(userProfile.getNik())
        .ktpPath(ktpUrl)
        .phoneNumber(userProfile.getPhoneNumber())
        .accountNumber(userProfile.getAccountNumber())
        .bankName(userProfile.getBankName())
        .updatedAt(userProfile.getUpdatedAt())
        .build();
  }
}
