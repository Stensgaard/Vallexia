package com.vallexia.security.unit;

import com.vallexia.audit.util.IpAddressExtractor;
import com.vallexia.config.security.RateLimitingConfig;
import com.vallexia.config.security.RateLimitingProperties;
import com.vallexia.security.RateLimitingFilter;
import com.vallexia.security.job.RateLimitingBucketCleanupJob;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitingFilter.
 * Tests rate limiting logic, IP validation, and bucket management.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingFilter Unit Tests")
class RateLimitingFilterTest {
  
  private RateLimitingFilter rateLimitingFilter;
  private RateLimitingConfig rateLimitingConfig;
  private RateLimitingProperties rateLimitingProperties;
  
  @Mock
  private Map<String, Bucket> loginRateLimitBuckets;
  
  @Mock
  private Map<String, Bucket> registrationRateLimitBuckets;
  
  @Mock
  private Map<String, Bucket> generalApiRateLimitBuckets;
  
  @Mock
  private Map<String, Bucket> refreshRateLimitBuckets;
  
  @Mock
  private IpAddressExtractor ipAddressExtractor;
  
  @Mock
  private RateLimitingBucketCleanupJob bucketCleanupJob;
  
  @Mock
  private HttpServletRequest request;
  
  @Mock
  private HttpServletResponse response;
  
  @Mock
  private FilterChain filterChain;
  
  @BeforeEach
  void setUp() {
    // Create rate limiting properties with default enabled values
    rateLimitingProperties = new RateLimitingProperties();
    rateLimitingProperties.setEnabled(true);
    rateLimitingProperties.getLogin().setEnabled(true);
    rateLimitingProperties.getRegistration().setEnabled(true);
    rateLimitingProperties.getGeneralApi().setEnabled(true);
    rateLimitingProperties.getRefresh().setEnabled(true);
    
    rateLimitingConfig = new RateLimitingConfig(rateLimitingProperties);
    
    // Initialize maps
    Map<String, Bucket> loginBuckets = new HashMap<>();
    Map<String, Bucket> registrationBuckets = new HashMap<>();
    Map<String, Bucket> generalApiBuckets = new HashMap<>();
    Map<String, Bucket> refreshBuckets = new HashMap<>();
    
    rateLimitingFilter = new RateLimitingFilter(
        loginBuckets,
        registrationBuckets,
        generalApiBuckets,
        refreshBuckets,
        rateLimitingConfig,
        rateLimitingProperties,
        ipAddressExtractor,
        bucketCleanupJob
    );
  }
  
  // ==================== IP Validation Tests ====================
  
  @Test
  @DisplayName("Should skip rate limiting when IP address is null")
  void shouldSkipRateLimitingWhenIpIsNull() throws Exception {
    // Given
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(null);
    
    // When - call through public doFilter method
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }
  
  @Test
  @DisplayName("Should skip rate limiting when IP address is empty")
  void shouldSkipRateLimitingWhenIpIsEmpty() throws Exception {
    // Given
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn("");
    
    // When - call through public doFilter method
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }
  
  @Test
  @DisplayName("Should skip rate limiting when IP address is invalid")
  void shouldSkipRateLimitingWhenIpIsInvalid() throws Exception {
    // Given
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn("invalid-ip-format");
    
    // When - call through public doFilter method
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }
  
  // ==================== Rate Limiting Logic Tests ====================
  
  @Test
  @DisplayName("Should allow request when rate limit is not exceeded")
  void shouldAllowRequestWhenRateLimitNotExceeded() throws Exception {
    // Given
    String validIp = "192.168.1.1";
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIp);
    
    // When - call through public doFilter method
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
    verify(bucketCleanupJob).recordBucketAccess(validIp, "login");
  }
  
  @Test
  @DisplayName("Should return 429 when rate limit is exceeded")
  void shouldReturn429WhenRateLimitExceeded() throws Exception {
    // Given
    String validIp = "192.168.1.1";
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIp);
    when(response.getWriter()).thenReturn(printWriter);
    
    // Create a bucket and consume all tokens
    Bucket bucket = rateLimitingConfig.createLoginBucket();
    // Consume all 5 tokens
    for (int i = 0; i < 5; i++) {
      bucket.tryConsume(1);
    }
    
    // Manually set bucket in the filter's map
    // We need to use reflection or make getBucketForRequest package-private for testing
    // For now, we'll test the rate limiting by calling filter multiple times
    // Let's consume tokens first
    for (int i = 0; i < 5; i++) {
      rateLimitingFilter.doFilter(request, response, filterChain);
    }
    
    // 6th request should be rate limited
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then - verify rate limit response
    verify(response, atLeastOnce()).setStatus(429); // TOO_MANY_REQUESTS
    verify(response, atLeastOnce()).setContentType("application/json");
  }
  
  @Test
  @DisplayName("Should apply correct rate limit bucket for login endpoint")
  void shouldApplyCorrectRateLimitBucketForLogin() throws Exception {
    // Given
    String validIp = "192.168.1.1";
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIp);
    
    // When
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(bucketCleanupJob).recordBucketAccess(validIp, "login");
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should apply correct rate limit bucket for registration endpoint")
  void shouldApplyCorrectRateLimitBucketForRegistration() throws Exception {
    // Given
    String validIp = "192.168.1.1";
    when(request.getRequestURI()).thenReturn("/api/v1/auth/register");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIp);
    
    // When
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(bucketCleanupJob).recordBucketAccess(validIp, "registration");
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should apply correct rate limit bucket for refresh endpoint")
  void shouldApplyCorrectRateLimitBucketForRefresh() throws Exception {
    // Given
    String validIp = "192.168.1.1";
    when(request.getRequestURI()).thenReturn("/api/v1/auth/refresh");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIp);
    
    // When
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(bucketCleanupJob).recordBucketAccess(validIp, "refresh");
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should apply general API rate limit bucket for other API endpoints")
  void shouldApplyGeneralApiBucketForOtherEndpoints() throws Exception {
    // Given
    String validIp = "192.168.1.1";
    when(request.getRequestURI()).thenReturn("/api/v1/users/profile");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIp);
    
    // When
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(bucketCleanupJob).recordBucketAccess(validIp, "generalApi");
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should not apply rate limiting for non-API endpoints")
  void shouldNotApplyRateLimitingForNonApiEndpoints() throws Exception {
    // Given
    String validIp = "192.168.1.1";
    when(request.getRequestURI()).thenReturn("/static/css/style.css");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIp);
    
    // When
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(bucketCleanupJob, never()).recordBucketAccess(anyString(), anyString());
    verify(filterChain).doFilter(request, response);
  }
  
  // ==================== IPv6 Support Tests ====================
  
  @Test
  @DisplayName("Should accept valid IPv6 address")
  void shouldAcceptValidIPv6Address() throws Exception {
    // Given
    String validIpv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(validIpv6);
    
    // When
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(filterChain).doFilter(request, response);
    verify(bucketCleanupJob).recordBucketAccess(validIpv6, "login");
  }
  
  @Test
  @DisplayName("Should accept compressed IPv6 address")
  void shouldAcceptCompressedIPv6Address() throws Exception {
    // Given
    String compressedIpv6 = "2001:db8::1";
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    when(ipAddressExtractor.extractClientIp(request)).thenReturn(compressedIpv6);
    
    // When
    rateLimitingFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(filterChain).doFilter(request, response);
  }
}
