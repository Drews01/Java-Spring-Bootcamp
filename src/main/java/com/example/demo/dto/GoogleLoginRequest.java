package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "ID Token is required") String idToken,
    String platform // optional, for FCM if needed later
    ) {}
