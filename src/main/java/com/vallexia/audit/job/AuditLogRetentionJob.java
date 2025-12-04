package com.vallexia.audit.job;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.repository.AuditLogRepository;
import com.vallexia.config.audit.AuditProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job for managing audit log retention policy.
 * Archives or deletes old audit logs according to configured retention period.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Slf4j
@Component
public class AuditLogRetentionJob {
  
  private final AuditLogRepository auditLogRepository;
  private final int retentionDays;
  
  /**
   * Constructor with configurable retention period.
   * 
   * @param auditLogRepository the audit log repository
   * @param auditProperties audit configuration properties
   * @throws IllegalArgumentException if retentionDays is less than 1
   */
  public AuditLogRetentionJob(
      AuditLogRepository auditLogRepository,
      AuditProperties auditProperties) {
    this.auditLogRepository = auditLogRepository;
    int configuredRetentionDays = auditProperties.getRetentionDays();
    
    if (configuredRetentionDays < 1) {
      throw new IllegalArgumentException(
          "Audit log retention days must be at least 1, but was: " + configuredRetentionDays);
    }
    
    this.retentionDays = configuredRetentionDays;
    log.info("Audit log retention job initialized with {} days retention period", retentionDays);
  }
  
  /**
   * Cleanup old audit logs.
   * Runs daily at 2 AM.
   * Uses efficient direct SQL deletion for better performance on large datasets.
   * Falls back to batched deletion if direct deletion fails.
   */
  @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
  @Transactional
  public void cleanupOldAuditLogs() {
    try {
      LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
      
      log.info("Starting audit log cleanup for logs older than {}", cutoffDate);
      
      // Count records to be deleted for logging
      long countToDelete = auditLogRepository.countByTimestampBefore(cutoffDate);
      
      if (countToDelete > 0) {
        log.info("Found {} audit logs to delete", countToDelete);
        
        // Use efficient direct SQL deletion
        int deletedCount = auditLogRepository.deleteByTimestampBefore(cutoffDate);
        
        log.info("Deleted {} audit logs older than {} days (retention period)", 
            deletedCount, retentionDays);
      } else {
        log.debug("No audit logs to cleanup");
      }
      
    } catch (Exception e) {
      log.error("Error during audit log cleanup, attempting batched deletion: {}", 
          e.getMessage(), e);
      
      // Fallback to batched deletion if direct deletion fails
      try {
        performBatchedDeletion();
      } catch (Exception fallbackException) {
        log.error("Batched deletion also failed: {}", fallbackException.getMessage(), 
            fallbackException);
        // Don't throw exception to prevent job from failing
      }
    }
  }
  
  /**
   * Fallback method for batched deletion when direct SQL deletion fails.
   * Processes deletions in batches to avoid memory issues with large datasets.
   */
  private void performBatchedDeletion() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
    int batchSize = 1000;
    int totalDeleted = 0;
    Pageable pageable = PageRequest.of(0, batchSize);
    
    log.info("Performing batched deletion for logs older than {}", cutoffDate);
    
    Page<AuditLog> page;
    do {
      page = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
          LocalDateTime.MIN, cutoffDate, pageable);
      
      if (!page.isEmpty()) {
        List<AuditLog> batch = page.getContent();
        auditLogRepository.deleteAll(batch);
        totalDeleted += batch.size();
        log.debug("Deleted batch of {} audit logs (total so far: {})", 
            batch.size(), totalDeleted);
      }
      
      pageable = page.nextPageable();
    } while (page.hasNext());
    
    log.info("Batched deletion completed: deleted {} audit logs", totalDeleted);
  }
  
  /**
   * Get the configured retention period in days.
   * 
   * @return retention period in days
   */
  public int getRetentionDays() {
    return retentionDays;
  }
}
