package com.vallexia.config.audit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for audit settings.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Data
@Validated
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
  @Min(value = 1, message = "Audit retention days must be at least 1")
  private int retentionDays = 90;
  
  /**
   * Path for audit fallback log file.
   * Used when database audit logging fails.
   * Default: deployment/logs/audit-fallback.log
   */
  @NotBlank(message = "Audit fallback log path must not be blank")
  private String fallbackLogPath = "deployment/logs/audit-fallback.log";
}
