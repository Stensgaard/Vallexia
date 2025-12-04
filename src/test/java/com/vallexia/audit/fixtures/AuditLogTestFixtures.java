package com.vallexia.audit.fixtures;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.enums.EventType;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Test fixtures for audit log testing.
 * Provides reusable test data and mock objects.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
public class AuditLogTestFixtures {
  
  public static final Long TEST_USER_ID = 1L;
  public static final Long TEST_ADMIN_ID = 100L;
  public static final String TEST_USERNAME = "testuser";
  public static final String TEST_IP_ADDRESS = "192.168.1.100";
  public static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
  public static final String TEST_REQUEST_URI = "/api/v1/users/profile";
  public static final String TEST_REQUEST_METHOD = "GET";
  
  /**
   * Creates a sample audit log with default values.
   */
  public static AuditLog createAuditLog() {
    return createAuditLog(TEST_USER_ID, TEST_USERNAME, EventType.LOGIN_SUCCESS);
  }
  
  /**
   * Creates a sample audit log with specified user and event type.
   */
  public static AuditLog createAuditLog(Long userId, String username, EventType eventType) {
    AuditLog log = new AuditLog();
    log.setId(1L);
    log.setEventType(eventType);
    log.setEventDescription("Test event: " + eventType);
    log.setUserId(userId);
    log.setUsername(username);
    log.setIpAddress(TEST_IP_ADDRESS);
    log.setUserAgent(TEST_USER_AGENT);
    log.setRequestMethod(TEST_REQUEST_METHOD);
    log.setRequestUri(TEST_REQUEST_URI);
    log.setResponseStatus(200);
    log.setSuccess(true);
    log.setTimestamp(LocalDateTime.now());
    return log;
  }
  
  /**
   * Creates an audit log for failed login attempt.
   */
  public static AuditLog createFailedLoginLog(String username) {
    AuditLog log = new AuditLog();
    log.setId(2L);
    log.setEventType(EventType.LOGIN_FAILURE);
    log.setEventDescription("Failed login attempt");
    log.setUsername(username);
    log.setIpAddress(TEST_IP_ADDRESS);
    log.setUserAgent(TEST_USER_AGENT);
    log.setRequestMethod("POST");
    log.setRequestUri("/api/v1/auth/login");
    log.setResponseStatus(401);
    log.setSuccess(false);
    log.setTimestamp(LocalDateTime.now());
    return log;
  }
  
  /**
   * Creates an audit log for security violation.
   */
  public static AuditLog createSecurityViolationLog(String username) {
    AuditLog log = new AuditLog();
    log.setId(3L);
    log.setEventType(EventType.SECURITY_VIOLATION);
    log.setEventDescription("Suspicious activity detected");
    log.setUsername(username);
    log.setIpAddress(TEST_IP_ADDRESS);
    log.setUserAgent(TEST_USER_AGENT);
    log.setRequestMethod("POST");
    log.setRequestUri("/api/v1/admin/users");
    log.setResponseStatus(403);
    log.setSuccess(false);
    log.setTimestamp(LocalDateTime.now());
    return log;
  }
  
  /**
   * Creates a list of audit logs with different event types.
   */
  public static List<AuditLog> createAuditLogList(int count) {
    List<AuditLog> logs = new ArrayList<>();
    EventType[] eventTypes = EventType.values();
    
    for (int i = 0; i < count; i++) {
      EventType eventType = eventTypes[i % eventTypes.length];
      AuditLog log = createAuditLog((long) i, "user" + i, eventType);
      log.setId((long) i);
      log.setTimestamp(LocalDateTime.now().minusHours(i));
      logs.add(log);
    }
    
    return logs;
  }
  
  /**
   * Creates a list of audit logs for a specific user.
   */
  public static List<AuditLog> createUserAuditLogs(Long userId, int count) {
    List<AuditLog> logs = new ArrayList<>();
    EventType[] eventTypes = EventType.values();
    
    for (int i = 0; i < count; i++) {
      EventType eventType = eventTypes[i % eventTypes.length];
      AuditLog log = createAuditLog(userId, "user" + userId, eventType);
      log.setId((long) i);
      log.setTimestamp(LocalDateTime.now().minusHours(i));
      logs.add(log);
    }
    
    return logs;
  }
  
  /**
   * Creates a mock HttpServletRequest with default values.
   */
  public static HttpServletRequest createMockRequest() {
    return createMockRequest(TEST_IP_ADDRESS, TEST_USER_AGENT, TEST_REQUEST_URI, TEST_REQUEST_METHOD);
  }
  
  /**
   * Creates a mock HttpServletRequest with specified values.
   */
  public static HttpServletRequest createMockRequest(
      String remoteAddr, String userAgent, String requestUri, String method) {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRemoteAddr()).thenReturn(remoteAddr);
    when(request.getHeader("User-Agent")).thenReturn(userAgent);
    when(request.getRequestURI()).thenReturn(requestUri);
    when(request.getMethod()).thenReturn(method);
    return request;
  }
  
  /**
   * Creates a mock HttpServletRequest with proxy headers.
   */
  public static HttpServletRequest createMockRequestWithProxy(
      String remoteAddr, String forwardedFor, String realIp) {
    HttpServletRequest request = createMockRequest();
    when(request.getRemoteAddr()).thenReturn(remoteAddr);
    if (forwardedFor != null) {
      when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedFor);
    }
    if (realIp != null) {
      when(request.getHeader("X-Real-IP")).thenReturn(realIp);
    }
    return request;
  }
  
  /**
   * Creates audit logs with timestamps in a specific date range.
   */
  public static List<AuditLog> createAuditLogsInDateRange(
      LocalDateTime start, LocalDateTime end, int count) {
    List<AuditLog> logs = new ArrayList<>();
    long totalMinutes = java.time.Duration.between(start, end).toMinutes();
    long minutesIncrement = totalMinutes / (count > 1 ? count - 1 : 1);
    
    for (int i = 0; i < count; i++) {
      AuditLog log = createAuditLog();
      log.setId((long) i);
      log.setTimestamp(start.plusMinutes(minutesIncrement * i));
      logs.add(log);
    }
    
    return logs;
  }
}
