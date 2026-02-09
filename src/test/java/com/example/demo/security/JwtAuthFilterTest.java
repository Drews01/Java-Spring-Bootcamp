package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Unit tests for {@link JwtAuthFilter}.
 *
 * <p>Tests JWT token validation and authentication filter behavior.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock private JwtService jwtService;

  @Mock private CustomUserDetailsService userDetailsService;

  @Mock private TokenBlacklistService tokenBlacklistService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtAuthFilter jwtAuthFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Should skip filter for login endpoint")
  void doFilterInternal_shouldSkipForLoginEndpoint() throws Exception {
    // Given
    when(request.getServletPath()).thenReturn("/auth/login");

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).extractUsername(anyString());
  }

  @Test
  @DisplayName("Should skip filter for register endpoint")
  void doFilterInternal_shouldSkipForRegisterEndpoint() throws Exception {
    // Given
    when(request.getServletPath()).thenReturn("/auth/register");

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).extractUsername(anyString());
  }

  @Test
  @DisplayName("Should skip filter when no Authorization header")
  void doFilterInternal_shouldSkipWhenNoAuthHeader() throws Exception {
    // Given
    when(request.getServletPath()).thenReturn("/api/loans");
    when(request.getHeader("Authorization")).thenReturn(null);

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).extractUsername(anyString());
  }

  @Test
  @DisplayName("Should skip filter when Authorization header does not start with Bearer")
  void doFilterInternal_shouldSkipWhenNotBearerToken() throws Exception {
    // Given
    when(request.getServletPath()).thenReturn("/api/loans");
    when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).extractUsername(anyString());
  }

  @Test
  @DisplayName("Should return 401 when token is blacklisted")
  void doFilterInternal_shouldReturn401WhenTokenBlacklisted() throws Exception {
    // Given
    when(request.getServletPath()).thenReturn("/api/loans");
    when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
    when(tokenBlacklistService.isBlacklisted("validtoken123")).thenReturn(true);

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("Should authenticate user with valid token")
  void doFilterInternal_shouldAuthenticateWithValidToken() throws Exception {
    // Given
    String token = "validtoken123";
    String username = "testuser";

    when(request.getServletPath()).thenReturn("/api/loans");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
    when(jwtService.extractUsername(token)).thenReturn(username);

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .isEqualTo(userDetails);
  }

  @Test
  @DisplayName("Should not authenticate when token is invalid")
  void doFilterInternal_shouldNotAuthenticateWithInvalidToken() throws Exception {
    // Given
    String token = "invalidtoken123";
    String username = "testuser";

    when(request.getServletPath()).thenReturn("/api/loans");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
    when(jwtService.extractUsername(token)).thenReturn(username);

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("Should return 401 for malformed JWT")
  void doFilterInternal_shouldReturn401ForMalformedJwt() throws Exception {
    // Given
    String token = "malformed.jwt.token";

    when(request.getServletPath()).thenReturn("/api/loans");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
    when(jwtService.extractUsername(token))
        .thenThrow(new io.jsonwebtoken.MalformedJwtException("Invalid JWT"));

    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);

    // When
    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType("application/json");
    verify(filterChain, never()).doFilter(request, response);
  }
}
