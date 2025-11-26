package com.vallexia.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for allowing frontend requests.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Configuration
public class CorsConfig {
  
  private final CorsProperties corsProperties;
  
  /**
   * Constructor with dependency injection.
   * 
   * @param corsProperties CORS configuration properties
   */
  public CorsConfig(CorsProperties corsProperties) {
    this.corsProperties = corsProperties;
  }
  
  /**
   * CORS configuration source bean.
   * 
   * @return CorsConfigurationSource instance
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Set allowed origins
    configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
    
    // Set allowed methods
    configuration.setAllowedMethods(corsProperties.getAllowedMethods());
    
    // Set allowed headers
    configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
    
    // Allow credentials
    configuration.setAllowCredentials(corsProperties.isAllowCredentials());
    
    // Expose headers
    configuration.setExposedHeaders(List.of("Authorization", "X-Total-Count"));
    
    // Set max age for preflight requests
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    
    return source;
  }
}
