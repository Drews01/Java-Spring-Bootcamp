package com.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

/** /** Custom Access Denied Handler to return JSON 403 response with detailed error message. */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");

    Map<String, Object> data = new HashMap<>();
    data.put("timestamp", java.time.Instant.now().toString());
    data.put("status", HttpServletResponse.SC_FORBIDDEN);
    data.put("error", "Forbidden");
    data.put("path", request.getRequestURI());

    // Check if it's a CSRF error
    if (accessDeniedException instanceof CsrfException) {
      data.put("message", "Invalid or missing CSRF Token");
    } else {
      data.put("message", "Access Denied: " + accessDeniedException.getMessage());
    }

    response.getWriter().write(objectMapper.writeValueAsString(data));
  }
}
