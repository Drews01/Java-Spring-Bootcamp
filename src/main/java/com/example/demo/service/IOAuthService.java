package com.example.demo.service;

import com.example.demo.dto.AuthResponse;

/**
 * OAuth Service Interface.
 *
 * <p>Defines the contract for OAuth authentication operations including:
 *
 * <ul>
 *   <li>Google OAuth 2.0 authentication
 * </ul>
 *
 * @author Java Spring Bootcamp
 * @version 1.0
 */
public interface IOAuthService {

  /**
   * Authenticates a user using Google ID Token.
   *
   * @param idTokenString the Google ID token string
   * @return AuthResponse containing JWT tokens and user information
   */
  AuthResponse loginWithGoogle(String idTokenString);
}
