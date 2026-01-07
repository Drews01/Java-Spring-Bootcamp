package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AdminCreateUserRequest(
    @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
        String username,
    @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
    Set<String> roleNames) {}
