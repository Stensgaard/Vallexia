package com.vallexia.audit.unit.job;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.fixtures.AuditLogTestFixtures;
import com.vallexia.audit.job.AuditLogRetentionJob;
import com.vallexia.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogRetentionJob.
 * Tests scheduled cleanup of old audit logs.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogRetentionJob Unit Tests")
class AuditLogRetentionJobTest {
  
  @Mock
  private AuditLogRepository auditLogRepository;
  
  private AuditLogRetentionJob retentionJob;
  
  private static final int RETENTION_DAYS = 90;
  
  private com.vallexia.config.audit.AuditProperties auditProperties;

  @BeforeEach
  void setUp() {
    auditProperties = new com.vallexia.config.audit.AuditProperties();
    auditProperties.setRetentionDays(RETENTION_DAYS);
    retentionJob = new AuditLogRetentionJob(auditLogRepository, auditProperties);
  }
  
  // ==================== cleanupOldAuditLogs() Tests ====================
  
  @Test
  @DisplayName("Should delete old audit log records")
  void shouldDeleteOldAuditLogRecords() {
    // Given
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
    List<AuditLog> oldLogs = AuditLogTestFixtures.createAuditLogList(5);
    
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(oldLogs);
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    
    verify(auditLogRepository).findByTimestampBetweenOrderByTimestampDesc(
        startCaptor.capture(), endCaptor.capture());
    verify(auditLogRepository).deleteAll(oldLogs);
    
    // Verify the cutoff date is approximately correct (within 1 second)
    assertThat(endCaptor.getValue()).isBetween(
        cutoffDate.minusSeconds(1), 
        cutoffDate.plusSeconds(1));
  }
  
  @Test
  @DisplayName("Should respect configured retention period")
  void shouldRespectConfiguredRetentionPeriod() {
    // Given
    List<AuditLog> oldLogs = AuditLogTestFixtures.createAuditLogList(3);
    
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(oldLogs);
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(auditLogRepository).findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), endCaptor.capture());
    
    LocalDateTime expectedCutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
    assertThat(endCaptor.getValue()).isBetween(
        expectedCutoff.minusSeconds(1),
        expectedCutoff.plusSeconds(1));
  }
  
  @Test
  @DisplayName("Should not delete anything when no old records exist")
  void shouldNotDeleteAnythingWhenNoOldRecordsExist() {
    // Given
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(Collections.emptyList());
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    verify(auditLogRepository).findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class));
    verify(auditLogRepository, never()).deleteAll(any());
  }
  
  @Test
  @DisplayName("Should not propagate exception when query fails")
  void shouldNotPropagateExceptionWhenQueryFails() {
    // Given
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenThrow(new RuntimeException("Database error"));
    
    // When - should not throw exception
    retentionJob.cleanupOldAuditLogs();
    
    // Then - verify the method was called
    verify(auditLogRepository).findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class));
  }
  
  @Test
  @DisplayName("Should not propagate exception when delete fails")
  void shouldNotPropagateExceptionWhenDeleteFails() {
    // Given
    List<AuditLog> oldLogs = AuditLogTestFixtures.createAuditLogList(2);
    
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(oldLogs);
    doThrow(new RuntimeException("Delete failed"))
        .when(auditLogRepository).deleteAll(any());
    
    // When - should not throw exception
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    verify(auditLogRepository).deleteAll(oldLogs);
  }
  
  // ==================== getRetentionDays() Tests ====================
  
  @Test
  @DisplayName("Should return configured retention days")
  void shouldReturnConfiguredRetentionDays() {
    // When
    int retentionDays = retentionJob.getRetentionDays();
    
    // Then
    assertThat(retentionDays).isEqualTo(RETENTION_DAYS);
  }
  
  // ==================== Constructor Tests ====================
  
  @Test
  @DisplayName("Should accept custom retention period in constructor")
  void shouldAcceptCustomRetentionPeriodInConstructor() {
    // Given
    int customRetentionDays = 30;
    com.vallexia.config.audit.AuditProperties customProperties = 
        new com.vallexia.config.audit.AuditProperties();
    customProperties.setRetentionDays(customRetentionDays);
    
    // When
    AuditLogRetentionJob customJob = new AuditLogRetentionJob(
        auditLogRepository, customProperties);
    
    // Then
    assertThat(customJob.getRetentionDays()).isEqualTo(customRetentionDays);
  }
  
  // ==================== Large Dataset Tests ====================
  
  @Test
  @DisplayName("Should handle large number of records efficiently")
  void shouldHandleLargeNumberOfRecordsEfficiently() {
    // Given
    List<AuditLog> oldLogs = AuditLogTestFixtures.createAuditLogList(100);
    
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(oldLogs);
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    verify(auditLogRepository).deleteAll(oldLogs);
    assertThat(oldLogs).hasSize(100);
  }
}
