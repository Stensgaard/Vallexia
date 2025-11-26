package com.vallexia.config.web;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Data
@Validated
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
  
  /**
   * Allowed origins for CORS.
   */
  @NotEmpty(message = "CORS allowed origins must be configured")
  private List<String> allowedOrigins;
  
  /**
   * Allowed HTTP methods for CORS.
   */
  @NotEmpty(message = "CORS allowed methods must be configured")
  private List<String> allowedMethods;
  
  /**
   * Allowed headers for CORS.
   */
  @NotEmpty(message = "CORS allowed headers must be configured")
  private List<String> allowedHeaders;
  
  /**
   * Whether to allow credentials in CORS requests.
   */
  private boolean allowCredentials;
  
  /**
   * Validates CORS configuration for complex business logic rules.
   * 
   * <p>Bean Validation handles basic null/empty checks. This method validates
   * complex security rules that cannot be expressed with annotations:
   * - Wildcard origin with credentials (security violation)
   * - Origin format validation (business logic)
   */
  @PostConstruct
  public void validateCorsConfiguration() {
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
    
    log.info("CORS configuration validated successfully. Allowed origins: {}", 
        allowedOrigins.size());
  }
}
