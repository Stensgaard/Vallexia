package com.vallexia.audit.job;

import com.vallexia.audit.repository.AuditLogRepository;
import com.vallexia.config.audit.AuditProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled job for managing audit log retention policy.
 * Archives or deletes old audit logs according to configured retention period.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
   */
  public AuditLogRetentionJob(
      AuditLogRepository auditLogRepository,
      AuditProperties auditProperties) {
    this.auditLogRepository = auditLogRepository;
    this.retentionDays = auditProperties.getRetentionDays();
    log.info("Audit log retention job initialized with {} days retention period", retentionDays);
  }
  
  /**
   * Cleanup old audit logs.
   * Runs daily at 2 AM.
   * 
   * TODO: In production, consider archiving logs to cold storage before deletion
   * to maintain compliance with longer retention requirements.
   */
  @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
  @Transactional
  public void cleanupOldAuditLogs() {
    try {
      LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
      
      log.info("Starting audit log cleanup for logs older than {}", cutoffDate);
      
      // Find logs to delete
      var oldLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
          LocalDateTime.MIN, cutoffDate);
      
      int count = oldLogs.size();
      
      if (count > 0) {
        // TODO: Archive to file or external storage before deletion
        // archiveAuditLogs(oldLogs);
        
        // Delete old logs
        auditLogRepository.deleteAll(oldLogs);
        
        log.info("Deleted {} audit logs older than {} days", count, retentionDays);
      } else {
        log.debug("No audit logs to cleanup");
      }
      
    } catch (Exception e) {
      log.error("Error during audit log cleanup: {}", e.getMessage(), e);
      // Don't throw exception to prevent job from failing
    }
  }
  
  /**
   * Get the configured retention period in days.
   * 
   * @return retention period in days
   */
  public int getRetentionDays() {
    return retentionDays;
  }
  
  // TODO: Implement archival to cold storage
  // private void archiveAuditLogs(List<AuditLog> logs) {
  //   // Archive to S3, file system, or other cold storage
  // }
}
