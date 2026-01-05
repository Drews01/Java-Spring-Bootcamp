package com.example.demo.security;

import com.example.demo.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** JWT Authentication Filter Intercepts every HTTP request to validate JWT tokens */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;
  private final TokenBlacklistService tokenBlacklistService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Skip filter for login and register endpoints
    final String requestPath = request.getServletPath();
    if (requestPath.equals("/auth/login") || requestPath.equals("/auth/register")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract JWT token from Authorization header
    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwt = authHeader.substring(7); // Remove "Bearer " prefix

    try {
      // Check if token is blacklisted
      if (tokenBlacklistService.isBlacklisted(jwt)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
      // Log and return 401 for any JWT related issues (malformed, expired, etc.)
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\": \"Invalid or malformed token\", \"status\": 401}");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
