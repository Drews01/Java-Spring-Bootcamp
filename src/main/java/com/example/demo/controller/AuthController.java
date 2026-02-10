package com.example.demo.controller;

import com.example.demo.base.ApiResponse;
import com.example.demo.base.ResponseUtil;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.CurrentUserDTO;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.entity.Role;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.AuthService;
import com.example.demo.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication Controller. Handles user registration, login, logout, and current user endpoints.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;

  @Value("${app.security.jwt.expiration-seconds:3600}")
  private long jwtExpirationSeconds;

  @Value("${app.security.cookie.secure:false}")
  private boolean secureCookie;

  /** Register a new user POST /auth/register */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
    AuthResponse authResponse = authService.register(request);
    // Set JWT in HttpOnly cookie
    ResponseCookie jwtCookie =
        CookieUtil.createJwtCookie(authResponse.token(), jwtExpirationSeconds, secureCookie);
    response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
    return ResponseUtil.created(authResponse, "User registered successfully");
  }

  /** Login user POST /auth/login */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody AuthRequest request, HttpServletResponse response) {
    AuthResponse authResponse = authService.login(request);
    // Set JWT in HttpOnly cookie
    ResponseCookie jwtCookie =
        CookieUtil.createJwtCookie(authResponse.token(), jwtExpirationSeconds, secureCookie);
    response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
    return ResponseUtil.ok(authResponse, "Login successful");
  }

  /** Login with Google POST /auth/google */
  @PostMapping("/google")
  public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
      @Valid @RequestBody com.example.demo.dto.GoogleLoginRequest request,
      HttpServletResponse response) {
    try {
      AuthResponse authResponse = authService.loginWithGoogle(request.idToken());
      // Set JWT in HttpOnly cookie
      ResponseCookie jwtCookie =
          CookieUtil.createJwtCookie(authResponse.token(), jwtExpirationSeconds, secureCookie);
      response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
      return ResponseUtil.ok(authResponse, "Google Login successful");
    } catch (Exception e) {
      log.error("Google login failed: {}", e.getMessage(), e);
      return ResponseUtil.error(
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
          "Google Login Error: " + e.getMessage());
    }
  }

  /** Logout user POST /auth/logout */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      HttpServletRequest request, HttpServletResponse response) {
    // Try to get token from cookie first, then from header
    String token =
        CookieUtil.extractJwtFromCookie(request)
            .orElseGet(
                () -> {
                  String authHeader = request.getHeader("Authorization");
                  if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                  }
                  return null;
                });

    if (token != null) {
      authService.logout(token);
    }

    // Clear JWT cookie
    ResponseCookie clearCookie = CookieUtil.clearJwtCookie(secureCookie);
    response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

    return ResponseUtil.ok(null, "Logout successful");
  }

  /** Get current authenticated user. GET /auth/me */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<CurrentUserDTO>> getCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      return ResponseUtil.error(
          org.springframework.http.HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    CurrentUserDTO currentUser =
        new CurrentUserDTO(
            userDetails.getUser().getId(),
            userDetails.getUser().getUsername(),
            userDetails.getUser().getEmail(),
            userDetails.getUser().getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()),
            userDetails.getUser().getBranch() != null
                ? userDetails.getUser().getBranch().getName()
                : null);

    return ResponseUtil.ok(currentUser, "Current user retrieved successfully");
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
      @Valid @RequestBody com.example.demo.dto.RefreshTokenRequest request,
      HttpServletResponse response) {
    AuthResponse authResponse = authService.refreshAccessToken(request.refreshToken());
    // Set new JWT in HttpOnly cookie
    ResponseCookie jwtCookie =
        CookieUtil.createJwtCookie(authResponse.token(), jwtExpirationSeconds, secureCookie);
    response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
    return ResponseUtil.ok(authResponse, "Token refreshed successfully");
  }
}
