package com.vallexia.security.job;

import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scheduled job for cleaning up unused rate limiting buckets to prevent memory leaks.
 * Removes buckets that haven't been accessed in the last hour.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Component
public class RateLimitingBucketCleanupJob {
  
  private static final long CLEANUP_INTERVAL_HOURS = 1;
  private static final long CLEANUP_THRESHOLD_MS = CLEANUP_INTERVAL_HOURS * 60 * 60 * 1000;
  
  private final Map<String, Bucket> loginRateLimitBuckets;
  private final Map<String, Bucket> registrationRateLimitBuckets;
  private final Map<String, Bucket> generalApiRateLimitBuckets;
  private final Map<String, Bucket> refreshRateLimitBuckets;
  
  // Track last access time for each bucket map
  private final Map<String, Long> loginLastAccess = new ConcurrentHashMap<>();
  private final Map<String, Long> registrationLastAccess = new ConcurrentHashMap<>();
  private final Map<String, Long> generalApiLastAccess = new ConcurrentHashMap<>();
  private final Map<String, Long> refreshLastAccess = new ConcurrentHashMap<>();
  
  /**
   * Constructor with dependency injection.
   * 
   * @param loginRateLimitBuckets login rate limit buckets
   * @param registrationRateLimitBuckets registration rate limit buckets
   * @param generalApiRateLimitBuckets general API rate limit buckets
   * @param refreshRateLimitBuckets refresh token rate limit buckets
   */
  public RateLimitingBucketCleanupJob(
      Map<String, Bucket> loginRateLimitBuckets,
      Map<String, Bucket> registrationRateLimitBuckets,
      Map<String, Bucket> generalApiRateLimitBuckets,
      Map<String, Bucket> refreshRateLimitBuckets) {
    this.loginRateLimitBuckets = loginRateLimitBuckets;
    this.registrationRateLimitBuckets = registrationRateLimitBuckets;
    this.generalApiRateLimitBuckets = generalApiRateLimitBuckets;
    this.refreshRateLimitBuckets = refreshRateLimitBuckets;
  }
  
  /**
   * Cleanup unused rate limiting buckets.
   * Runs every hour to remove buckets that haven't been accessed in the last hour.
   */
  @Scheduled(fixedRate = 3600000) // Run every hour
  public void cleanupUnusedBuckets() {
    try {
      long cutoffTime = System.currentTimeMillis() - CLEANUP_THRESHOLD_MS;
      
      log.debug("Starting rate limiting bucket cleanup (cutoff: {}ms ago)", CLEANUP_THRESHOLD_MS);
      
      int loginRemoved = cleanupBucketMap(loginRateLimitBuckets, loginLastAccess, "login", cutoffTime);
      int registrationRemoved = cleanupBucketMap(registrationRateLimitBuckets, registrationLastAccess, 
          "registration", cutoffTime);
      int generalApiRemoved = cleanupBucketMap(generalApiRateLimitBuckets, generalApiLastAccess, 
          "general API", cutoffTime);
      int refreshRemoved = cleanupBucketMap(refreshRateLimitBuckets, refreshLastAccess, 
          "refresh", cutoffTime);
      
      int totalRemoved = loginRemoved + registrationRemoved + generalApiRemoved + refreshRemoved;
      
      if (totalRemoved > 0) {
        log.info("Rate limiting bucket cleanup completed: removed {} unused buckets (login: {}, "
            + "registration: {}, general API: {}, refresh: {})", 
            totalRemoved, loginRemoved, registrationRemoved, generalApiRemoved, refreshRemoved);
      } else {
        log.debug("No unused rate limiting buckets to cleanup");
      }
      
    } catch (Exception e) {
      log.error("Error during rate limiting bucket cleanup: {}", e.getMessage(), e);
      // Don't throw exception to prevent job from failing
    }
  }
  
  /**
   * Cleanup a specific bucket map by removing entries that haven't been accessed recently.
   * Also removes buckets that exist but were never tracked (edge case where recordBucketAccess
   * was never called, e.g., if bucketCleanupJob was null when bucket was created).
   * 
   * @param buckets the bucket map to clean
   * @param lastAccessMap the last access time tracking map
   * @param bucketType the type name for logging
   * @param cutoffTime the cutoff time in milliseconds
   * @return number of buckets removed
   */
  private int cleanupBucketMap(Map<String, Bucket> buckets, Map<String, Long> lastAccessMap,
                               String bucketType, long cutoffTime) {
    int removed = 0;
    
    // First, remove buckets that are tracked but haven't been accessed recently
    Iterator<Map.Entry<String, Long>> iterator = lastAccessMap.entrySet().iterator();
    
    while (iterator.hasNext()) {
      Map.Entry<String, Long> entry = iterator.next();
      String ip = entry.getKey();
      Long lastAccess = entry.getValue();
      
      if (lastAccess != null && lastAccess < cutoffTime) {
        buckets.remove(ip);
        iterator.remove();
        removed++;
        log.trace("Removed unused {} bucket for IP: {} (last access: {}ms ago)", 
            bucketType, ip, System.currentTimeMillis() - lastAccess);
      }
    }
    
    // Second, remove buckets that exist but were never tracked (treat as old)
    // This handles edge cases where buckets were created but recordBucketAccess was never called
    Iterator<Map.Entry<String, Bucket>> bucketIterator = buckets.entrySet().iterator();
    while (bucketIterator.hasNext()) {
      Map.Entry<String, Bucket> entry = bucketIterator.next();
      String ip = entry.getKey();
      
      // If this IP is not in the lastAccess map, it was never tracked
      // Treat it as old and remove it
      if (!lastAccessMap.containsKey(ip)) {
        bucketIterator.remove();
        removed++;
        log.trace("Removed untracked {} bucket for IP: {} (never had access recorded)", 
            bucketType, ip);
      }
    }
    
    return removed;
  }
  
  /**
   * Record that a bucket was accessed. Called by RateLimitingFilter when a bucket is used.
   * 
   * @param ipAddress the IP address
   * @param bucketType the type of bucket (login, registration, generalApi, refresh)
   */
  public void recordBucketAccess(String ipAddress, String bucketType) {
    if (ipAddress == null || ipAddress.trim().isEmpty()) {
      return;
    }
    
    long currentTime = System.currentTimeMillis();
    
    switch (bucketType) {
      case "login":
        loginLastAccess.put(ipAddress, currentTime);
        break;
      case "registration":
        registrationLastAccess.put(ipAddress, currentTime);
        break;
      case "generalApi":
        generalApiLastAccess.put(ipAddress, currentTime);
        break;
      case "refresh":
        refreshLastAccess.put(ipAddress, currentTime);
        break;
      default:
        log.warn("Unknown bucket type for access tracking: {}", bucketType);
    }
  }
}
