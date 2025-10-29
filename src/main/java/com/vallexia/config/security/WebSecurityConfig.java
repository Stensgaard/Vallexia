package com.vallexia.config.security;

import com.vallexia.auth.service.TokenBlacklistService;
import com.vallexia.config.web.CorsConfig;
import com.vallexia.security.AuthEntryPointJwt;
import com.vallexia.security.AuthTokenFilter;
import com.vallexia.security.JwtUtils;
import com.vallexia.security.RateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for JWT-based authentication.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
  
  private final AuthEntryPointJwt unauthorizedHandler;
  private final CorsConfig corsConfig;
  private final RateLimitingFilter rateLimitingFilter;
  private final JwtUtils jwtUtils;
  private final TokenBlacklistService tokenBlacklistService;
  
  /**
   * Constructor with dependency injection.
   * 
   * @param unauthorizedHandler handler for unauthorized access attempts
   * @param corsConfig CORS configuration
   * @param rateLimitingFilter rate limiting filter
   * @param jwtUtils JWT utility for token operations
   * @param tokenBlacklistService service for checking token blacklist
   */
  public WebSecurityConfig(
      AuthEntryPointJwt unauthorizedHandler,
      CorsConfig corsConfig,
      RateLimitingFilter rateLimitingFilter,
      JwtUtils jwtUtils,
      TokenBlacklistService tokenBlacklistService) {
    this.unauthorizedHandler = unauthorizedHandler;
    this.corsConfig = corsConfig;
    this.rateLimitingFilter = rateLimitingFilter;
    this.jwtUtils = jwtUtils;
    this.tokenBlacklistService = tokenBlacklistService;
  }
  
  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter(jwtUtils, tokenBlacklistService);
  }
  
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> 
            auth.requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
        )
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.deny())
            .contentTypeOptions(contentTypeOptions -> {})
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true)
            )
            .addHeaderWriter((request, response) -> {
              response.setHeader("X-XSS-Protection", "1; mode=block");
              response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
              response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
              // Content-Security-Policy for REST API (allows JSON responses)
              response.setHeader("Content-Security-Policy", 
                  "default-src 'none'; frame-ancestors 'none'");
            })
        );
    
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
  }
}
