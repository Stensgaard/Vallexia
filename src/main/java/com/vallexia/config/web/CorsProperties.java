package com.vallexia.config.web;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
  
  /**
   * Allowed origins for CORS.
   */
  private List<String> allowedOrigins;
  
  /**
   * Allowed HTTP methods for CORS.
   */
  private List<String> allowedMethods;
  
  /**
   * Allowed headers for CORS.
   */
  private List<String> allowedHeaders;
  
  /**
   * Whether to allow credentials in CORS requests.
   */
  private boolean allowCredentials;
  
  /**
   * Validates CORS configuration after properties are loaded.
   * Ensures configuration is secure and properly set.
   */
  @PostConstruct
  public void validateCorsConfiguration() {
    if (allowedOrigins == null || allowedOrigins.isEmpty()) {
      throw new IllegalStateException(
          "CORS allowed origins must be configured. Set app.cors.allowed-origins property.");
    }
    
    // Check for overly permissive wildcard configuration
    if (allowedOrigins.contains("*") && allowCredentials) {
      throw new IllegalStateException(
          "CORS configuration error: Cannot use wildcard origin (*) with credentials enabled. "
              + "Specify explicit origins or disable credentials.");
    }
    
    // Warn about wildcard origins
    if (allowedOrigins.contains("*")) {
      log.warn("CORS is configured with wildcard origin (*). "
          + "This is not recommended for production environments.");
    }
    
    // Validate that origins are properly formatted (basic check)
    for (String origin : allowedOrigins) {
      if (origin.trim().isEmpty()) {
        throw new IllegalStateException(
            "CORS configuration contains empty origin. Remove empty entries.");
      }
      
      // Check for common misconfigurations
      if (!origin.equals("*") && !origin.startsWith("http://") 
          && !origin.startsWith("https://")) {
        log.warn("CORS origin '{}' does not start with http:// or https://. "
            + "Verify this is intentional.", origin);
      }
    }
    
    if (allowedMethods == null || allowedMethods.isEmpty()) {
      throw new IllegalStateException(
          "CORS allowed methods must be configured. Set app.cors.allowed-methods property.");
    }
    
    log.info("CORS configuration validated successfully. Allowed origins: {}", 
        allowedOrigins.size());
  }
}
