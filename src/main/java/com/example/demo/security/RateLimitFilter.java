package com.example.demo.security;

import com.example.demo.service.RateLimitingService;
import com.example.demo.service.RateLimitingService.Endpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that applies rate limiting to authentication endpoints. Returns 429 Too Many Requests when
 * rate limit is exceeded.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitingService rateLimitingService;
  private final ObjectMapper objectMapper;

  @org.springframework.beans.factory.annotation.Value("${app.rate-limit.enabled:true}")
  private boolean rateLimitEnabled;

  private static final String LOGIN_PATH = "/auth/login";
  private static final String REGISTER_PATH = "/auth/register";
  private static final String FORGOT_PASSWORD_PATH = "/auth/forgot-password";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!rateLimitEnabled) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getRequestURI();
    String method = request.getMethod();

    // Only apply rate limiting to POST requests on auth endpoints
    if (!"POST".equalsIgnoreCase(method)) {
      filterChain.doFilter(request, response);
      return;
    }

    Endpoint endpoint = getEndpointForPath(path);
    if (endpoint == null) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientIp = getClientIp(request);

    if (!rateLimitingService.tryConsume(clientIp, endpoint)) {
      log.warn("Rate limit exceeded for IP {} on path {}", clientIp, path);
      sendRateLimitExceededResponse(response, clientIp, endpoint);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private Endpoint getEndpointForPath(String path) {
    if (path.equals(LOGIN_PATH)) {
      return Endpoint.LOGIN;
    } else if (path.equals(REGISTER_PATH)) {
      return Endpoint.REGISTER;
    } else if (path.equals(FORGOT_PASSWORD_PATH)) {
      return Endpoint.FORGOT_PASSWORD;
    }
    return null;
  }

  /**
   * Extracts the client IP address from the request. Supports X-Forwarded-For header for requests
   * behind proxies/load balancers.
   */
  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // X-Forwarded-For can contain multiple IPs; the first one is the original
      // client
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void sendRateLimitExceededResponse(
      HttpServletResponse response, String clientIp, Endpoint endpoint) throws IOException {
    long retryAfter = rateLimitingService.getSecondsUntilRefill(clientIp, endpoint);

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader("Retry-After", String.valueOf(retryAfter));

    Map<String, Object> errorBody = new LinkedHashMap<>();
    errorBody.put("timestamp", LocalDateTime.now().toString());
    errorBody.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
    errorBody.put("error", "Too Many Requests");
    errorBody.put(
        "message", "Rate limit exceeded. Please try again after " + retryAfter + " seconds.");
    errorBody.put("retryAfterSeconds", retryAfter);

    response.getWriter().write(objectMapper.writeValueAsString(errorBody));
  }
}
