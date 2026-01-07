package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
    @NotNull(message = "isActive is required") Boolean isActive) {}
