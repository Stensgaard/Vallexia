package com.vallexia.audit.unit.service;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.EventType;
import com.vallexia.audit.fixtures.AuditLogTestFixtures;
import com.vallexia.audit.repository.AuditLogRepository;
import com.vallexia.audit.service.AuditService;
import com.vallexia.audit.util.IpAddressExtractor;
import com.vallexia.config.audit.AuditProperties;
import com.vallexia.security.AuthenticationHelper;
import com.vallexia.security.util.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService.
 * Tests business logic and access control with mocked dependencies.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuditService Unit Tests")
class AuditServiceTest {
  
  @Mock
  private AuditLogRepository auditLogRepository;
  
  @Mock
  private InputSanitizer inputSanitizer;
  
  @Mock
  private IpAddressExtractor ipAddressExtractor;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @Mock
  private AuditProperties auditProperties;
  
  @InjectMocks
  private AuditService auditService;
  
  private HttpServletRequest mockRequest;
  
  @BeforeEach
  void setUp() {
    mockRequest = AuditLogTestFixtures.createMockRequest();
    
    // Default sanitizer behavior - return same value
    when(inputSanitizer.sanitizeDescription(anyString())).thenAnswer(i -> i.getArgument(0));
    when(inputSanitizer.sanitizeUsername(anyString())).thenAnswer(i -> i.getArgument(0));
    when(inputSanitizer.sanitizeIpAddress(anyString())).thenAnswer(i -> i.getArgument(0));
    when(inputSanitizer.sanitizeUserAgent(anyString())).thenAnswer(i -> i.getArgument(0));
    when(inputSanitizer.sanitizeRequestUri(anyString())).thenAnswer(i -> i.getArgument(0));
    
    // Default IP extractor behavior
    when(ipAddressExtractor.extractClientIp(any(HttpServletRequest.class)))
        .thenReturn(AuditLogTestFixtures.TEST_IP_ADDRESS);
    
    // Default audit properties behavior
    when(auditProperties.getFallbackLogPath()).thenReturn("logs/audit-fallback.log");
  }
  
  // ==================== logAuthenticationEvent() Tests ====================
  
  @Test
  @DisplayName("Should log authentication event successfully")
  void shouldLogAuthenticationEventSuccessfully() {
    // Given
    when(auditLogRepository.save(any(AuditLog.class)))
        .thenAnswer(i -> i.getArgument(0));
    
    // When
    auditService.logAuthenticationEvent(
        EventType.LOGIN_SUCCESS,
        "User logged in",
        AuditLogTestFixtures.TEST_USER_ID,
        AuditLogTestFixtures.TEST_USERNAME,
        mockRequest,
        true
    );
    
    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    
    AuditLog savedLog = captor.getValue();
    assertThat(savedLog.getEventType()).isEqualTo(EventType.LOGIN_SUCCESS);
    assertThat(savedLog.getEventDescription()).isEqualTo("User logged in");
    assertThat(savedLog.getUserId()).isEqualTo(AuditLogTestFixtures.TEST_USER_ID);
    assertThat(savedLog.getUsername()).isEqualTo(AuditLogTestFixtures.TEST_USERNAME);
    assertThat(savedLog.getSuccess()).isTrue();
  }
  
  @Test
  @DisplayName("Should sanitize input when logging authentication event")
  void shouldSanitizeInputWhenLoggingAuthenticationEvent() {
    // Given
    String maliciousInput = "<script>alert('xss')</script>";
    String sanitizedInput = "alert xss";
    
    when(inputSanitizer.sanitizeDescription(maliciousInput)).thenReturn(sanitizedInput);
    when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    auditService.logAuthenticationEvent(
        EventType.LOGIN_SUCCESS,
        maliciousInput,
        AuditLogTestFixtures.TEST_USER_ID,
        AuditLogTestFixtures.TEST_USERNAME,
        mockRequest,
        true
    );
    
    // Then
    verify(inputSanitizer).sanitizeDescription(maliciousInput);
    verify(inputSanitizer).sanitizeUsername(AuditLogTestFixtures.TEST_USERNAME);
    verify(inputSanitizer).sanitizeIpAddress(anyString());
    verify(inputSanitizer).sanitizeUserAgent(anyString());
    verify(inputSanitizer).sanitizeRequestUri(anyString());
  }
  
  @Test
  @DisplayName("Should fall back to file logging when database fails")
  void shouldFallBackToFileLoggingWhenDatabaseFails() {
    // Given
    when(auditLogRepository.save(any(AuditLog.class)))
        .thenThrow(new RuntimeException("Database error"));
    
    // When - should not throw exception
    auditService.logAuthenticationEvent(
        EventType.LOGIN_SUCCESS,
        "User logged in",
        AuditLogTestFixtures.TEST_USER_ID,
        AuditLogTestFixtures.TEST_USERNAME,
        mockRequest,
        true
    );
    
    // Then - verify save was attempted
    verify(auditLogRepository).save(any(AuditLog.class));
  }
  
  // ==================== logProfileUpdateEvent() Tests ====================
  
  @Test
  @DisplayName("Should log profile update event successfully")
  void shouldLogProfileUpdateEventSuccessfully() {
    // Given
    when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    auditService.logProfileUpdateEvent(
        AuditLogTestFixtures.TEST_USER_ID,
        AuditLogTestFixtures.TEST_USERNAME,
        "Profile updated",
        mockRequest
    );
    
    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    
    AuditLog savedLog = captor.getValue();
    assertThat(savedLog.getEventType()).isEqualTo(EventType.PROFILE_UPDATE);
    assertThat(savedLog.getSuccess()).isTrue();
  }
  
  // ==================== logEvent() Tests ====================
  
  @Test
  @DisplayName("Should log event without HTTP request successfully")
  void shouldLogEventWithoutHttpRequestSuccessfully() {
    // Given
    when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    auditService.logEvent(
        EventType.PASSWORD_CHANGE,
        AuditLogTestFixtures.TEST_USER_ID,
        "Password changed"
    );
    
    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    
    AuditLog savedLog = captor.getValue();
    assertThat(savedLog.getEventType()).isEqualTo(EventType.PASSWORD_CHANGE);
    assertThat(savedLog.getUserId()).isEqualTo(AuditLogTestFixtures.TEST_USER_ID);
    assertThat(savedLog.getSuccess()).isTrue();
  }
  
  // ==================== logSecurityViolation() Tests ====================
  
  @Test
  @DisplayName("Should log security violation successfully")
  void shouldLogSecurityViolationSuccessfully() {
    // Given
    when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    auditService.logSecurityViolation(
        "Unauthorized access attempt",
        AuditLogTestFixtures.TEST_USERNAME,
        mockRequest
    );
    
    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    
    AuditLog savedLog = captor.getValue();
    assertThat(savedLog.getEventType()).isEqualTo(EventType.SECURITY_VIOLATION);
    assertThat(savedLog.getSuccess()).isFalse();
  }
  
  // ==================== logApiAccess() Tests ====================
  
  @Test
  @DisplayName("Should log API access with success status")
  void shouldLogApiAccessWithSuccessStatus() {
    // Given
    when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    auditService.logApiAccess(
        "API accessed",
        AuditLogTestFixtures.TEST_USER_ID,
        AuditLogTestFixtures.TEST_USERNAME,
        mockRequest,
        200
    );
    
    // Then
    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(auditLogRepository).save(captor.capture());
    
    AuditLog savedLog = captor.getValue();
    assertThat(savedLog.getEventType()).isEqualTo(EventType.API_ACCESS);
    assertThat(savedLog.getResponseStatus()).isEqualTo(200);
    assertThat(savedLog.getSuccess()).isTrue();
  }
  
  // ==================== getUserAuditLogs() Tests ====================
  
  @Test
  @DisplayName("Should retrieve audit logs for current user")
  void shouldRetrieveAuditLogsForCurrentUser() {
    // Given
    Long userId = AuditLogTestFixtures.TEST_USER_ID;
    List<AuditLog> expectedLogs = AuditLogTestFixtures.createUserAuditLogs(userId, 5);
    
    when(authenticationHelper.getCurrentUserId()).thenReturn(userId);
    when(auditLogRepository.findByUserIdOrderByTimestampDesc(userId))
        .thenReturn(expectedLogs);
    
    // When
    List<AuditLog> logs = auditService.getUserAuditLogs(userId);
    
    // Then
    assertThat(logs).hasSize(5);
    verify(auditLogRepository).findByUserIdOrderByTimestampDesc(userId);
  }
  
  @Test
  @DisplayName("Should throw AccessDeniedException when non-admin tries to access other user logs")
  void shouldThrowAccessDeniedExceptionWhenNonAdminTriesToAccessOtherUserLogs() {
    // Given
    Long currentUserId = AuditLogTestFixtures.TEST_USER_ID;
    Long targetUserId = AuditLogTestFixtures.TEST_ADMIN_ID;
    
    when(authenticationHelper.getCurrentUserId()).thenReturn(currentUserId);
    when(authenticationHelper.hasRole("ROLE_ADMIN")).thenReturn(false);
    
    // When & Then
    assertThatThrownBy(() -> auditService.getUserAuditLogs(targetUserId))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("permission to access audit logs");
  }
  
  @Test
  @DisplayName("Should allow admin to access any user audit logs")
  void shouldAllowAdminToAccessAnyUserAuditLogs() {
    // Given
    Long currentUserId = AuditLogTestFixtures.TEST_ADMIN_ID;
    Long targetUserId = AuditLogTestFixtures.TEST_USER_ID;
    List<AuditLog> expectedLogs = AuditLogTestFixtures.createUserAuditLogs(targetUserId, 3);
    
    when(authenticationHelper.getCurrentUserId()).thenReturn(currentUserId);
    when(authenticationHelper.hasRole("ROLE_ADMIN")).thenReturn(true);
    when(auditLogRepository.findByUserIdOrderByTimestampDesc(targetUserId))
        .thenReturn(expectedLogs);
    
    // When
    List<AuditLog> logs = auditService.getUserAuditLogs(targetUserId);
    
    // Then
    assertThat(logs).hasSize(3);
    verify(auditLogRepository).findByUserIdOrderByTimestampDesc(targetUserId);
  }
  
  @Test
  @DisplayName("Should retrieve user audit logs with pagination")
  void shouldRetrieveUserAuditLogsWithPagination() {
    // Given
    Long userId = AuditLogTestFixtures.TEST_USER_ID;
    Pageable pageable = PageRequest.of(0, 10);
    List<AuditLog> logs = AuditLogTestFixtures.createUserAuditLogs(userId, 10);
    Page<AuditLog> expectedPage = new PageImpl<>(logs, pageable, 10);
    
    when(authenticationHelper.getCurrentUserId()).thenReturn(userId);
    when(auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable))
        .thenReturn(expectedPage);
    
    // When
    Page<AuditLog> result = auditService.getUserAuditLogs(userId, pageable);
    
    // Then
    assertThat(result.getContent()).hasSize(10);
    assertThat(result.getTotalElements()).isEqualTo(10);
  }
  
  // ==================== getAuditLogs() Tests ====================
  
  @Test
  @DisplayName("Should retrieve all audit logs for admin")
  void shouldRetrieveAllAuditLogsForAdmin() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    List<AuditLog> logs = AuditLogTestFixtures.createAuditLogList(20);
    Page<AuditLog> expectedPage = new PageImpl<>(logs, pageable, 20);
    
    when(authenticationHelper.hasRole("ROLE_ADMIN")).thenReturn(true);
    when(auditLogRepository.findAllByOrderByTimestampDesc(pageable))
        .thenReturn(expectedPage);
    
    // When
    Page<AuditLog> result = auditService.getAuditLogs(pageable);
    
    // Then
    assertThat(result.getContent()).hasSize(20);
    verify(auditLogRepository).findAllByOrderByTimestampDesc(pageable);
  }
  
  @Test
  @DisplayName("Should throw AccessDeniedException when non-admin tries to get all logs")
  void shouldThrowAccessDeniedExceptionWhenNonAdminTriesToGetAllLogs() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    when(authenticationHelper.hasRole("ROLE_ADMIN")).thenReturn(false);
    
    // When & Then
    assertThatThrownBy(() -> auditService.getAuditLogs(pageable))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("Admin role required");
  }
  
  // ==================== getFailedLoginAttempts() Tests ====================
  
  @Test
  @DisplayName("Should retrieve failed login attempts for admin")
  void shouldRetrieveFailedLoginAttemptsForAdmin() {
    // Given
    String username = AuditLogTestFixtures.TEST_USERNAME;
    Pageable pageable = PageRequest.of(0, 10);
    List<AuditLog> failedLogins = List.of(
        AuditLogTestFixtures.createFailedLoginLog(username)
    );
    Page<AuditLog> expectedPage = new PageImpl<>(failedLogins, pageable, 1);
    
    when(authenticationHelper.hasRole("ROLE_ADMIN")).thenReturn(true);
    when(auditLogRepository.findFailedLoginAttempts(username, pageable))
        .thenReturn(expectedPage);
    
    // When
    Page<AuditLog> result = auditService.getFailedLoginAttempts(username, pageable);
    
    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getEventType())
        .isEqualTo(EventType.LOGIN_FAILURE);
  }
  
  // ==================== getSecurityViolations() Tests ====================
  
  @Test
  @DisplayName("Should retrieve security violations for admin")
  void shouldRetrieveSecurityViolationsForAdmin() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    List<AuditLog> violations = List.of(
        AuditLogTestFixtures.createSecurityViolationLog(AuditLogTestFixtures.TEST_USERNAME)
    );
    Page<AuditLog> expectedPage = new PageImpl<>(violations, pageable, 1);
    
    when(authenticationHelper.hasRole("ROLE_ADMIN")).thenReturn(true);
    when(auditLogRepository.findSecurityViolations(pageable))
        .thenReturn(expectedPage);
    
    // When
    Page<AuditLog> result = auditService.getSecurityViolations(pageable);
    
    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getEventType())
        .isEqualTo(EventType.SECURITY_VIOLATION);
  }
  
  // ==================== getAuditLogsByDateRange() Tests ====================
  
  @Test
  @DisplayName("Should retrieve audit logs by date range for admin")
  void shouldRetrieveAuditLogsByDateRangeForAdmin() {
    // Given
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now();
    Pageable pageable = PageRequest.of(0, 10);
    List<AuditLog> logs = AuditLogTestFixtures.createAuditLogsInDateRange(start, end, 5);
    Page<AuditLog> expectedPage = new PageImpl<>(logs, pageable, 5);
    
    when(authenticationHelper.hasRole("ROLE_ADMIN")).thenReturn(true);
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable))
        .thenReturn(expectedPage);
    
    // When
    Page<AuditLog> result = auditService.getAuditLogsByDateRange(start, end, pageable);
    
    // Then
    assertThat(result.getContent()).hasSize(5);
    assertThat(result.getContent().get(0).getTimestamp()).isAfterOrEqualTo(start);
    assertThat(result.getContent().get(0).getTimestamp()).isBeforeOrEqualTo(end);
  }
}
