package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserProfileDTO>> createUserProfile(@RequestBody UserProfileDTO dto) {
        UserProfileDTO created = userProfileService.createUserProfile(dto);
        return ResponseUtil.created(created, "User profile created successfully");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(@PathVariable Long userId) {
        UserProfileDTO userProfile = userProfileService.getUserProfile(userId);
        return ResponseUtil.ok(userProfile, "User profile retrieved successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileDTO>>> getAllUserProfiles() {
        List<UserProfileDTO> userProfiles = userProfileService.getAllUserProfiles();
        return ResponseUtil.ok(userProfiles, "User profiles retrieved successfully");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileDTO dto) {
        UserProfileDTO updated = userProfileService.updateUserProfile(userId, dto);
        return ResponseUtil.ok(updated, "User profile updated successfully");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserProfile(@PathVariable Long userId) {
        userProfileService.deleteUserProfile(userId);
        return ResponseUtil.okMessage("User profile deleted successfully");
    }
}
