package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authentication Controller Handles user registration and login endpoints */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /** Register a new user POST /auth/register */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    AuthResponse authResponse = authService.register(request);
    return ResponseUtil.created(authResponse, "User registered successfully");
  }

  /** Login user POST /auth/login */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
    AuthResponse authResponse = authService.login(request);
    return ResponseUtil.ok(authResponse, "Login successful");
  }

  /** Logout user POST /auth/logout */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(jakarta.servlet.http.HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      authService.logout(token);
    }
    return ResponseUtil.ok(null, "Logout successful");
  }

  /** Initiate password reset POST /auth/forgot-password */
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request.getEmail());
    return ResponseUtil.ok(null, "Password reset link sent to email");
  }

  /** Reset password POST /auth/reset-password */
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.getToken(), request.getNewPassword());
    return ResponseUtil.ok(null, "Password reset successfully");
  }

  /** Refresh access token POST /auth/refresh */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody com.example.demo.dto.RefreshTokenRequest request) {
    AuthResponse authResponse = authService.refreshAccessToken(request.refreshToken());
    return ResponseUtil.ok(authResponse, "Token refreshed successfully");
  }
}
