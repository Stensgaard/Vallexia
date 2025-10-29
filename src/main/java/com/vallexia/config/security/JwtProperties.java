package com.vallexia.config.security;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT settings.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
  
  /**
   * JWT secret key for signing tokens.
   */
  private String secret;
  
  /**
   * Access token expiration time in milliseconds.
   */
  private long accessTokenExpiration;
  
  /**
   * Refresh token expiration time in milliseconds.
   */
  private long refreshTokenExpiration;
  
  /**
   * Validate JWT secret key size after properties are loaded.
   * Ensures secret is at least 256 bits (32 characters) for security.
   */
  @PostConstruct
  public void validateSecret() {
    if (secret == null || secret.trim().isEmpty()) {
      throw new IllegalStateException(
          "JWT secret is not configured. Set JWT_SECRET environment variable.");
    }
    
    if (secret.length() < 32) {
      throw new IllegalStateException(
          "JWT secret must be at least 32 characters long for security. Current length: "
              + secret.length());
    }
    
    log.info("JWT secret validation passed");
  }
}
