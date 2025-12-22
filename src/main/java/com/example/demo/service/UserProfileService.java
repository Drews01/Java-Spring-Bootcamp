package com.example.demo.service;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProfile;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserProfileDTO createUserProfile(UserProfileDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .address(dto.getAddress())
                .nik(dto.getNik())
                .ktpPath(dto.getKtpPath())
                .phoneNumber(dto.getPhoneNumber())
                .build();

        UserProfile saved = userProfileRepository.save(userProfile);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for user id: " + userId));
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
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for user id: " + userId));

        userProfile.setAddress(dto.getAddress());
        userProfile.setNik(dto.getNik());
        userProfile.setKtpPath(dto.getKtpPath());
        userProfile.setPhoneNumber(dto.getPhoneNumber());

        UserProfile updated = userProfileRepository.save(userProfile);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteUserProfile(Long userId) {
        userProfileRepository.deleteById(userId);
    }

    private UserProfileDTO convertToDTO(UserProfile userProfile) {
        return UserProfileDTO.builder()
                .userId(userProfile.getUserId())
                .address(userProfile.getAddress())
                .nik(userProfile.getNik())
                .ktpPath(userProfile.getKtpPath())
                .phoneNumber(userProfile.getPhoneNumber())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }
}
