package com.example.demo.dto;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(
    String token,
    String tokenType,
    Instant expiresAt,
    String username,
    String email,
    Set<String> roles) {}
