package com.vallexia.config.security;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for authentication lockout settings.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.auth.lockout")
public class AccountSecurityProperties {
  
  /**
   * Maximum number of failed login attempts before account lockout.
   * Default: 5
   */
  @Min(value = 1, message = "Maximum failed attempts must be at least 1")
  private int maxFailedAttempts = 5;
  
  /**
   * Account lockout duration in minutes.
   * Default: 15 minutes
   */
  @Min(value = 1, message = "Lockout duration must be at least 1 minute")
  private int durationMinutes = 15;
}
