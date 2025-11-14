package com.vallexia.audit.unit.controller;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.fixtures.AuditLogTestFixtures;
import com.vallexia.audit.service.AuditService;
import com.vallexia.security.AuthenticationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogController.
 * Tests REST endpoints for audit log operations with mocked dependencies.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogController Unit Tests")
class AuditLogControllerTest {
  
  @Mock
  private AuditService auditService;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @InjectMocks
  private com.vallexia.audit.controller.AuditLogController auditLogController;
  
  private Page<AuditLog> mockPage;
  
  @BeforeEach
  void setUp() {
    List<AuditLog> logs = AuditLogTestFixtures.createAuditLogList(5);
    Pageable pageable = PageRequest.of(0, 20);
    mockPage = new PageImpl<>(logs, pageable, 5);
  }
  
  // ==================== getMyAuditLogs() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve audit logs for current user with default pagination")
  void shouldRetrieveAuditLogsForCurrentUserWithDefaultPagination() {
    // Given
    Long currentUserId = AuditLogTestFixtures.TEST_USER_ID;
    when(authenticationHelper.getCurrentUserId()).thenReturn(currentUserId);
    when(auditService.getUserAuditLogs(currentUserId, PageRequest.of(0, 20)))
        .thenReturn(mockPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = auditLogController.getMyAuditLogs(0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(5);
    verify(auditService).getUserAuditLogs(currentUserId, PageRequest.of(0, 20));
    verify(authenticationHelper).getCurrentUserId();
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve audit logs for current user with custom pagination")
  void shouldRetrieveAuditLogsForCurrentUserWithCustomPagination() {
    // Given
    Long currentUserId = AuditLogTestFixtures.TEST_USER_ID;
    Pageable pageable = PageRequest.of(2, 10);
    List<AuditLog> logs = AuditLogTestFixtures.createAuditLogList(10);
    Page<AuditLog> customPage = new PageImpl<>(logs, pageable, 50);
    
    when(authenticationHelper.getCurrentUserId()).thenReturn(currentUserId);
    when(auditService.getUserAuditLogs(currentUserId, pageable))
        .thenReturn(customPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getMyAuditLogs(2, 10);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(10);
    assertThat(body.getTotalElements()).isEqualTo(50);
    verify(auditService).getUserAuditLogs(currentUserId, pageable);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty page when user has no audit logs")
  void shouldReturnEmptyPageWhenUserHasNoAuditLogs() {
    // Given
    Long currentUserId = AuditLogTestFixtures.TEST_USER_ID;
    Page<AuditLog> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    
    when(authenticationHelper.getCurrentUserId()).thenReturn(currentUserId);
    when(auditService.getUserAuditLogs(eq(currentUserId), any(Pageable.class)))
        .thenReturn(emptyPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = auditLogController.getMyAuditLogs(0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).isEmpty();
    assertThat(body.getTotalElements()).isEqualTo(0);
  }
  
  // ==================== getUserAuditLogs() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve audit logs for specific user as admin")
  void shouldRetrieveAuditLogsForSpecificUserAsAdmin() {
    // Given
    Long userId = AuditLogTestFixtures.TEST_USER_ID;
    when(auditService.getUserAuditLogs(eq(userId), any(Pageable.class)))
        .thenReturn(mockPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getUserAuditLogs(userId, 0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(5);
    verify(auditService).getUserAuditLogs(eq(userId), any(Pageable.class));
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve audit logs for specific user with pagination")
  void shouldRetrieveAuditLogsForSpecificUserWithPagination() {
    // Given
    Long userId = AuditLogTestFixtures.TEST_ADMIN_ID;
    Pageable pageable = PageRequest.of(1, 5);
    List<AuditLog> logs = AuditLogTestFixtures.createUserAuditLogs(userId, 5);
    Page<AuditLog> customPage = new PageImpl<>(logs, pageable, 15);
    
    when(auditService.getUserAuditLogs(userId, pageable))
        .thenReturn(customPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getUserAuditLogs(userId, 1, 5);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getSize()).isEqualTo(5);
    assertThat(body.getNumber()).isEqualTo(1);
    verify(auditService).getUserAuditLogs(userId, pageable);
  }
  
  // ==================== getAllAuditLogs() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve all audit logs with default pagination")
  void shouldRetrieveAllAuditLogsWithDefaultPagination() {
    // Given
    List<AuditLog> logs = AuditLogTestFixtures.createAuditLogList(20);
    Page<AuditLog> page = new PageImpl<>(logs, PageRequest.of(0, 20), 100);
    
    when(auditService.getAuditLogs(any(Pageable.class))).thenReturn(page);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getAllAuditLogs(0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(20);
    assertThat(body.getTotalElements()).isEqualTo(100);
    verify(auditService).getAuditLogs(any(Pageable.class));
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve all audit logs with custom pagination")
  void shouldRetrieveAllAuditLogsWithCustomPagination() {
    // Given
    Pageable pageable = PageRequest.of(3, 15);
    List<AuditLog> logs = AuditLogTestFixtures.createAuditLogList(15);
    Page<AuditLog> page = new PageImpl<>(logs, pageable, 100);
    
    when(auditService.getAuditLogs(pageable)).thenReturn(page);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getAllAuditLogs(3, 15);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getSize()).isEqualTo(15);
    assertThat(body.getNumber()).isEqualTo(3);
    verify(auditService).getAuditLogs(pageable);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty page when no audit logs exist")
  void shouldReturnEmptyPageWhenNoAuditLogsExist() {
    // Given
    Page<AuditLog> emptyPage = new PageImpl<>(
        List.of(), PageRequest.of(0, 20), 0);
    
    when(auditService.getAuditLogs(any(Pageable.class))).thenReturn(emptyPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getAllAuditLogs(0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).isEmpty();
    verify(auditService).getAuditLogs(any(Pageable.class));
  }
  
  // ==================== getFailedLoginAttempts() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve failed login attempts for username")
  void shouldRetrieveFailedLoginAttemptsForUsername() {
    // Given
    String username = AuditLogTestFixtures.TEST_USERNAME;
    AuditLog failedLogin = AuditLogTestFixtures.createFailedLoginLog(username);
    List<AuditLog> failedLogins = List.of(failedLogin);
    Page<AuditLog> page = new PageImpl<>(failedLogins, PageRequest.of(0, 20), 1);
    
    when(auditService.getFailedLoginAttempts(eq(username), any(Pageable.class)))
        .thenReturn(page);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getFailedLoginAttempts(username, 0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(1);
    assertThat(body.getContent().get(0).getEventType())
        .isEqualTo(EventType.LOGIN_FAILURE);
    assertThat(body.getContent().get(0).getUsername())
        .isEqualTo(username);
    verify(auditService).getFailedLoginAttempts(eq(username), any(Pageable.class));
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty page when no failed login attempts exist")
  void shouldReturnEmptyPageWhenNoFailedLoginAttemptsExist() {
    // Given
    String username = "nonexistent";
    Page<AuditLog> emptyPage = new PageImpl<>(
        List.of(), PageRequest.of(0, 20), 0);
    
    when(auditService.getFailedLoginAttempts(eq(username), any(Pageable.class)))
        .thenReturn(emptyPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getFailedLoginAttempts(username, 0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).isEmpty();
  }
  
  // ==================== getSecurityViolations() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve security violations")
  void shouldRetrieveSecurityViolations() {
    // Given
    AuditLog violation = AuditLogTestFixtures
        .createSecurityViolationLog(AuditLogTestFixtures.TEST_USERNAME);
    List<AuditLog> violations = List.of(violation);
    Page<AuditLog> page = new PageImpl<>(violations, PageRequest.of(0, 20), 1);
    
    when(auditService.getSecurityViolations(any(Pageable.class)))
        .thenReturn(page);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getSecurityViolations(0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(1);
    assertThat(body.getContent().get(0).getEventType())
        .isEqualTo(EventType.SECURITY_VIOLATION);
    verify(auditService).getSecurityViolations(any(Pageable.class));
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty page when no security violations exist")
  void shouldReturnEmptyPageWhenNoSecurityViolationsExist() {
    // Given
    Page<AuditLog> emptyPage = new PageImpl<>(
        List.of(), PageRequest.of(0, 20), 0);
    
    when(auditService.getSecurityViolations(any(Pageable.class)))
        .thenReturn(emptyPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getSecurityViolations(0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).isEmpty();
  }
  
  // ==================== getAuditLogsByDateRange() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve audit logs within date range")
  void shouldRetrieveAuditLogsWithinDateRange() {
    // Given
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now();
    List<AuditLog> logs = AuditLogTestFixtures
        .createAuditLogsInDateRange(start, end, 10);
    Page<AuditLog> page = new PageImpl<>(logs, PageRequest.of(0, 20), 10);
    
    when(auditService.getAuditLogsByDateRange(eq(start), eq(end), any(Pageable.class)))
        .thenReturn(page);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getAuditLogsByDateRange(start, end, 0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(10);
    verify(auditService).getAuditLogsByDateRange(eq(start), eq(end), any(Pageable.class));
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve audit logs with custom pagination in date range")
  void shouldRetrieveAuditLogsWithCustomPaginationInDateRange() {
    // Given
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
    Pageable pageable = PageRequest.of(0, 50);
    List<AuditLog> logs = AuditLogTestFixtures.createAuditLogList(50);
    Page<AuditLog> page = new PageImpl<>(logs, pageable, 100);
    
    when(auditService.getAuditLogsByDateRange(start, end, pageable))
        .thenReturn(page);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getAuditLogsByDateRange(start, end, 0, 50);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(50);
    assertThat(body.getTotalElements()).isEqualTo(100);
    verify(auditService).getAuditLogsByDateRange(start, end, pageable);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return empty page when no logs in date range")
  void shouldReturnEmptyPageWhenNoLogsInDateRange() {
    // Given
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now().minusDays(6);
    Page<AuditLog> emptyPage = new PageImpl<>(
        List.of(), PageRequest.of(0, 20), 0);
    
    when(auditService.getAuditLogsByDateRange(eq(start), eq(end), any(Pageable.class)))
        .thenReturn(emptyPage);
    
    // When
    ResponseEntity<Page<AuditLog>> response = 
        auditLogController.getAuditLogsByDateRange(start, end, 0, 20);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<AuditLog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).isEmpty();
  }
}
