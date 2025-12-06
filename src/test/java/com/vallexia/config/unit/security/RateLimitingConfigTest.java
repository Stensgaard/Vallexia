package com.vallexia.config.unit.security;

import com.vallexia.config.security.RateLimitingConfig;
import com.vallexia.config.security.RateLimitingProperties;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RateLimitingConfig.
 * Tests bucket Map beans and bucket creation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("RateLimitingConfig Tests")
class RateLimitingConfigTest {
  
  private RateLimitingProperties createMockProperties() {
    RateLimitingProperties properties = mock(RateLimitingProperties.class);
    
    // Mock LoginConfig
    RateLimitingProperties.LoginConfig loginConfig = new RateLimitingProperties.LoginConfig();
    loginConfig.setRequests(5);
    loginConfig.setDurationMinutes(1);
    when(properties.getLogin()).thenReturn(loginConfig);
    
    // Mock RegistrationConfig
    RateLimitingProperties.RegistrationConfig registrationConfig = 
        new RateLimitingProperties.RegistrationConfig();
    registrationConfig.setRequests(3);
    registrationConfig.setDurationMinutes(5);
    when(properties.getRegistration()).thenReturn(registrationConfig);
    
    // Mock GeneralApiConfig
    RateLimitingProperties.GeneralApiConfig generalApiConfig = 
        new RateLimitingProperties.GeneralApiConfig();
    generalApiConfig.setRequests(100);
    generalApiConfig.setDurationMinutes(1);
    when(properties.getGeneralApi()).thenReturn(generalApiConfig);
    
    // Mock RefreshConfig
    RateLimitingProperties.RefreshConfig refreshConfig = 
        new RateLimitingProperties.RefreshConfig();
    refreshConfig.setRequests(10);
    refreshConfig.setDurationMinutes(5);
    when(properties.getRefresh()).thenReturn(refreshConfig);
    
    return properties;
  }
  
  @Test
  @DisplayName("Should create loginRateLimitBuckets bean")
  void shouldCreateLoginRateLimitBucketsBean() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Map<String, Bucket> buckets = config.loginRateLimitBuckets();
    
    // Then
    assertThat(buckets).isNotNull();
    assertThat(buckets).isInstanceOf(ConcurrentHashMap.class);
    assertThat(buckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should create registrationRateLimitBuckets bean")
  void shouldCreateRegistrationRateLimitBucketsBean() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Map<String, Bucket> buckets = config.registrationRateLimitBuckets();
    
    // Then
    assertThat(buckets).isNotNull();
    assertThat(buckets).isInstanceOf(ConcurrentHashMap.class);
    assertThat(buckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should create generalApiRateLimitBuckets bean")
  void shouldCreateGeneralApiRateLimitBucketsBean() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Map<String, Bucket> buckets = config.generalApiRateLimitBuckets();
    
    // Then
    assertThat(buckets).isNotNull();
    assertThat(buckets).isInstanceOf(ConcurrentHashMap.class);
    assertThat(buckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should create refreshRateLimitBuckets bean")
  void shouldCreateRefreshRateLimitBucketsBean() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Map<String, Bucket> buckets = config.refreshRateLimitBuckets();
    
    // Then
    assertThat(buckets).isNotNull();
    assertThat(buckets).isInstanceOf(ConcurrentHashMap.class);
    assertThat(buckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should create login bucket with correct configuration")
  void shouldCreateLoginBucketWithCorrectConfiguration() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Bucket bucket = config.createLoginBucket();
    
    // Then
    assertThat(bucket).isNotNull();
    // Test that bucket allows the configured number of requests
    for (int i = 0; i < 5; i++) {
      assertThat(bucket.tryConsume(1)).isTrue();
    }
    // 6th request should fail
    assertThat(bucket.tryConsume(1)).isFalse();
  }
  
  @Test
  @DisplayName("Should create registration bucket with correct configuration")
  void shouldCreateRegistrationBucketWithCorrectConfiguration() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Bucket bucket = config.createRegistrationBucket();
    
    // Then
    assertThat(bucket).isNotNull();
    // Test that bucket allows the configured number of requests
    for (int i = 0; i < 3; i++) {
      assertThat(bucket.tryConsume(1)).isTrue();
    }
    // 4th request should fail
    assertThat(bucket.tryConsume(1)).isFalse();
  }
  
  @Test
  @DisplayName("Should create general API bucket with correct configuration")
  void shouldCreateGeneralApiBucketWithCorrectConfiguration() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Bucket bucket = config.createGeneralApiBucket();
    
    // Then
    assertThat(bucket).isNotNull();
    // Test that bucket allows the configured number of requests
    for (int i = 0; i < 100; i++) {
      assertThat(bucket.tryConsume(1)).isTrue();
    }
    // 101st request should fail
    assertThat(bucket.tryConsume(1)).isFalse();
  }
  
  @Test
  @DisplayName("Should create refresh bucket with correct configuration")
  void shouldCreateRefreshBucketWithCorrectConfiguration() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Bucket bucket = config.createRefreshBucket();
    
    // Then
    assertThat(bucket).isNotNull();
    // Test that bucket allows the configured number of requests
    for (int i = 0; i < 10; i++) {
      assertThat(bucket.tryConsume(1)).isTrue();
    }
    // 11th request should fail
    assertThat(bucket.tryConsume(1)).isFalse();
  }
  
  @Test
  @DisplayName("Should create buckets with different configurations")
  void shouldCreateBucketsWithDifferentConfigurations() {
    // Given
    RateLimitingProperties properties = createMockProperties();
    RateLimitingConfig config = new RateLimitingConfig(properties);
    
    // When
    Bucket loginBucket = config.createLoginBucket();
    Bucket registrationBucket = config.createRegistrationBucket();
    Bucket generalApiBucket = config.createGeneralApiBucket();
    Bucket refreshBucket = config.createRefreshBucket();
    
    // Then
    assertThat(loginBucket).isNotNull();
    assertThat(registrationBucket).isNotNull();
    assertThat(generalApiBucket).isNotNull();
    assertThat(refreshBucket).isNotNull();
    
    // Verify they have different limits
    // Login: 5 requests
    assertThat(loginBucket.getAvailableTokens()).isEqualTo(5);
    // Registration: 3 requests
    assertThat(registrationBucket.getAvailableTokens()).isEqualTo(3);
    // General API: 100 requests
    assertThat(generalApiBucket.getAvailableTokens()).isEqualTo(100);
    // Refresh: 10 requests
    assertThat(refreshBucket.getAvailableTokens()).isEqualTo(10);
  }
}
