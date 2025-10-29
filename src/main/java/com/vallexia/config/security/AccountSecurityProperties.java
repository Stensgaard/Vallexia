package com.vallexia.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for authentication lockout settings.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@ConfigurationProperties(prefix = "app.auth.lockout")
public class AccountSecurityProperties {
  
  /**
   * Maximum number of failed login attempts before account lockout.
   * Default: 5
   */
  private int maxFailedAttempts = 5;
  
  /**
   * Account lockout duration in minutes.
   * Default: 15 minutes
   */
  private int durationMinutes = 15;
}
