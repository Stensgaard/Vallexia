package com.vallexia.audit.service;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.repository.AuditLogRepository;
import com.vallexia.config.audit.AuditProperties;
import com.vallexia.security.util.IpAddressExtractor;
import com.vallexia.security.AuthenticationHelper;
import com.vallexia.security.util.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing audit logging with security hardening.
 * Implements input sanitization, access control, and fallback logging.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Slf4j
@Service
public class AuditService {
  
  private final AuditLogRepository auditLogRepository;
  private final InputSanitizer inputSanitizer;
  private final IpAddressExtractor ipAddressExtractor;
  private final AuthenticationHelper authenticationHelper;
  private final AuditProperties auditProperties;
  
  private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
      DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
  
  /**
   * Constructor for dependency injection.
   * 
   * @param auditLogRepository the audit log repository
   * @param inputSanitizer input sanitization utility
   * @param ipAddressExtractor IP address extraction utility
   * @param authenticationHelper authentication helper for access control
   * @param auditProperties audit configuration properties
   */
  public AuditService(
      AuditLogRepository auditLogRepository,
      InputSanitizer inputSanitizer,
      IpAddressExtractor ipAddressExtractor,
      AuthenticationHelper authenticationHelper,
      AuditProperties auditProperties) {
    this.auditLogRepository = auditLogRepository;
    this.inputSanitizer = inputSanitizer;
    this.ipAddressExtractor = ipAddressExtractor;
    this.authenticationHelper = authenticationHelper;
    this.auditProperties = auditProperties;
  }
  
  /**
   * Log an authentication event.
   * Uses REQUIRES_NEW propagation to ensure audit logs are committed even if
   * the calling transaction rolls back (e.g., on authentication failures).
   * 
   * @param eventType the type of event
   * @param description event description
   * @param userId user ID
   * @param username username
   * @param request HTTP request for extracting metadata
   * @param success whether the operation was successful
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logAuthenticationEvent(
      EventType eventType,
      String description,
      Long userId,
      String username,
      HttpServletRequest request,
      Boolean success) {
    try {
      // Sanitize inputs
      String sanitizedDescription = inputSanitizer.sanitizeDescription(description);
      String sanitizedUsername = inputSanitizer.sanitizeUsername(username);
      String clientIp = ipAddressExtractor.extractClientIp(request);
      String sanitizedIp = inputSanitizer.sanitizeIpAddress(clientIp);
      String sanitizedUserAgent = inputSanitizer.sanitizeUserAgent(request.getHeader("User-Agent"));
      String sanitizedRequestUri = inputSanitizer.sanitizeRequestUri(request.getRequestURI());
      
      AuditLog.UserContext userContext = new AuditLog.UserContext(userId, sanitizedUsername);
      AuditLog.HttpRequestContext httpContext = new AuditLog.HttpRequestContext(
          sanitizedIp,
          sanitizedUserAgent,
          request.getMethod(),
          sanitizedRequestUri,
          null
      );
      
      AuditLog auditLog = new AuditLog(
          eventType,
          sanitizedDescription,
          userContext,
          httpContext,
          success
      );
      
      auditLogRepository.saveAndFlush(auditLog);
      log.debug("Audit log saved: {} for user {}", eventType, sanitizedUsername);
      
    } catch (Exception e) {
      log.error("Error saving audit log: {}", e.getMessage(), e);
      // Fallback to file logging
      logToFallbackFile(eventType, description, userId, username, success, e);
      // Alert on audit failure
      alertAuditFailure(eventType, e);
    }
  }
  
  /**
   * Log a profile update event.
   * 
   * @param userId user ID
   * @param username username
   * @param description event description
   * @param request HTTP request
   */
  @Transactional
  public void logProfileUpdateEvent(
      Long userId,
      String username,
      String description,
      HttpServletRequest request) {
    logAuthenticationEvent(
        EventType.PROFILE_UPDATE,
        description,
        userId,
        username,
        request,
        true
    );
  }
  
  /**
   * Log a simple event without HTTP request context (for service layer).
   * 
   * @param eventType the event type
   * @param userId the user ID
   * @param description the event description
   */
  @Transactional
  public void logEvent(EventType eventType, Long userId, String description) {
    try {
      String sanitizedDescription = inputSanitizer.sanitizeDescription(description);
      
      AuditLog auditLog = new AuditLog(
          eventType,
          sanitizedDescription,
          userId,
          true
      );
      
      auditLogRepository.save(auditLog);
      log.debug("Audit log saved: {} for user ID {}", eventType, userId);
      
    } catch (Exception e) {
      log.error("Error saving audit log: {}", e.getMessage(), e);
      logToFallbackFile(eventType, description, userId, null, true, e);
      alertAuditFailure(eventType, e);
    }
  }
  
  /**
   * Log a security violation event.
   * 
   * @param description violation description
   * @param username username (may be null for anonymous)
   * @param request HTTP request
   */
  @Transactional
  public void logSecurityViolation(String description, String username, HttpServletRequest request) {
    logAuthenticationEvent(
        EventType.SECURITY_VIOLATION,
        description,
        null,
        username,
        request,
        false
    );
  }
  
  /**
   * Log an API access event.
   * 
   * @param description access description
   * @param userId user ID
   * @param username username
   * @param request HTTP request
   * @param responseStatus HTTP response status code
   */
  @Transactional
  public void logApiAccess(
      String description,
      Long userId,
      String username,
      HttpServletRequest request,
      Integer responseStatus) {
    try {
      String sanitizedDescription = inputSanitizer.sanitizeDescription(description);
      String sanitizedUsername = inputSanitizer.sanitizeUsername(username);
      String clientIp = ipAddressExtractor.extractClientIp(request);
      String sanitizedIp = inputSanitizer.sanitizeIpAddress(clientIp);
      String sanitizedUserAgent = inputSanitizer.sanitizeUserAgent(request.getHeader("User-Agent"));
      String sanitizedRequestUri = inputSanitizer.sanitizeRequestUri(request.getRequestURI());
      
      AuditLog.UserContext userContext = new AuditLog.UserContext(userId, sanitizedUsername);
      AuditLog.HttpRequestContext httpContext = new AuditLog.HttpRequestContext(
          sanitizedIp,
          sanitizedUserAgent,
          request.getMethod(),
          sanitizedRequestUri,
          responseStatus
      );
      
      AuditLog auditLog = new AuditLog(
          EventType.API_ACCESS,
          sanitizedDescription,
          userContext,
          httpContext,
          responseStatus != null && responseStatus < 400
      );
      
      auditLogRepository.save(auditLog);
      
    } catch (Exception e) {
      log.error("Error saving API access audit log: {}", e.getMessage(), e);
      logToFallbackFile(EventType.API_ACCESS, description, userId, username, true, e);
      alertAuditFailure(EventType.API_ACCESS, e);
    }
  }
  
  /**
   * Get audit logs for a user with access control.
   * Users can only view their own logs unless they have admin role.
   * 
   * @param userId user ID to query
   * @return list of audit logs
   * @throws AccessDeniedException if user doesn't have permission
   */
  @Transactional(readOnly = true)
  public List<AuditLog> getUserAuditLogs(Long userId) {
    validateAuditLogAccess(userId);
    return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
  }
  
  /**
   * Get audit logs for a user with pagination and access control.
   * 
   * @param userId user ID to query
   * @param pageable pagination parameters
   * @return page of audit logs
   * @throws AccessDeniedException if user doesn't have permission
   */
  @Transactional(readOnly = true)
  public Page<AuditLog> getUserAuditLogs(Long userId, Pageable pageable) {
    validateAuditLogAccess(userId);
    return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
  }
  
  /**
   * Get all audit logs with pagination (admin only).
   * 
   * @param pageable pagination parameters
   * @return page of audit logs
   * @throws AccessDeniedException if user doesn't have admin role
   */
  @Transactional(readOnly = true)
  public Page<AuditLog> getAuditLogs(Pageable pageable) {
    validateAdminAccess();
    return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
  }
  
  /**
   * Get failed login attempts for a user (admin only).
   * 
   * @param username username to query
   * @return list of failed login attempts
   * @throws AccessDeniedException if user doesn't have admin role
   */
  @Transactional(readOnly = true)
  public List<AuditLog> getFailedLoginAttempts(String username) {
    validateAdminAccess();
    return auditLogRepository.findFailedLoginAttempts(username);
  }
  
  /**
   * Get failed login attempts for a user with pagination (admin only).
   * 
   * @param username username to query
   * @param pageable pagination parameters
   * @return page of failed login attempts
   * @throws AccessDeniedException if user doesn't have admin role
   */
  @Transactional(readOnly = true)
  public Page<AuditLog> getFailedLoginAttempts(String username, Pageable pageable) {
    validateAdminAccess();
    return auditLogRepository.findFailedLoginAttempts(username, pageable);
  }
  
  /**
   * Get security violations (admin only).
   * 
   * @return list of security violations
   * @throws AccessDeniedException if user doesn't have admin role
   */
  @Transactional(readOnly = true)
  public List<AuditLog> getSecurityViolations() {
    validateAdminAccess();
    return auditLogRepository.findSecurityViolations();
  }
  
  /**
   * Get security violations with pagination (admin only).
   * 
   * @param pageable pagination parameters
   * @return page of security violations
   * @throws AccessDeniedException if user doesn't have admin role
   */
  @Transactional(readOnly = true)
  public Page<AuditLog> getSecurityViolations(Pageable pageable) {
    validateAdminAccess();
    return auditLogRepository.findSecurityViolations(pageable);
  }
  
  /**
   * Get audit logs within date range (admin only).
   * 
   * @param start start date
   * @param end end date
   * @return list of audit logs
   * @throws AccessDeniedException if user doesn't have admin role
   */
  @Transactional(readOnly = true)
  public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
    validateAdminAccess();
    return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
  }
  
  /**
   * Get audit logs within date range with pagination (admin only).
   * 
   * @param start start date
   * @param end end date
   * @param pageable pagination parameters
   * @return page of audit logs
   * @throws AccessDeniedException if user doesn't have admin role
   */
  @Transactional(readOnly = true)
  public Page<AuditLog> getAuditLogsByDateRange(
      LocalDateTime start, LocalDateTime end, Pageable pageable) {
    validateAdminAccess();
    return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable);
  }
  
  /**
   * Validates that the current user has permission to access audit logs for the given user ID.
   * Users can only view their own logs unless they have admin role.
   * 
   * @param userId the user ID being accessed
   * @throws AccessDeniedException if user doesn't have permission
   */
  private void validateAuditLogAccess(Long userId) {
    Long currentUserId = authenticationHelper.getCurrentUserId();
    
    // Check if user is accessing their own logs
    if (currentUserId != null && currentUserId.equals(userId)) {
      return;
    }
    
    // Check if user has admin role
    if (authenticationHelper.hasRole("ROLE_ADMIN")) {
      return;
    }
    
    throw new AccessDeniedException(
        "You do not have permission to access audit logs for user: " + userId);
  }
  
  /**
   * Validates that the current user has admin role.
   * 
   * @throws AccessDeniedException if user doesn't have admin role
   */
  private void validateAdminAccess() {
    if (!authenticationHelper.hasRole("ROLE_ADMIN")) {
      throw new AccessDeniedException(
          "Admin role required to access system-wide audit logs");
    }
  }
  
  /**
   * Fallback logging to file when database logging fails.
   * 
   * @param eventType event type
   * @param description event description
   * @param userId user ID
   * @param username username
   * @param success success flag
   * @param error the error that caused fallback
   */
  private void logToFallbackFile(
      EventType eventType,
      String description,
      Long userId,
      String username,
      Boolean success,
      Exception error) {
    try {
      String fallbackLogPath = auditProperties.getFallbackLogPath();
      Path logPath = Paths.get(fallbackLogPath);
      
      // Create parent directories if they don't exist
      if (logPath.getParent() != null) {
        Files.createDirectories(logPath.getParent());
      }
      
      String logEntry = String.format(
          "%s | %s | UserId: %s | Username: %s | Success: %s | Description: %s | Error: %s%n",
          LocalDateTime.now().format(TIMESTAMP_FORMATTER),
          eventType,
          userId,
          username,
          success,
          description,
          error.getMessage()
      );
      
      Files.writeString(
          logPath,
          logEntry,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND
      );
      
      log.warn("Audit log written to fallback file: {}", fallbackLogPath);
      
    } catch (IOException ioException) {
      log.error("Failed to write to fallback audit log file: {}", 
          ioException.getMessage(), ioException);
    }
  }
  
  /**
   * Alerts on audit logging failures.
   * In production, this should integrate with monitoring/alerting systems.
   * 
   * @param eventType the event type that failed to log
   * @param error the error that occurred
   */
  private void alertAuditFailure(EventType eventType, Exception error) {
    // TODO: Integrate with alerting system (e.g., email, Slack, PagerDuty)
    log.error("CRITICAL: Audit logging failed for event type {}. Error: {}", 
        eventType, error.getMessage(), error);
    
    // For now, just log at ERROR level
    // In production, this should trigger alerts to security team
  }
}
