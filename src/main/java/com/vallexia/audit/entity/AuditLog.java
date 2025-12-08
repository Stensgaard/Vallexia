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
   * Parameter object for HTTP request context.
   * Groups HTTP-related fields to reduce constructor parameter count.
   */
  public static class HttpRequestContext {
    private final String ipAddress;
    private final String userAgent;
    private final String requestMethod;
    private final String requestUri;
    private final Integer responseStatus;
    
    public HttpRequestContext(String ipAddress, String userAgent, String requestMethod, 
                             String requestUri, Integer responseStatus) {
      this.ipAddress = ipAddress;
      this.userAgent = userAgent;
      this.requestMethod = requestMethod;
      this.requestUri = requestUri;
      this.responseStatus = responseStatus;
    }
    
    public String getIpAddress() {
      return ipAddress;
    }
    
    public String getUserAgent() {
      return userAgent;
    }
    
    public String getRequestMethod() {
      return requestMethod;
    }
    
    public String getRequestUri() {
      return requestUri;
    }
    
    public Integer getResponseStatus() {
      return responseStatus;
    }
  }
  
  /**
   * Parameter object for user context.
   * Groups user-related fields to reduce constructor parameter count.
   */
  public static class UserContext {
    private final Long userId;
    private final String username;
    
    public UserContext(Long userId, String username) {
      this.userId = userId;
      this.username = username;
    }
    
    public Long getUserId() {
      return userId;
    }
    
    public String getUsername() {
      return username;
    }
  }
  
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
   * Constructor for events with HTTP request context.
   * Handles both authentication events and API access events (with or without response status).
   * 
   * @param eventType the type of event
   * @param description event description
   * @param userContext user context containing userId and username
   * @param httpContext HTTP request context containing IP, user agent, method, URI, and optionally response status
   * @param success whether the operation was successful
   */
  public AuditLog(EventType eventType, String description, UserContext userContext, 
                  HttpRequestContext httpContext, Boolean success) {
    this.eventType = eventType;
    this.eventDescription = description;
    this.userId = userContext != null ? userContext.getUserId() : null;
    this.username = userContext != null ? userContext.getUsername() : null;
    this.ipAddress = httpContext != null ? httpContext.getIpAddress() : null;
    this.userAgent = httpContext != null ? httpContext.getUserAgent() : null;
    this.requestMethod = httpContext != null ? httpContext.getRequestMethod() : null;
    this.requestUri = httpContext != null ? httpContext.getRequestUri() : null;
    this.responseStatus = httpContext != null ? httpContext.getResponseStatus() : null;
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
   * @param userContext user context containing userId and username
   * @param httpContext HTTP request context containing IP, user agent, method, URI, response status
   * @param details additional details (TEXT field)
   * @param success whether the operation was successful
   */
  public AuditLog(EventType eventType, String description, UserContext userContext, 
                  HttpRequestContext httpContext, String details, Boolean success) {
    this.eventType = eventType;
    this.eventDescription = description;
    this.userId = userContext != null ? userContext.getUserId() : null;
    this.username = userContext != null ? userContext.getUsername() : null;
    this.ipAddress = httpContext != null ? httpContext.getIpAddress() : null;
    this.userAgent = httpContext != null ? httpContext.getUserAgent() : null;
    this.requestMethod = httpContext != null ? httpContext.getRequestMethod() : null;
    this.requestUri = httpContext != null ? httpContext.getRequestUri() : null;
    this.responseStatus = httpContext != null ? httpContext.getResponseStatus() : null;
    this.details = details;
    this.success = success;
  }
}
