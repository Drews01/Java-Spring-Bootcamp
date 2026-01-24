package com.example.demo.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.ResponseCookie;

/**
 * Utility class for creating and managing HttpOnly cookies. Used for secure JWT storage in cookies
 * instead of localStorage.
 */
public final class CookieUtil {

  public static final String JWT_COOKIE_NAME = "jwt";
  public static final String COOKIE_PATH = "/";

  private CookieUtil() {
    // Utility class - prevent instantiation
  }

  /**
   * Creates an HttpOnly, Secure, SameSite cookie for JWT storage.
   *
   * @param token JWT token value
   * @param maxAgeSeconds cookie expiration in seconds
   * @param secure whether to set Secure flag (true for HTTPS, false for local dev)
   * @return ResponseCookie configured with security attributes
   */
  public static ResponseCookie createJwtCookie(String token, long maxAgeSeconds, boolean secure) {
    return ResponseCookie.from(JWT_COOKIE_NAME, token)
        .httpOnly(true) // Prevents JavaScript access (XSS protection)
        .secure(secure) // Only sent over HTTPS when true
        .sameSite("Lax") // CSRF protection for unsafe methods
        .path(COOKIE_PATH) // Available for all paths
        .maxAge(maxAgeSeconds)
        .build();
  }

  /**
   * Creates a cookie to clear the JWT (for logout). Sets Max-Age to 0 to immediately expire the
   * cookie.
   *
   * @param secure whether to set Secure flag
   * @return ResponseCookie that clears the JWT cookie
   */
  public static ResponseCookie clearJwtCookie(boolean secure) {
    return ResponseCookie.from(JWT_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(secure)
        .sameSite("Lax")
        .path(COOKIE_PATH)
        .maxAge(0) // Immediately expires the cookie
        .build();
  }

  /**
   * Extracts a cookie value from the request by name.
   *
   * @param request HttpServletRequest
   * @param cookieName name of the cookie to extract
   * @return Optional containing cookie value if found
   */
  public static Optional<String> extractCookieValue(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return Arrays.stream(cookies)
        .filter(cookie -> cookieName.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  /**
   * Extracts JWT token from the request cookie.
   *
   * @param request HttpServletRequest
   * @return Optional containing JWT token if found
   */
  public static Optional<String> extractJwtFromCookie(HttpServletRequest request) {
    return extractCookieValue(request, JWT_COOKIE_NAME);
  }
}
