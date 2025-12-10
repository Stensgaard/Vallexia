package com.vallexia.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * 
 * <p>This configuration ensures the ObjectMapper bean is available early
 * for security components that need it during initialization.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-10
 */
@Configuration
public class JacksonConfig {
  
  /**
   * Provides a configured ObjectMapper bean.
   * 
   * <p>This ensures the bean is available early for security configuration.
   * Configured with JavaTimeModule for proper LocalDateTime serialization.
   * 
   * @return configured ObjectMapper instance
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
