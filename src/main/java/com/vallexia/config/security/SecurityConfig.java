package com.vallexia.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for password encoding.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
public class SecurityConfig {
  
  /**
   * Password encoder bean using BCrypt with strength 12.
   * 
   * @return BCryptPasswordEncoder instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
