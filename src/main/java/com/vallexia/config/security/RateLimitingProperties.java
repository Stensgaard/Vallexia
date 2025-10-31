package com.vallexia.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for rate limiting settings.
 * Allows configuration of rate limits per endpoint type and enables/disables
 * rate limiting globally or per endpoint.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@ConfigurationProperties(prefix = "app.rate-limiting")
public class RateLimitingProperties {
  
  /**
   * Global flag to enable/disable all rate limiting.
   * When false, rate limiting is completely bypassed.
   * Default: true
   */
  private boolean enabled = true;
  
  /**
   * Rate limiting configuration for login endpoint.
   */
  private LoginConfig login = new LoginConfig();
  
  /**
   * Rate limiting configuration for registration endpoint.
   */
  private RegistrationConfig registration = new RegistrationConfig();
  
  /**
   * Rate limiting configuration for general API endpoints.
   */
  private GeneralApiConfig generalApi = new GeneralApiConfig();
  
  /**
   * Rate limiting configuration for refresh token endpoint.
   */
  private RefreshConfig refresh = new RefreshConfig();
  
  /**
   * Configuration for login endpoint rate limiting.
   */
  @Data
  public static class LoginConfig {
    /**
     * Enable/disable rate limiting for login endpoint.
     * Default: true
     */
    private boolean enabled = true;
    
    /**
     * Number of requests allowed per time window.
     * Default: 5
     */
    private int requests = 5;
    
    /**
     * Time window duration in minutes.
     * Default: 1
     */
    private int durationMinutes = 1;
  }
  
  /**
   * Configuration for registration endpoint rate limiting.
   */
  @Data
  public static class RegistrationConfig {
    /**
     * Enable/disable rate limiting for registration endpoint.
     * Default: true
     */
    private boolean enabled = true;
    
    /**
     * Number of requests allowed per time window.
     * Default: 3
     */
    private int requests = 3;
    
    /**
     * Time window duration in minutes.
     * Default: 5
     */
    private int durationMinutes = 5;
  }
  
  /**
   * Configuration for general API endpoints rate limiting.
   */
  @Data
  public static class GeneralApiConfig {
    /**
     * Enable/disable rate limiting for general API endpoints.
     * Default: true
     */
    private boolean enabled = true;
    
    /**
     * Number of requests allowed per time window.
     * Default: 100
     */
    private int requests = 100;
    
    /**
     * Time window duration in minutes.
     * Default: 1
     */
    private int durationMinutes = 1;
  }
  
  /**
   * Configuration for refresh token endpoint rate limiting.
   */
  @Data
  public static class RefreshConfig {
    /**
     * Enable/disable rate limiting for refresh token endpoint.
     * Default: true
     */
    private boolean enabled = true;
    
    /**
     * Number of requests allowed per time window.
     * Default: 10
     */
    private int requests = 10;
    
    /**
     * Time window duration in minutes.
     * Default: 5
     */
    private int durationMinutes = 5;
  }
}
