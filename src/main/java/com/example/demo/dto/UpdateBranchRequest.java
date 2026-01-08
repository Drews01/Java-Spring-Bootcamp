package com.example.demo.dto;

import jakarta.validation.constraints.Size;

public record UpdateBranchRequest(
    @Size(min = 2, max = 100, message = "Branch name must be between 2 and 100 characters")
        String name,
    @Size(max = 255, message = "Address must not exceed 255 characters") String address,
    Boolean isActive) {}
