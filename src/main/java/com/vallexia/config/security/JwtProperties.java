package com.vallexia.config.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for JWT settings.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
  
  /**
   * JWT secret key for signing tokens.
   * Must be at least 32 characters for security (256 bits).
   */
  @NotBlank(message = "JWT secret must not be blank")
  @Size(min = 32, message = "JWT secret must be at least 32 characters long for security")
  private String secret;
  
  /**
   * Access token expiration time in milliseconds.
   */
  @Min(value = 1, message = "Access token expiration must be at least 1 millisecond")
  private long accessTokenExpiration;
  
  /**
   * Refresh token expiration time in milliseconds.
   */
  @Min(value = 1, message = "Refresh token expiration must be at least 1 millisecond")
  private long refreshTokenExpiration;
}
