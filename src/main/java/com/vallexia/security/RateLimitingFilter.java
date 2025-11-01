package com.vallexia.security;

import com.vallexia.audit.util.IpAddressExtractor;
import com.vallexia.config.security.RateLimitingConfig;
import com.vallexia.config.security.RateLimitingProperties;
import com.vallexia.security.job.RateLimitingBucketCleanupJob;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Rate limiting filter using Bucket4j with per-IP rate limiting.
 * Uses trusted proxy validation to prevent IP spoofing.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    
    // Check if rate limiting is globally disabled
    if (!rateLimitingProperties.isEnabled()) {
      log.debug("Rate limiting is globally disabled, skipping rate limit check");
      filterChain.doFilter(request, response);
      return;
    }
    
    String requestURI = request.getRequestURI();
    String clientIp = ipAddressExtractor.extractClientIp(request);
    
    // Validate IP address before using
    if (clientIp == null || clientIp.trim().isEmpty() || !isValidIpAddress(clientIp)) {
      log.warn("Could not extract or validate client IP, skipping rate limiting");
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
   * @param requestURI the request URI
   * @param clientIp the client IP address
   * @return the bucket with type for rate limiting, or null if no rate limiting applies
   */
  private BucketWithType getBucketForRequest(String requestURI, String clientIp) {
    if (requestURI.contains("/api/v1/auth/login")) {
      if (!rateLimitingProperties.getLogin().isEnabled()) {
        log.debug("Rate limiting disabled for login endpoint");
        return null;
      }
      Bucket bucket = loginRateLimitBuckets.computeIfAbsent(
          clientIp, k -> rateLimitingConfig.createLoginBucket());
      return new BucketWithType(bucket, "login");
    } else if (requestURI.contains("/api/v1/auth/register")) {
      if (!rateLimitingProperties.getRegistration().isEnabled()) {
        log.debug("Rate limiting disabled for registration endpoint");
        return null;
      }
      Bucket bucket = registrationRateLimitBuckets.computeIfAbsent(
          clientIp, k -> rateLimitingConfig.createRegistrationBucket());
      return new BucketWithType(bucket, "registration");
    } else if (requestURI.contains("/api/v1/auth/refresh")) {
      if (!rateLimitingProperties.getRefresh().isEnabled()) {
        log.debug("Rate limiting disabled for refresh endpoint");
        return null;
      }
      Bucket bucket = refreshRateLimitBuckets.computeIfAbsent(
          clientIp, k -> rateLimitingConfig.createRefreshBucket());
      return new BucketWithType(bucket, "refresh");
    } else if (requestURI.startsWith("/api/")) {
      if (!rateLimitingProperties.getGeneralApi().isEnabled()) {
        log.debug("Rate limiting disabled for general API endpoints");
        return null;
      }
      // Rate limit actuator endpoints as well
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
  
  /**
   * Validates if the given string is a valid IP address (IPv4 or IPv6).
   * Uses java.net.InetAddress for robust validation that handles all IPv4 and IPv6 formats,
   * including compressed IPv6, mixed notation, and edge cases.
   * 
   * This method prevents DNS lookups by checking the format first, then using InetAddress
   * only for numeric IP addresses to validate the range and format correctness.
   * 
   * @param ipAddress the IP address string to validate
   * @return true if valid IP address, false otherwise
   */
  private boolean isValidIpAddress(String ipAddress) {
    if (ipAddress == null || ipAddress.trim().isEmpty()) {
      return false;
    }
    
    String trimmed = ipAddress.trim();
    
    // Quick check: IP addresses should only contain numeric characters, dots, colons, and brackets
    // This prevents DNS lookups for hostnames
    if (!trimmed.matches("^[0-9a-fA-F:.\\[\\]]+$")) {
      return false;
    }
    
    // Remove IPv6 brackets if present for validation
    String addressToValidate = trimmed.replaceAll("^\\[|\\]$", "");
    
    try {
      // Use InetAddress.getByName() to validate the IP address format and range
      // Since we've filtered out hostnames above, this will only parse IP addresses
      // This validates:
      // - IPv4: correct octet ranges (0-255), proper format
      // - IPv6: correct hex format, compressed notation (::), mixed notation
      // - Rejects invalid ranges (e.g., 256.256.256.256)
      InetAddress addr = InetAddress.getByName(addressToValidate);
      
      // Verify it's actually an IP address and not a hostname
      // getHostAddress() returns the numeric IP, so if it matches patterns, it's an IP
      String hostAddress = addr.getHostAddress();
      
      // For IPv4, check that getHostAddress() is in IPv4 format
      // For IPv6, check that it's in IPv6 format (contains colons)
      // This ensures we didn't accidentally accept a hostname
      boolean isIPv4 = hostAddress.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
      boolean isIPv6 = hostAddress.contains(":");
      
      if (!isIPv4 && !isIPv6) {
        // Not a valid IP format
        return false;
      }
      
      // Additional validation: ensure the byte array length matches expected IP version
      byte[] addressBytes = addr.getAddress();
      if (isIPv4 && addressBytes.length != 4) {
        return false;
      }
      if (isIPv6 && addressBytes.length != 16) {
        return false;
      }
      
      // Successfully validated as IP address
      return true;
             
    } catch (UnknownHostException e) {
      // getByName() couldn't parse it as an IP address
      log.debug("Invalid IP address format: {}", trimmed);
      return false;
    } catch (Exception e) {
      // Catch any other exceptions (security or parsing issues)
      log.debug("Error validating IP address {}: {}", trimmed, e.getClass().getSimpleName());
      return false;
    }
  }
  
}
