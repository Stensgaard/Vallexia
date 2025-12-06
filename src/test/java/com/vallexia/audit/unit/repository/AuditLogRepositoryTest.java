package com.vallexia.audit.unit.repository;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.fixtures.AuditLogTestFixtures;
import com.vallexia.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogRepository.
 * Tests repository query methods with mocked implementations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogRepository Unit Tests")
class AuditLogRepositoryTest {
  
  @Mock
  private AuditLogRepository auditLogRepository;
  
  private List<AuditLog> mockLogs;
  private Page<AuditLog> mockPage;
  
  @BeforeEach
  void setUp() {
    mockLogs = AuditLogTestFixtures.createAuditLogList(10);
    Pageable pageable = PageRequest.of(0, 10);
    mockPage = new PageImpl<>(mockLogs, pageable, 100);
  }
  
  // ==================== findByUserIdOrderByTimestampDesc() Tests ====================
  
  @Test
  @DisplayName("Should find audit logs by user ID without pagination")
  void shouldFindAuditLogsByUserIdWithoutPagination() {
    // Given
    Long userId = AuditLogTestFixtures.TEST_USER_ID;
    List<AuditLog> userLogs = AuditLogTestFixtures.createUserAuditLogs(userId, 5);
    
    when(auditLogRepository.findByUserIdOrderByTimestampDesc(userId))
        .thenReturn(userLogs);
    
    // When
    List<AuditLog> result = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(5);
    verify(auditLogRepository).findByUserIdOrderByTimestampDesc(userId);
  }
  
  @Test
  @DisplayName("Should find audit logs by user ID with pagination")
  void shouldFindAuditLogsByUserIdWithPagination() {
    // Given
    Long userId = AuditLogTestFixtures.TEST_USER_ID;
    Pageable pageable = PageRequest.of(0, 10);
    
    when(auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable))
        .thenReturn(mockPage);
    
    // When
    Page<AuditLog> result = auditLogRepository
        .findByUserIdOrderByTimestampDesc(userId, pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(10);
    verify(auditLogRepository).findByUserIdOrderByTimestampDesc(userId, pageable);
  }
  
  // ==================== findByTimestampBetweenOrderByTimestampDesc() Tests ====================
  
  @Test
  @DisplayName("Should find audit logs within date range without pagination")
  void shouldFindAuditLogsWithinDateRangeWithoutPagination() {
    // Given
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now();
    List<AuditLog> logs = AuditLogTestFixtures
        .createAuditLogsInDateRange(start, end, 5);
    
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end))
        .thenReturn(logs);
    
    // When
    List<AuditLog> result = auditLogRepository
        .findByTimestampBetweenOrderByTimestampDesc(start, end);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(5);
    verify(auditLogRepository).findByTimestampBetweenOrderByTimestampDesc(start, end);
  }
  
  @Test
  @DisplayName("Should find audit logs within date range with pagination")
  void shouldFindAuditLogsWithinDateRangeWithPagination() {
    // Given
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
    Pageable pageable = PageRequest.of(0, 10);
    
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        start, end, pageable)).thenReturn(mockPage);
    
    // When
    Page<AuditLog> result = auditLogRepository
        .findByTimestampBetweenOrderByTimestampDesc(start, end, pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(10);
    verify(auditLogRepository).findByTimestampBetweenOrderByTimestampDesc(
        start, end, pageable);
  }
  
  // ==================== findFailedLoginAttempts() Tests ====================
  
  @Test
  @DisplayName("Should find failed login attempts without pagination")
  void shouldFindFailedLoginAttemptsWithoutPagination() {
    // Given
    String username = AuditLogTestFixtures.TEST_USERNAME;
    List<AuditLog> failedLogins = List.of(
        AuditLogTestFixtures.createFailedLoginLog(username)
    );
    
    when(auditLogRepository.findFailedLoginAttempts(username))
        .thenReturn(failedLogins);
    
    // When
    List<AuditLog> result = auditLogRepository.findFailedLoginAttempts(username);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getEventType()).isEqualTo(EventType.LOGIN_FAILURE);
    verify(auditLogRepository).findFailedLoginAttempts(username);
  }
  
  @Test
  @DisplayName("Should find failed login attempts with pagination")
  void shouldFindFailedLoginAttemptsWithPagination() {
    // Given
    String username = AuditLogTestFixtures.TEST_USERNAME;
    Pageable pageable = PageRequest.of(0, 10);
    List<AuditLog> failedLogins = List.of(
        AuditLogTestFixtures.createFailedLoginLog(username)
    );
    Page<AuditLog> page = new PageImpl<>(failedLogins, pageable, 1);
    
    when(auditLogRepository.findFailedLoginAttempts(username, pageable))
        .thenReturn(page);
    
    // When
    Page<AuditLog> result = auditLogRepository
        .findFailedLoginAttempts(username, pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getEventType())
        .isEqualTo(EventType.LOGIN_FAILURE);
    verify(auditLogRepository).findFailedLoginAttempts(username, pageable);
  }
  
  // ==================== findSecurityViolations() Tests ====================
  
  @Test
  @DisplayName("Should find security violations without pagination")
  void shouldFindSecurityViolationsWithoutPagination() {
    // Given
    List<AuditLog> violations = List.of(
        AuditLogTestFixtures.createSecurityViolationLog(
            AuditLogTestFixtures.TEST_USERNAME
        )
    );
    
    when(auditLogRepository.findSecurityViolations()).thenReturn(violations);
    
    // When
    List<AuditLog> result = auditLogRepository.findSecurityViolations();
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getEventType()).isEqualTo(EventType.SECURITY_VIOLATION);
    verify(auditLogRepository).findSecurityViolations();
  }
  
  @Test
  @DisplayName("Should find security violations with pagination")
  void shouldFindSecurityViolationsWithPagination() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    List<AuditLog> violations = List.of(
        AuditLogTestFixtures.createSecurityViolationLog(
            AuditLogTestFixtures.TEST_USERNAME
        )
    );
    Page<AuditLog> page = new PageImpl<>(violations, pageable, 1);
    
    when(auditLogRepository.findSecurityViolations(pageable))
        .thenReturn(page);
    
    // When
    Page<AuditLog> result = auditLogRepository.findSecurityViolations(pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getEventType())
        .isEqualTo(EventType.SECURITY_VIOLATION);
    verify(auditLogRepository).findSecurityViolations(pageable);
  }
  
  // ==================== findAllByOrderByTimestampDesc() Tests ====================
  
  @Test
  @DisplayName("Should find all audit logs ordered by timestamp")
  void shouldFindAllAuditLogsOrderedByTimestamp() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    
    when(auditLogRepository.findAllByOrderByTimestampDesc(pageable))
        .thenReturn(mockPage);
    
    // When
    Page<AuditLog> result = auditLogRepository
        .findAllByOrderByTimestampDesc(pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(10);
    verify(auditLogRepository).findAllByOrderByTimestampDesc(pageable);
  }
}
