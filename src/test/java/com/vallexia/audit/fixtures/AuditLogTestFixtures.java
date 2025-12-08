package com.vallexia.audit.fixtures;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.enums.EventType;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mockito;

import java.lang.reflect.Field;
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
    AuditLog.UserContext userContext = new AuditLog.UserContext(userId, username);
    AuditLog.HttpRequestContext httpContext = new AuditLog.HttpRequestContext(
        TEST_IP_ADDRESS,
        TEST_USER_AGENT,
        TEST_REQUEST_METHOD,
        TEST_REQUEST_URI,
        200
    );
    AuditLog log = new AuditLog(
        eventType,
        "Test event: " + eventType,
        userContext,
        httpContext,
        true
    );
    setIdAndTimestamp(log, 1L, LocalDateTime.now());
    return log;
  }
  
  /**
   * Creates an audit log for failed login attempt.
   */
  public static AuditLog createFailedLoginLog(String username) {
    AuditLog.UserContext userContext = new AuditLog.UserContext(null, username);
    AuditLog.HttpRequestContext httpContext = new AuditLog.HttpRequestContext(
        TEST_IP_ADDRESS,
        TEST_USER_AGENT,
        "POST",
        "/api/v1/auth/login",
        401
    );
    AuditLog log = new AuditLog(
        EventType.LOGIN_FAILURE,
        "Failed login attempt",
        userContext,
        httpContext,
        false
    );
    setIdAndTimestamp(log, 2L, LocalDateTime.now());
    return log;
  }
  
  /**
   * Creates an audit log for security violation.
   */
  public static AuditLog createSecurityViolationLog(String username) {
    AuditLog.UserContext userContext = new AuditLog.UserContext(null, username);
    AuditLog.HttpRequestContext httpContext = new AuditLog.HttpRequestContext(
        TEST_IP_ADDRESS,
        TEST_USER_AGENT,
        "POST",
        "/api/v1/admin/users",
        403
    );
    AuditLog log = new AuditLog(
        EventType.SECURITY_VIOLATION,
        "Suspicious activity detected",
        userContext,
        httpContext,
        false
    );
    setIdAndTimestamp(log, 3L, LocalDateTime.now());
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
      setIdAndTimestamp(log, (long) i, LocalDateTime.now().minusHours(i));
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
      setIdAndTimestamp(log, (long) i, LocalDateTime.now().minusHours(i));
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
      setIdAndTimestamp(log, (long) i, start.plusMinutes(minutesIncrement * i));
      logs.add(log);
    }
    
    return logs;
  }
  
  /**
   * Creates an audit log with details field (for testing security exclusion).
   */
  public static AuditLog createAuditLogWithDetails(String details) {
    AuditLog.UserContext userContext = new AuditLog.UserContext(1L, TEST_USERNAME);
    AuditLog.HttpRequestContext httpContext = new AuditLog.HttpRequestContext(
        null,
        null,
        null,
        null,
        null
    );
    AuditLog log = new AuditLog(
        EventType.LOGIN_SUCCESS,
        "Login successful",
        userContext,
        httpContext,
        details,
        true
    );
    setIdAndTimestamp(log, 1L, LocalDateTime.now());
    return log;
  }
  
  /**
   * Creates a minimal audit log with only required fields (no HTTP context).
   */
  public static AuditLog createMinimalAuditLog(EventType eventType, String description, Long userId) {
    AuditLog log = new AuditLog(
        eventType,
        description,
        userId,
        true
    );
    setIdAndTimestamp(log, 1L, null);
    return log;
  }
  
  /**
   * Creates a complete audit log with all fields populated.
   */
  public static AuditLog createCompleteAuditLog(
      Long id,
      EventType eventType,
      String description,
      Long userId,
      String username,
      String ipAddress,
      String userAgent,
      String requestMethod,
      String requestUri,
      Integer responseStatus,
      Boolean success,
      LocalDateTime timestamp) {
    AuditLog.UserContext userContext = new AuditLog.UserContext(userId, username);
    AuditLog.HttpRequestContext httpContext = new AuditLog.HttpRequestContext(
        ipAddress,
        userAgent,
        requestMethod,
        requestUri,
        responseStatus
    );
    AuditLog log = new AuditLog(
        eventType,
        description,
        userContext,
        httpContext,
        success
    );
    setIdAndTimestamp(log, id, timestamp);
    return log;
  }
  
  /**
   * Helper method to set id and timestamp using reflection.
   * These fields are not final and are managed by JPA, so we can set them in tests.
   * Made public so test classes can use it.
   */
  public static void setIdAndTimestamp(AuditLog log, Long id, LocalDateTime timestamp) {
    try {
      if (id != null) {
        Field idField = AuditLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(log, id);
      }
      
      if (timestamp != null) {
        Field timestampField = AuditLog.class.getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.set(log, timestamp);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to set id or timestamp via reflection", e);
    }
  }
}
