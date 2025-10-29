package com.vallexia.config.audit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for audit settings.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@ConfigurationProperties(prefix = "app.audit")
public class AuditProperties {
  
  /**
   * Comma-separated list of trusted proxy IP addresses.
   * Used for extracting real client IP from X-Forwarded-For headers.
   * Example: "172.17.0.1,10.0.0.1"
   */
  private String trustedProxies = "";
  
  /**
   * Audit log retention period in days.
   * Audit logs older than this will be automatically deleted.
   * Default: 90 days
   */
  private int retentionDays = 90;
  
  /**
   * Path for audit fallback log file.
   * Used when database audit logging fails.
   * Default: deployment/logs/audit-fallback.log
   */
  private String fallbackLogPath = "deployment/logs/audit-fallback.log";
}
