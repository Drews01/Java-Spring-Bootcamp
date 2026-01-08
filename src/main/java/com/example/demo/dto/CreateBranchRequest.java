package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
    @NotBlank(message = "Branch code is required")
        @Size(min = 2, max = 50, message = "Branch code must be between 2 and 50 characters")
        String code,
    @NotBlank(message = "Branch name is required")
        @Size(min = 2, max = 100, message = "Branch name must be between 2 and 100 characters")
        String name,
    @Size(max = 255, message = "Address must not exceed 255 characters") String address) {}
