package com.vallexia.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j with per-IP bucket storage.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Configuration
public class RateLimitingConfig {
  
  private final RateLimitingProperties rateLimitingProperties;
  
  /**
   * Constructor with dependency injection.
   * 
   * @param rateLimitingProperties rate limiting configuration properties
   */
  public RateLimitingConfig(RateLimitingProperties rateLimitingProperties) {
    this.rateLimitingProperties = rateLimitingProperties;
  }
  
  /**
   * Storage for per-IP rate limiting buckets for login attempts.
   * Uses ConcurrentHashMap for thread-safe access.
   */
  @Bean
  public Map<String, Bucket> loginRateLimitBuckets() {
    return new ConcurrentHashMap<>();
  }
  
  /**
   * Storage for per-IP rate limiting buckets for registration attempts.
   * Uses ConcurrentHashMap for thread-safe access.
   */
  @Bean
  public Map<String, Bucket> registrationRateLimitBuckets() {
    return new ConcurrentHashMap<>();
  }
  
  /**
   * Storage for per-IP rate limiting buckets for general API requests.
   * Uses ConcurrentHashMap for thread-safe access.
   */
  @Bean
  public Map<String, Bucket> generalApiRateLimitBuckets() {
    return new ConcurrentHashMap<>();
  }
  
  /**
   * Storage for per-IP rate limiting buckets for refresh token requests.
   * Uses ConcurrentHashMap for thread-safe access.
   */
  @Bean
  public Map<String, Bucket> refreshRateLimitBuckets() {
    return new ConcurrentHashMap<>();
  }
  
  /**
   * Creates a rate limiting bucket for login attempts.
   * Configuration is read from application properties.
   * 
   * @return Bucket instance for login rate limiting
   */
  public Bucket createLoginBucket() {
    RateLimitingProperties.LoginConfig config = rateLimitingProperties.getLogin();
    int requests = config.getRequests();
    int durationMinutes = config.getDurationMinutes();
    return Bucket.builder()
        .addLimit(Bandwidth.classic(requests, Refill.intervally(requests, Duration.ofMinutes(durationMinutes))))
        .build();
  }
  
  /**
   * Creates a rate limiting bucket for registration attempts.
   * Configuration is read from application properties.
   * 
   * @return Bucket instance for registration rate limiting
   */
  public Bucket createRegistrationBucket() {
    RateLimitingProperties.RegistrationConfig config = rateLimitingProperties.getRegistration();
    int requests = config.getRequests();
    int durationMinutes = config.getDurationMinutes();
    return Bucket.builder()
        .addLimit(Bandwidth.classic(requests, Refill.intervally(requests, Duration.ofMinutes(durationMinutes))))
        .build();
  }
  
  /**
   * Creates a rate limiting bucket for general API requests.
   * Configuration is read from application properties.
   * 
   * @return Bucket instance for general API rate limiting
   */
  public Bucket createGeneralApiBucket() {
    RateLimitingProperties.GeneralApiConfig config = rateLimitingProperties.getGeneralApi();
    int requests = config.getRequests();
    int durationMinutes = config.getDurationMinutes();
    return Bucket.builder()
        .addLimit(Bandwidth.classic(requests, Refill.intervally(requests, Duration.ofMinutes(durationMinutes))))
        .build();
  }
  
  /**
   * Creates a rate limiting bucket for refresh token attempts.
   * Configuration is read from application properties.
   * Stricter than general API to prevent refresh token brute force attacks.
   * 
   * @return Bucket instance for refresh token rate limiting
   */
  public Bucket createRefreshBucket() {
    RateLimitingProperties.RefreshConfig config = rateLimitingProperties.getRefresh();
    int requests = config.getRequests();
    int durationMinutes = config.getDurationMinutes();
    return Bucket.builder()
        .addLimit(Bandwidth.classic(requests, Refill.intervally(requests, Duration.ofMinutes(durationMinutes))))
        .build();
  }
}
