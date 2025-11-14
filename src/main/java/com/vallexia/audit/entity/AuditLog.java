package com.vallexia.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import com.vallexia.audit.entity.enums.EventType;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking security and user events.
 * This entity is immutable after creation to maintain audit trail integrity.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class AuditLog {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false)
  private Long id;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private EventType eventType;
  
  @Column(nullable = false, updatable = false)
  private String eventDescription;
  
  @Column(updatable = false)
  private Long userId;
  
  @Column(updatable = false)
  private String username;
  
  @Column(updatable = false)
  private String ipAddress;
  
  @Column(updatable = false)
  private String userAgent;
  
  @Column(updatable = false)
  private String requestMethod;
  
  @Column(updatable = false)
  private String requestUri;
  
  @Column(updatable = false)
  private Integer responseStatus;
  
  @Column(columnDefinition = "TEXT", updatable = false)
  private String details;
  
  @Column(updatable = false)
  private Boolean success;
  
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime timestamp;
  
  /**
   * Constructor for common events.
   * 
   * @param eventType the type of event
   * @param description event description
   * @param userId user ID
   * @param username username
   * @param ipAddress client IP address
   * @param userAgent user agent string
   * @param success whether the operation was successful
   */
  public AuditLog(EventType eventType, String description, Long userId, String username, 
                  String ipAddress, String userAgent, Boolean success) {
    this.eventType = eventType;
    this.eventDescription = description;
    this.userId = userId;
    this.username = username;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.success = success;
  }
}
