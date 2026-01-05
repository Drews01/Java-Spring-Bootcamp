package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${app.security.jwt.secret}")
  private String jwtSecret;

  @Value("${app.security.jwt.expiration-seconds:3600}")
  private long jwtExpirationSeconds;

  @Value("${app.security.jwt.refresh-expiration-seconds:604800}")
  private long refreshExpirationSeconds;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractIssuedAt(String token) {
    return extractClaim(token, Claims::getIssuedAt);
  }

  public Instant extractExpirationInstant(String token) {
    return extractClaim(token, Claims::getExpiration).toInstant();
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpirationSeconds);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(Map.of("type", "access"), userDetails);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(Map.of("type", "refresh"), userDetails, refreshExpirationSeconds);
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(expiration)))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);

    boolean isUsernameValid = username.equals(userDetails.getUsername());
    boolean isExpired = isTokenExpired(token);

    if (!isUsernameValid || isExpired) {
      return false;
    }

    // New: Check token type if present
    try {
      String type = extractClaim(token, claims -> claims.get("type", String.class));
      if ("refresh".equals(type)) {
        return false; // Refresh tokens cannot be used as access tokens
      }
    } catch (Exception e) {
      // Ignore if type claim is missing (legacy tokens)
    }

    // Check if token was issued before the last password reset
    if (userDetails instanceof CustomUserDetails) {
      CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
      if (customUserDetails.getUser().getLastPasswordResetDate() != null) {
        Date issuedAt = extractIssuedAt(token);
        Instant issuedAtInstant = issuedAt.toInstant();
        Instant lastResetInstant = customUserDetails
            .getUser()
            .getLastPasswordResetDate()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant();

        // If issued before reset, token is invalid
        if (issuedAtInstant.isBefore(lastResetInstant)) {
          return false;
        }
      }
    }

    return true;
  }

  public long getRefreshExpirationSeconds() {
    return refreshExpirationSeconds;
  }

  private boolean isTokenExpired(String token) {
    return extractExpirationInstant(token).isBefore(Instant.now());
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
