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
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
public class RateLimitingConfig {
  
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
   * Allows 5 requests per minute per IP address.
   * 
   * @return Bucket instance for login rate limiting
   */
  public Bucket createLoginBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
        .build();
  }
  
  /**
   * Creates a rate limiting bucket for registration attempts.
   * Allows 3 requests per 5 minutes per IP address.
   * 
   * @return Bucket instance for registration rate limiting
   */
  public Bucket createRegistrationBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(5))))
        .build();
  }
  
  /**
   * Creates a rate limiting bucket for general API requests.
   * Allows 100 requests per minute per IP address.
   * 
   * @return Bucket instance for general API rate limiting
   */
  public Bucket createGeneralApiBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
        .build();
  }
  
  /**
   * Creates a rate limiting bucket for refresh token attempts.
   * Allows 10 requests per 5 minutes per IP address.
   * Stricter than general API to prevent refresh token brute force attacks.
   * 
   * @return Bucket instance for refresh token rate limiting
   */
  public Bucket createRefreshBucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5))))
        .build();
  }
}
