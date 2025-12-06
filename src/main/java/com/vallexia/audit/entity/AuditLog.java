package com.vallexia.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import com.vallexia.audit.entity.enums.EventType;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking security and user events.
 * This entity is immutable after creation to maintain audit trail integrity.
 * Immutability is enforced at multiple levels:
 * - Code level: fields are final (except id and timestamp which JPA manages)
 * - Framework level: @Immutable annotation prevents Hibernate updates
 * - Database level: triggers prevent updates/deletes
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Entity
@Table(name = "audit_logs")
@Getter
@ToString
@Immutable
public class AuditLog {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false)
  private Long id;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private final EventType eventType;
  
  @Column(nullable = false, updatable = false)
  private final String eventDescription;
  
  @Column(updatable = false)
  private final Long userId;
  
  @Column(updatable = false)
  private final String username;
  
  @Column(updatable = false)
  private final String ipAddress;
  
  @Column(updatable = false)
  private final String userAgent;
  
  @Column(updatable = false)
  private final String requestMethod;
  
  @Column(updatable = false)
  private final String requestUri;
  
  @Column(updatable = false)
  private final Integer responseStatus;
  
  @Column(columnDefinition = "TEXT", updatable = false)
  private final String details;
  
  @Column(updatable = false)
  private final Boolean success;
  
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime timestamp;
  
  /**
   * No-args constructor required by JPA.
   * Initializes all fields to null. This should only be used by JPA.
   * The database constraints will ensure required fields are not null.
   */
  protected AuditLog() {
    this.eventType = null;
    this.eventDescription = null;
    this.userId = null;
    this.username = null;
    this.ipAddress = null;
    this.userAgent = null;
    this.requestMethod = null;
    this.requestUri = null;
    this.responseStatus = null;
    this.details = null;
    this.success = null;
  }
  
  /**
   * Constructor for authentication events with HTTP request context.
   * 
   * @param eventType the type of event
   * @param description event description
   * @param userId user ID
   * @param username username
   * @param ipAddress client IP address
   * @param userAgent user agent string
   * @param requestMethod HTTP request method
   * @param requestUri HTTP request URI
   * @param success whether the operation was successful
   */
  public AuditLog(EventType eventType, String description, Long userId, String username, 
                  String ipAddress, String userAgent, String requestMethod, 
                  String requestUri, Boolean success) {
    this.eventType = eventType;
    this.eventDescription = description;
    this.userId = userId;
    this.username = username;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.requestMethod = requestMethod;
    this.requestUri = requestUri;
    this.success = success;
    this.responseStatus = null;
    this.details = null;
  }
  
  /**
   * Constructor for API access events with response status.
   * 
   * @param eventType the type of event
   * @param description event description
   * @param userId user ID
   * @param username username
   * @param ipAddress client IP address
   * @param userAgent user agent string
   * @param requestMethod HTTP request method
   * @param requestUri HTTP request URI
   * @param responseStatus HTTP response status code
   * @param success whether the operation was successful
   */
  public AuditLog(EventType eventType, String description, Long userId, String username, 
                  String ipAddress, String userAgent, String requestMethod, 
                  String requestUri, Integer responseStatus, Boolean success) {
    this.eventType = eventType;
    this.eventDescription = description;
    this.userId = userId;
    this.username = username;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.requestMethod = requestMethod;
    this.requestUri = requestUri;
    this.responseStatus = responseStatus;
    this.success = success;
    this.details = null;
  }
  
  /**
   * Constructor for simple events without HTTP request context.
   * Used for service-layer events that don't have HTTP context.
   * 
   * @param eventType the type of event
   * @param description event description
   * @param userId user ID
   * @param success whether the operation was successful
   */
  public AuditLog(EventType eventType, String description, Long userId, Boolean success) {
    this.eventType = eventType;
    this.eventDescription = description;
    this.userId = userId;
    this.success = success;
    this.username = null;
    this.ipAddress = null;
    this.userAgent = null;
    this.requestMethod = null;
    this.requestUri = null;
    this.responseStatus = null;
    this.details = null;
  }
  
  /**
   * Constructor for events with optional details field.
   * 
   * @param eventType the type of event
   * @param description event description
   * @param userId user ID
   * @param username username
   * @param ipAddress client IP address
   * @param userAgent user agent string
   * @param requestMethod HTTP request method
   * @param requestUri HTTP request URI
   * @param responseStatus HTTP response status code
   * @param details additional details (TEXT field)
   * @param success whether the operation was successful
   */
  public AuditLog(EventType eventType, String description, Long userId, String username, 
                  String ipAddress, String userAgent, String requestMethod, 
                  String requestUri, Integer responseStatus, String details, Boolean success) {
    this.eventType = eventType;
    this.eventDescription = description;
    this.userId = userId;
    this.username = username;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.requestMethod = requestMethod;
    this.requestUri = requestUri;
    this.responseStatus = responseStatus;
    this.details = details;
    this.success = success;
  }
}
