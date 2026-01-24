package com.example.demo.dto;

/** DTO for CSRF token response. Provides the token and header name for SPA frontend integration. */
public record CsrfTokenDTO(String token, String headerName) {}
