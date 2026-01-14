package com.example.demo.config;

import com.example.demo.security.DynamicAuthorizationManager;
import com.example.demo.security.JwtCookieAuthenticationFilter;
import com.example.demo.security.SpaCsrfTokenRequestHandler;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security Configuration.
 * Configures JWT-based authentication with HttpOnly cookies and CSRF
 * protection.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;
  private final DynamicAuthorizationManager dynamicAuthorizationManager;

  @Value("${app.cors.allowed-origins:http://localhost:4200}")
  private String allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // CSRF Protection with Cookie-based token repository
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            .ignoringRequestMatchers(
                "/auth/login",
                "/auth/register",
                "/auth/forgot-password",
                "/auth/reset-password",
                "/api/user-profiles/upload-ktp"))
        // CORS Configuration
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // Authorization rules
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/auth/login",
                "/auth/register",
                "/auth/forgot-password",
                "/auth/reset-password",
                "/auth/logout",
                "/api/csrf-token")
            .permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/api/products/**").permitAll()
            .requestMatchers("/uploads/**").permitAll()
            .anyRequest().access(dynamicAuthorizationManager))
        // Stateless session management
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // JWT Cookie Authentication Filter
        .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * CORS configuration for frontend applications.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true); // Required for cookies
    configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "X-XSRF-TOKEN"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
