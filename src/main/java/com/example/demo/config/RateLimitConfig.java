package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for rate limiting. Configurable via application.yml using the
 * 'app.rate-limit' prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitConfig {

  private EndpointLimit login = new EndpointLimit(5, 60);
  private EndpointLimit register = new EndpointLimit(3, 60);
  private EndpointLimit forgotPassword = new EndpointLimit(2, 60);

  public EndpointLimit getLogin() {
    return login;
  }

  public void setLogin(EndpointLimit login) {
    this.login = login;
  }

  public EndpointLimit getRegister() {
    return register;
  }

  public void setRegister(EndpointLimit register) {
    this.register = register;
  }

  public EndpointLimit getForgotPassword() {
    return forgotPassword;
  }

  public void setForgotPassword(EndpointLimit forgotPassword) {
    this.forgotPassword = forgotPassword;
  }

  /** Represents rate limit configuration for a single endpoint. */
  public static class EndpointLimit {
    private int maxAttempts;
    private int durationSeconds;

    public EndpointLimit() {}

    public EndpointLimit(int maxAttempts, int durationSeconds) {
      this.maxAttempts = maxAttempts;
      this.durationSeconds = durationSeconds;
    }

    public int getMaxAttempts() {
      return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
      this.maxAttempts = maxAttempts;
    }

    public int getDurationSeconds() {
      return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
      this.durationSeconds = durationSeconds;
    }
  }
}
