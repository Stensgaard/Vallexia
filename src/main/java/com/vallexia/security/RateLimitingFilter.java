package com.vallexia.security;

import com.vallexia.config.security.RateLimitingConfig;
import com.vallexia.security.util.IpAddressExtractor;
import com.vallexia.config.security.RateLimitingProperties;
import com.vallexia.security.job.RateLimitingBucketCleanupJob;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Rate limiting filter using Bucket4j with per-IP rate limiting.
 * Uses trusted proxy validation to prevent IP spoofing.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
  
  private final Map<String, Bucket> loginRateLimitBuckets;
  private final Map<String, Bucket> registrationRateLimitBuckets;
  private final Map<String, Bucket> generalApiRateLimitBuckets;
  private final Map<String, Bucket> refreshRateLimitBuckets;
  private final RateLimitingConfig rateLimitingConfig;
  private final RateLimitingProperties rateLimitingProperties;
  private final IpAddressExtractor ipAddressExtractor;
  private final RateLimitingBucketCleanupJob bucketCleanupJob;
  
  /**
   * Constructor with dependency injection.
   * 
   * @param loginRateLimitBuckets bucket storage for login attempts
   * @param registrationRateLimitBuckets bucket storage for registration attempts
   * @param generalApiRateLimitBuckets bucket storage for general API requests
   * @param refreshRateLimitBuckets bucket storage for refresh token requests
   * @param rateLimitingConfig configuration for creating new buckets
   * @param rateLimitingProperties rate limiting configuration properties
   * @param ipAddressExtractor secure IP address extraction with proxy validation
   * @param bucketCleanupJob cleanup job for tracking bucket access (optional, may be null)
   */
  public RateLimitingFilter(
      Map<String, Bucket> loginRateLimitBuckets,
      Map<String, Bucket> registrationRateLimitBuckets,
      Map<String, Bucket> generalApiRateLimitBuckets,
      Map<String, Bucket> refreshRateLimitBuckets,
      RateLimitingConfig rateLimitingConfig,
      RateLimitingProperties rateLimitingProperties,
      IpAddressExtractor ipAddressExtractor,
      @Nullable RateLimitingBucketCleanupJob bucketCleanupJob) {
    this.loginRateLimitBuckets = loginRateLimitBuckets;
    this.registrationRateLimitBuckets = registrationRateLimitBuckets;
    this.generalApiRateLimitBuckets = generalApiRateLimitBuckets;
    this.refreshRateLimitBuckets = refreshRateLimitBuckets;
    this.rateLimitingConfig = rateLimitingConfig;
    this.rateLimitingProperties = rateLimitingProperties;
    this.ipAddressExtractor = ipAddressExtractor;
    this.bucketCleanupJob = bucketCleanupJob;
  }
  
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    
    // Check if rate limiting is globally disabled
    if (!rateLimitingProperties.isEnabled()) {
      log.debug("Rate limiting is globally disabled, skipping rate limit check");
      filterChain.doFilter(request, response);
      return;
    }
    
    String requestURI = request.getRequestURI();
    String clientIp = ipAddressExtractor.extractClientIp(request);
    
    // Validate IP address before using (IpAddressExtractor already validates, just check for null/empty)
    if (clientIp == null || clientIp.trim().isEmpty()) {
      log.warn("Could not extract client IP, skipping rate limiting");
      filterChain.doFilter(request, response);
      return;
    }
    
    // Get the appropriate bucket for this IP and endpoint
    BucketWithType bucketWithType = getBucketForRequest(requestURI, clientIp);
    
    if (bucketWithType != null) {
      Bucket bucket = bucketWithType.bucket;
      
      // Record bucket access for cleanup tracking
      if (bucketCleanupJob != null) {
        bucketCleanupJob.recordBucketAccess(clientIp, bucketWithType.type);
      }
      if (bucket.tryConsume(1)) {
        log.debug("Rate limit check passed for IP: {} on endpoint: {}", clientIp, requestURI);
        filterChain.doFilter(request, response);
      } else {
        log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, requestURI);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\":\"Rate limit exceeded. Please try again later.\"}");
        return;
      }
    } else {
      // No rate limiting for this endpoint
      filterChain.doFilter(request, response);
    }
  }
  
  /**
   * Get the appropriate rate limiting bucket based on the request URI and client IP.
   * Creates a new bucket if one doesn't exist for this IP.
   * 
   * <p>The logout endpoint (/api/v1/auth/logout) is excluded from general API rate limiting
   * 
   * @param requestURI the request URI
   * @param clientIp the client IP address
   * @return the bucket with type for rate limiting, or null if no rate limiting applies
   */
  private BucketWithType getBucketForRequest(String requestURI, String clientIp) {
    if (requestURI.equals("/api/v1/auth/login")) {
      if (!rateLimitingProperties.getLogin().isEnabled()) {
        log.debug("Rate limiting disabled for login endpoint");
        return null;
      }
      Bucket bucket = loginRateLimitBuckets.computeIfAbsent(
          clientIp, k -> rateLimitingConfig.createLoginBucket());
      return new BucketWithType(bucket, "login");
    } else if (requestURI.equals("/api/v1/auth/register")) {
      if (!rateLimitingProperties.getRegistration().isEnabled()) {
        log.debug("Rate limiting disabled for registration endpoint");
        return null;
      }
      Bucket bucket = registrationRateLimitBuckets.computeIfAbsent(
          clientIp, k -> rateLimitingConfig.createRegistrationBucket());
      return new BucketWithType(bucket, "registration");
    } else if (requestURI.equals("/api/v1/auth/refresh")) {
      if (!rateLimitingProperties.getRefresh().isEnabled()) {
        log.debug("Rate limiting disabled for refresh endpoint");
        return null;
      }
      Bucket bucket = refreshRateLimitBuckets.computeIfAbsent(
          clientIp, k -> rateLimitingConfig.createRefreshBucket());
      return new BucketWithType(bucket, "refresh");
    } else if (requestURI.startsWith("/api/") && !requestURI.equals("/api/v1/auth/logout")) {
      if (!rateLimitingProperties.getGeneralApi().isEnabled()) {
        log.debug("Rate limiting disabled for general API endpoints");
        return null;
      }
      // Rate limit actuator endpoints as well
      // Exclude logout endpoint from general API rate limiting (requires authentication)
      Bucket bucket = generalApiRateLimitBuckets.computeIfAbsent(
          clientIp, k -> rateLimitingConfig.createGeneralApiBucket());
      return new BucketWithType(bucket, "generalApi");
    }

    return null;
  }
  
  /**
   * Helper class to hold bucket and its type for cleanup tracking.
   */
  private static class BucketWithType {
    final Bucket bucket;
    final String type;
    
    BucketWithType(Bucket bucket, String type) {
      this.bucket = bucket;
      this.type = type;
    }
  }
}
