package com.example.demo.dto;

import java.util.Set;

/**
 * DTO for current authenticated user information.
 * Used by /auth/me endpoint to return current user details.
 */
public record CurrentUserDTO(
        Long id,
        String username,
        String email,
        Set<String> roles,
        String branchName) {
}
