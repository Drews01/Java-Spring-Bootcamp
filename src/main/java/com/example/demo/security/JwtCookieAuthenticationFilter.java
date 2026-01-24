package com.example.demo.security;

import com.example.demo.service.TokenBlacklistService;
import com.example.demo.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter that reads JWT from cookies. Falls back to Authorization header for
 * backward compatibility.
 *
 * <p>Processing order: 1. Check for JWT in HttpOnly cookie 2. If not found, check Authorization:
 * Bearer header 3. Validate token and set SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;
  private final TokenBlacklistService tokenBlacklistService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Skip filter for public endpoints
    final String requestPath = request.getServletPath();
    if (isPublicEndpoint(requestPath)) {
      filterChain.doFilter(request, response);
      return;
    }

    // Try to extract JWT from cookie first, then fall back to Authorization header
    Optional<String> jwtFromCookie = CookieUtil.extractJwtFromCookie(request);
    String jwt = jwtFromCookie.orElseGet(() -> extractJwtFromHeader(request));

    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Check if token is blacklisted
      if (tokenBlacklistService.isBlacklisted(jwt)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"Token has been revoked\", \"status\": 401}");
        return;
      }

      final String username = jwtService.extractUsername(jwt);

      // If token is valid and user is not already authenticated
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (io.jsonwebtoken.JwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\": \"Invalid or malformed token\", \"status\": 401}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  /** Checks if the request path is a public endpoint that doesn't require authentication. */
  private boolean isPublicEndpoint(String path) {
    return path.equals("/auth/login")
        || path.equals("/auth/register")
        || path.equals("/auth/forgot-password")
        || path.equals("/auth/reset-password")
        || path.startsWith("/api/products")
        || path.startsWith("/uploads")
        || path.equals("/api/csrf-token");
  }

  /** Extracts JWT from Authorization header (Bearer token). */
  private String extractJwtFromHeader(HttpServletRequest request) {
    final String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}
