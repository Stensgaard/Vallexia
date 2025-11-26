package com.vallexia.security.unit.job;

import com.vallexia.config.security.RateLimitingConfig;
import com.vallexia.config.security.RateLimitingProperties;
import com.vallexia.security.job.RateLimitingBucketCleanupJob;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RateLimitingBucketCleanupJob.
 * Tests bucket cleanup logic, access tracking, and memory leak prevention.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("RateLimitingBucketCleanupJob Unit Tests")
class RateLimitingBucketCleanupJobTest {
  
  private RateLimitingBucketCleanupJob cleanupJob;
  private Map<String, Bucket> loginRateLimitBuckets;
  private Map<String, Bucket> registrationRateLimitBuckets;
  private Map<String, Bucket> generalApiRateLimitBuckets;
  private Map<String, Bucket> refreshRateLimitBuckets;
  private RateLimitingConfig rateLimitingConfig;
  
  @BeforeEach
  void setUp() {
    // Initialize bucket maps
    loginRateLimitBuckets = new ConcurrentHashMap<>();
    registrationRateLimitBuckets = new ConcurrentHashMap<>();
    generalApiRateLimitBuckets = new ConcurrentHashMap<>();
    refreshRateLimitBuckets = new ConcurrentHashMap<>();
    
    // Create rate limiting config for bucket creation
    RateLimitingProperties properties = new RateLimitingProperties();
    properties.getLogin().setRequests(5);
    properties.getLogin().setDurationMinutes(1);
    properties.getRegistration().setRequests(3);
    properties.getRegistration().setDurationMinutes(5);
    properties.getGeneralApi().setRequests(100);
    properties.getGeneralApi().setDurationMinutes(1);
    properties.getRefresh().setRequests(10);
    properties.getRefresh().setDurationMinutes(1);
    
    rateLimitingConfig = new RateLimitingConfig(properties);
    
    // Create cleanup job
    cleanupJob = new RateLimitingBucketCleanupJob(
        loginRateLimitBuckets,
        registrationRateLimitBuckets,
        generalApiRateLimitBuckets,
        refreshRateLimitBuckets
    );
  }
  
  // ==================== Bucket Access Tracking Tests ====================
  
  @Test
  @DisplayName("Should track login bucket access")
  void shouldTrackLoginBucketAccess() throws Exception {
    // Given
    String ipAddress = "192.168.1.1";
    
    // When
    cleanupJob.recordBucketAccess(ipAddress, "login");
    
    // Then - verify access was recorded by checking internal map via reflection
    Field loginLastAccessField = RateLimitingBucketCleanupJob.class.getDeclaredField("loginLastAccess");
    loginLastAccessField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Long> loginLastAccess = (Map<String, Long>) loginLastAccessField.get(cleanupJob);
    assertThat(loginLastAccess).containsKey(ipAddress);
    assertThat(loginLastAccess.get(ipAddress)).isNotNull();
  }
  
  @Test
  @DisplayName("Should track registration bucket access")
  void shouldTrackRegistrationBucketAccess() {
    // Given
    String ipAddress = "192.168.1.2";
    
    // When
    cleanupJob.recordBucketAccess(ipAddress, "registration");
    
    // Then
    assertThat(registrationRateLimitBuckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should track general API bucket access")
  void shouldTrackGeneralApiBucketAccess() {
    // Given
    String ipAddress = "192.168.1.3";
    
    // When
    cleanupJob.recordBucketAccess(ipAddress, "generalApi");
    
    // Then
    assertThat(generalApiRateLimitBuckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should track refresh bucket access")
  void shouldTrackRefreshBucketAccess() {
    // Given
    String ipAddress = "192.168.1.4";
    
    // When
    cleanupJob.recordBucketAccess(ipAddress, "refresh");
    
    // Then
    assertThat(refreshRateLimitBuckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should ignore null IP address")
  void shouldIgnoreNullIpAddress() {
    // When
    cleanupJob.recordBucketAccess(null, "login");
    
    // Then - should not throw exception
    assertThat(loginRateLimitBuckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should ignore empty IP address")
  void shouldIgnoreEmptyIpAddress() {
    // When
    cleanupJob.recordBucketAccess("", "login");
    cleanupJob.recordBucketAccess("   ", "login");
    
    // Then - should not throw exception
    assertThat(loginRateLimitBuckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should handle unknown bucket type gracefully")
  void shouldHandleUnknownBucketType() {
    // Given
    String ipAddress = "192.168.1.5";
    
    // When - should not throw exception
    cleanupJob.recordBucketAccess(ipAddress, "unknownType");
    
    // Then
    assertThat(loginRateLimitBuckets).isEmpty();
  }
  
  // ==================== Cleanup Tests ====================
  
  @Test
  @DisplayName("Should cleanup unused login buckets older than 1 hour")
  void shouldCleanupUnusedLoginBuckets() throws Exception {
    // Given
    String oldIp = "192.168.1.10";
    String recentIp = "192.168.1.11";
    
    // Create buckets
    Bucket oldBucket = rateLimitingConfig.createLoginBucket();
    Bucket recentBucket = rateLimitingConfig.createLoginBucket();
    loginRateLimitBuckets.put(oldIp, oldBucket);
    loginRateLimitBuckets.put(recentIp, recentBucket);
    
    // Record access for both IPs
    cleanupJob.recordBucketAccess(oldIp, "login");
    cleanupJob.recordBucketAccess(recentIp, "login");
    
    // Manually set old timestamp for oldIp (2 hours ago)
    Field loginLastAccessField = RateLimitingBucketCleanupJob.class.getDeclaredField("loginLastAccess");
    loginLastAccessField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Long> loginLastAccess = (Map<String, Long>) loginLastAccessField.get(cleanupJob);
    long twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000); // 2 hours ago
    loginLastAccess.put(oldIp, twoHoursAgo);
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - old bucket should be removed, recent bucket should remain
    assertThat(loginRateLimitBuckets).doesNotContainKey(oldIp);
    assertThat(loginRateLimitBuckets).containsKey(recentIp);
    assertThat(loginLastAccess).doesNotContainKey(oldIp);
    assertThat(loginLastAccess).containsKey(recentIp);
  }
  
  @Test
  @DisplayName("Should not cleanup recently accessed buckets")
  void shouldNotCleanupRecentlyAccessedBuckets() {
    // Given
    String ip1 = "192.168.1.20";
    String ip2 = "192.168.1.21";
    String ip3 = "192.168.1.22";
    
    Bucket bucket1 = rateLimitingConfig.createLoginBucket();
    Bucket bucket2 = rateLimitingConfig.createRegistrationBucket();
    Bucket bucket3 = rateLimitingConfig.createGeneralApiBucket();
    
    loginRateLimitBuckets.put(ip1, bucket1);
    registrationRateLimitBuckets.put(ip2, bucket2);
    generalApiRateLimitBuckets.put(ip3, bucket3);
    
    // Record recent access for all buckets
    cleanupJob.recordBucketAccess(ip1, "login");
    cleanupJob.recordBucketAccess(ip2, "registration");
    cleanupJob.recordBucketAccess(ip3, "generalApi");
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - all buckets should still exist
    assertThat(loginRateLimitBuckets).containsKey(ip1);
    assertThat(registrationRateLimitBuckets).containsKey(ip2);
    assertThat(generalApiRateLimitBuckets).containsKey(ip3);
  }
  
  @Test
  @DisplayName("Should cleanup old buckets from all bucket types")
  void shouldCleanupOldBucketsFromAllTypes() throws Exception {
    // Given - create old and recent buckets for all types
    String oldLoginIp = "192.168.1.30";
    String recentLoginIp = "192.168.1.31";
    String oldRegistrationIp = "192.168.1.32";
    String recentRegistrationIp = "192.168.1.33";
    String oldGeneralApiIp = "192.168.1.34";
    String recentGeneralApiIp = "192.168.1.35";
    String oldRefreshIp = "192.168.1.36";
    String recentRefreshIp = "192.168.1.37";
    
    // Create old buckets
    loginRateLimitBuckets.put(oldLoginIp, rateLimitingConfig.createLoginBucket());
    registrationRateLimitBuckets.put(oldRegistrationIp, rateLimitingConfig.createRegistrationBucket());
    generalApiRateLimitBuckets.put(oldGeneralApiIp, rateLimitingConfig.createGeneralApiBucket());
    refreshRateLimitBuckets.put(oldRefreshIp, rateLimitingConfig.createRefreshBucket());
    
    // Create recent buckets
    loginRateLimitBuckets.put(recentLoginIp, rateLimitingConfig.createLoginBucket());
    registrationRateLimitBuckets.put(recentRegistrationIp, rateLimitingConfig.createRegistrationBucket());
    generalApiRateLimitBuckets.put(recentGeneralApiIp, rateLimitingConfig.createGeneralApiBucket());
    refreshRateLimitBuckets.put(recentRefreshIp, rateLimitingConfig.createRefreshBucket());
    
    // Record access for all
    cleanupJob.recordBucketAccess(oldLoginIp, "login");
    cleanupJob.recordBucketAccess(recentLoginIp, "login");
    cleanupJob.recordBucketAccess(oldRegistrationIp, "registration");
    cleanupJob.recordBucketAccess(recentRegistrationIp, "registration");
    cleanupJob.recordBucketAccess(oldGeneralApiIp, "generalApi");
    cleanupJob.recordBucketAccess(recentGeneralApiIp, "generalApi");
    cleanupJob.recordBucketAccess(oldRefreshIp, "refresh");
    cleanupJob.recordBucketAccess(recentRefreshIp, "refresh");
    
    // Set old timestamps (2 hours ago)
    long twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000);
    setOldTimestamp("loginLastAccess", oldLoginIp, twoHoursAgo);
    setOldTimestamp("registrationLastAccess", oldRegistrationIp, twoHoursAgo);
    setOldTimestamp("generalApiLastAccess", oldGeneralApiIp, twoHoursAgo);
    setOldTimestamp("refreshLastAccess", oldRefreshIp, twoHoursAgo);
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - old buckets should be removed, recent buckets should remain
    assertThat(loginRateLimitBuckets).doesNotContainKey(oldLoginIp);
    assertThat(loginRateLimitBuckets).containsKey(recentLoginIp);
    assertThat(registrationRateLimitBuckets).doesNotContainKey(oldRegistrationIp);
    assertThat(registrationRateLimitBuckets).containsKey(recentRegistrationIp);
    assertThat(generalApiRateLimitBuckets).doesNotContainKey(oldGeneralApiIp);
    assertThat(generalApiRateLimitBuckets).containsKey(recentGeneralApiIp);
    assertThat(refreshRateLimitBuckets).doesNotContainKey(oldRefreshIp);
    assertThat(refreshRateLimitBuckets).containsKey(recentRefreshIp);
  }
  
  /**
   * Helper method to set old timestamp in a lastAccess map using reflection.
   */
  private void setOldTimestamp(String fieldName, String ipAddress, long timestamp) throws Exception {
    Field field = RateLimitingBucketCleanupJob.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Long> lastAccessMap = (Map<String, Long>) field.get(cleanupJob);
    lastAccessMap.put(ipAddress, timestamp);
  }
  
  @Test
  @DisplayName("Should handle empty bucket maps gracefully")
  void shouldHandleEmptyBucketMaps() {
    // Given - empty maps
    
    // When - should not throw exception
    cleanupJob.cleanupUnusedBuckets();
    
    // Then
    assertThat(loginRateLimitBuckets).isEmpty();
    assertThat(registrationRateLimitBuckets).isEmpty();
    assertThat(generalApiRateLimitBuckets).isEmpty();
    assertThat(refreshRateLimitBuckets).isEmpty();
  }
  
  @Test
  @DisplayName("Should cleanup buckets without tracked access")
  void shouldCleanupBucketsWithoutTrackedAccess() {
    // Given - bucket exists but access was never tracked
    // This simulates an edge case where a bucket was created but recordBucketAccess
    // was never called (e.g., if bucketCleanupJob was null when bucket was created)
    String untrackedIp = "192.168.1.40";
    String trackedIp = "192.168.1.41";
    
    Bucket untrackedBucket = rateLimitingConfig.createLoginBucket();
    Bucket trackedBucket = rateLimitingConfig.createLoginBucket();
    loginRateLimitBuckets.put(untrackedIp, untrackedBucket);
    loginRateLimitBuckets.put(trackedIp, trackedBucket);
    
    // Only track access for one bucket
    cleanupJob.recordBucketAccess(trackedIp, "login");
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - untracked bucket should be removed (treated as old),
    // tracked bucket should remain (recently accessed)
    assertThat(loginRateLimitBuckets).doesNotContainKey(untrackedIp);
    assertThat(loginRateLimitBuckets).containsKey(trackedIp);
  }
  
  @Test
  @DisplayName("Should handle multiple IPs per bucket type")
  void shouldHandleMultipleIpsPerBucketType() {
    // Given
    String ip1 = "192.168.1.50";
    String ip2 = "192.168.1.51";
    String ip3 = "192.168.1.52";
    
    loginRateLimitBuckets.put(ip1, rateLimitingConfig.createLoginBucket());
    loginRateLimitBuckets.put(ip2, rateLimitingConfig.createLoginBucket());
    loginRateLimitBuckets.put(ip3, rateLimitingConfig.createLoginBucket());
    
    // Record access for all
    cleanupJob.recordBucketAccess(ip1, "login");
    cleanupJob.recordBucketAccess(ip2, "login");
    cleanupJob.recordBucketAccess(ip3, "login");
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - all should still exist
    assertThat(loginRateLimitBuckets).hasSize(3);
    assertThat(loginRateLimitBuckets).containsKeys(ip1, ip2, ip3);
  }
  
  @Test
  @DisplayName("Should update access time on multiple accesses")
  void shouldUpdateAccessTimeOnMultipleAccesses() throws InterruptedException {
    // Given
    String ip = "192.168.1.60";
    Bucket bucket = rateLimitingConfig.createLoginBucket();
    loginRateLimitBuckets.put(ip, bucket);
    
    // Record first access
    cleanupJob.recordBucketAccess(ip, "login");
    
    // Wait a bit
    Thread.sleep(10);
    
    // Record second access (should update timestamp)
    cleanupJob.recordBucketAccess(ip, "login");
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - bucket should still exist (most recently accessed)
    assertThat(loginRateLimitBuckets).containsKey(ip);
  }
  
  // ==================== Integration-style Tests ====================
  
  @Test
  @DisplayName("Should cleanup scenario: mix of old, recent, and untracked buckets")
  void shouldCleanupMixOfOldRecentAndUntrackedBuckets() throws Exception {
    // Given - create multiple buckets with mix of old, recent, and untracked
    String recentLoginIp = "192.168.1.70";
    String oldRegistrationIp = "192.168.1.71";
    String recentGeneralApiIp = "192.168.1.72";
    String oldRefreshIp = "192.168.1.73";
    String untrackedLoginIp = "192.168.1.74";
    
    loginRateLimitBuckets.put(recentLoginIp, rateLimitingConfig.createLoginBucket());
    loginRateLimitBuckets.put(untrackedLoginIp, rateLimitingConfig.createLoginBucket());
    registrationRateLimitBuckets.put(oldRegistrationIp, rateLimitingConfig.createRegistrationBucket());
    generalApiRateLimitBuckets.put(recentGeneralApiIp, rateLimitingConfig.createGeneralApiBucket());
    refreshRateLimitBuckets.put(oldRefreshIp, rateLimitingConfig.createRefreshBucket());
    
    // Record access for tracked buckets
    cleanupJob.recordBucketAccess(recentLoginIp, "login");
    // Don't record access for untrackedLoginIp to simulate untracked bucket
    cleanupJob.recordBucketAccess(oldRegistrationIp, "registration");
    cleanupJob.recordBucketAccess(recentGeneralApiIp, "generalApi");
    cleanupJob.recordBucketAccess(oldRefreshIp, "refresh");
    
    // Set old timestamps for old buckets (2 hours ago)
    long twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000);
    setOldTimestamp("registrationLastAccess", oldRegistrationIp, twoHoursAgo);
    setOldTimestamp("refreshLastAccess", oldRefreshIp, twoHoursAgo);
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - recent buckets should exist, old and untracked buckets should be removed
    assertThat(loginRateLimitBuckets).containsKey(recentLoginIp);
    assertThat(loginRateLimitBuckets).doesNotContainKey(untrackedLoginIp);
    assertThat(registrationRateLimitBuckets).doesNotContainKey(oldRegistrationIp);
    assertThat(generalApiRateLimitBuckets).containsKey(recentGeneralApiIp);
    assertThat(refreshRateLimitBuckets).doesNotContainKey(oldRefreshIp);
  }
  
  @Test
  @DisplayName("Should handle cleanup with no buckets to remove")
  void shouldHandleCleanupWithNoBucketsToRemove() {
    // Given - empty maps or only recent buckets
    String ip = "192.168.1.80";
    loginRateLimitBuckets.put(ip, rateLimitingConfig.createLoginBucket());
    cleanupJob.recordBucketAccess(ip, "login");
    
    // When
    cleanupJob.cleanupUnusedBuckets();
    
    // Then - should complete without errors
    assertThat(loginRateLimitBuckets).containsKey(ip);
  }
}
