package com.example.demo.dto;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(
    String token,
    String refreshToken,
    String tokenType,
    Instant expiresAt,
    Instant refreshExpiresAt,
    String username,
    String email,
    Set<String> roles) {}
