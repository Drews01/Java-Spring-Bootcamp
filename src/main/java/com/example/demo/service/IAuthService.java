package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;

/**
 * Authentication Service Interface.
 *
 * <p>Defines the contract for authentication operations including:
 *
 * <ul>
 *   <li>User registration with email and password
 *   <li>Local authentication with username/email and password
 *   <li>JWT token refresh
 *   <li>User logout and token invalidation
 * </ul>
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 */
public interface IAuthService {

  /**
   * Registers a new user in the system.
   *
   * @param request the registration request containing username, email, and password
   * @return AuthResponse containing JWT tokens and user information
   */
  AuthResponse register(RegisterRequest request);

  /**
   * Authenticates a user with username/email and password.
   *
   * @param request the login request containing credentials and optional FCM token
   * @return AuthResponse containing JWT tokens and user information
   */
  AuthResponse login(AuthRequest request);

  /**
   * Refreshes the access token using a valid refresh token.
   *
   * @param refreshToken the refresh token
   * @return AuthResponse containing new JWT tokens and user information
   */
  AuthResponse refreshAccessToken(String refreshToken);

  /**
   * Logs out a user by blacklisting their access token.
   *
   * @param token the access token to blacklist
   */
  void logout(String token);

  /**
   * Initiates password reset process by sending reset email.
   *
   * @param email the user's email address
   */
  void forgotPassword(String email);

  /**
   * Resets user password using a valid reset token.
   *
   * @param token the password reset token
   * @param newPassword the new password
   */
  void resetPassword(String token, String newPassword);
}
