package com.vallexia.audit.dto;

import com.vallexia.audit.entity.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for audit log information.
 * Note: The details field is intentionally excluded for security reasons.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
  
  private Long id;
  private EventType eventType;
  private String eventDescription;
  private Long userId;
  private String username;
  private String ipAddress;
  private String userAgent;
  private String requestMethod;
  private String requestUri;
  private Integer responseStatus;
  private Boolean success;
  private LocalDateTime timestamp;
}
