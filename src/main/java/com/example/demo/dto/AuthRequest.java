package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "usernameOrEmail is required") String usernameOrEmail,
        @NotBlank(message = "password is required") String password
) {
}
