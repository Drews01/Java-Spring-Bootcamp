package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing access token.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required") String refreshToken) {
}
