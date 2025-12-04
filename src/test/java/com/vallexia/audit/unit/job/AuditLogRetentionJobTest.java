package com.vallexia.audit.unit.job;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.job.AuditLogRetentionJob;
import com.vallexia.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogRetentionJob.
 * Tests scheduled cleanup of old audit logs.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
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
  @DisplayName("Should delete old audit log records using efficient direct deletion")
  void shouldDeleteOldAuditLogRecords() {
    // Given
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
    long countToDelete = 5L;
    int deletedCount = 5;
    
    when(auditLogRepository.countByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(countToDelete);
    when(auditLogRepository.deleteByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(deletedCount);
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    
    verify(auditLogRepository).countByTimestampBefore(cutoffCaptor.capture());
    verify(auditLogRepository).deleteByTimestampBefore(cutoffCaptor.capture());
    
    // Verify the cutoff date is approximately correct (within 1 second)
    assertThat(cutoffCaptor.getAllValues().get(0)).isBetween(
        cutoffDate.minusSeconds(1), 
        cutoffDate.plusSeconds(1));
  }
  
  @Test
  @DisplayName("Should respect configured retention period")
  void shouldRespectConfiguredRetentionPeriod() {
    // Given
    long countToDelete = 3L;
    int deletedCount = 3;
    
    when(auditLogRepository.countByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(countToDelete);
    when(auditLogRepository.deleteByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(deletedCount);
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(auditLogRepository).countByTimestampBefore(cutoffCaptor.capture());
    
    LocalDateTime expectedCutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
    assertThat(cutoffCaptor.getValue()).isBetween(
        expectedCutoff.minusSeconds(1),
        expectedCutoff.plusSeconds(1));
  }
  
  @Test
  @DisplayName("Should not delete anything when no old records exist")
  void shouldNotDeleteAnythingWhenNoOldRecordsExist() {
    // Given
    when(auditLogRepository.countByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(0L);
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    verify(auditLogRepository).countByTimestampBefore(any(LocalDateTime.class));
    verify(auditLogRepository, never()).deleteByTimestampBefore(any(LocalDateTime.class));
  }
  
  @Test
  @DisplayName("Should not propagate exception when count query fails")
  void shouldNotPropagateExceptionWhenCountQueryFails() {
    // Given
    when(auditLogRepository.countByTimestampBefore(any(LocalDateTime.class)))
        .thenThrow(new RuntimeException("Database error"));
    
    // When - should not throw exception, should attempt batched deletion
    retentionJob.cleanupOldAuditLogs();
    
    // Then - verify the method was called
    verify(auditLogRepository).countByTimestampBefore(any(LocalDateTime.class));
  }
  
  @Test
  @DisplayName("Should not propagate exception when delete fails, should fallback to batched deletion")
  void shouldNotPropagateExceptionWhenDeleteFails() {
    // Given
    long countToDelete = 2L;
    
    when(auditLogRepository.countByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(countToDelete);
    when(auditLogRepository.deleteByTimestampBefore(any(LocalDateTime.class)))
        .thenThrow(new RuntimeException("Delete failed"));
    
    // Mock batched deletion fallback
    @SuppressWarnings("unchecked")
    Page<AuditLog> emptyPage = mock(Page.class);
    when(emptyPage.isEmpty()).thenReturn(true);
    when(emptyPage.hasNext()).thenReturn(false);
    when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class), any()))
        .thenReturn(emptyPage);
    
    // When - should not throw exception, should attempt batched deletion
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    verify(auditLogRepository).deleteByTimestampBefore(any(LocalDateTime.class));
    // Verify fallback was attempted
    verify(auditLogRepository, atLeastOnce()).findByTimestampBetweenOrderByTimestampDesc(
        any(LocalDateTime.class), any(LocalDateTime.class), any());
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
  @DisplayName("Should handle large number of records efficiently using direct deletion")
  void shouldHandleLargeNumberOfRecordsEfficiently() {
    // Given
    long countToDelete = 10000L;
    int deletedCount = 10000;
    
    when(auditLogRepository.countByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(countToDelete);
    when(auditLogRepository.deleteByTimestampBefore(any(LocalDateTime.class)))
        .thenReturn(deletedCount);
    
    // When
    retentionJob.cleanupOldAuditLogs();
    
    // Then
    verify(auditLogRepository).countByTimestampBefore(any(LocalDateTime.class));
    verify(auditLogRepository).deleteByTimestampBefore(any(LocalDateTime.class));
    verify(auditLogRepository, never()).deleteAll(any());
  }
  
  @Test
  @DisplayName("Should validate retention days in constructor")
  void shouldValidateRetentionDaysInConstructor() {
    // Given
    com.vallexia.config.audit.AuditProperties invalidProperties = 
        new com.vallexia.config.audit.AuditProperties();
    invalidProperties.setRetentionDays(0);
    
    // When/Then
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
        new AuditLogRetentionJob(auditLogRepository, invalidProperties))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("retention days must be at least 1");
  }
}
