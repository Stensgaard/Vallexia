package com.vallexia.audit.unit.mapper;

import com.vallexia.audit.dto.AuditLogDto;
import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.fixtures.AuditLogTestFixtures;
import com.vallexia.audit.mapper.AuditLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuditLogMapper.
 * Tests entity-to-DTO mapping with real MapStruct implementation.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootTest(classes = {
    com.vallexia.audit.mapper.AuditLogMapperImpl.class
})
@ActiveProfiles("test")
@DisplayName("AuditLogMapper Unit Tests")
class AuditLogMapperTest {
  
  @Autowired
  private AuditLogMapper auditLogMapper;
  
  // ==================== toDto() Tests ====================
  
  @Test
  @DisplayName("Should map all fields from entity to DTO")
  void shouldMapAllFieldsFromEntityToDto() {
    // Given
    AuditLog auditLog = AuditLogTestFixtures.createAuditLog();
    
    // When
    AuditLogDto dto = auditLogMapper.toDto(auditLog);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(auditLog.getId());
    assertThat(dto.getEventType()).isEqualTo(auditLog.getEventType());
    assertThat(dto.getEventDescription()).isEqualTo(auditLog.getEventDescription());
    assertThat(dto.getUserId()).isEqualTo(auditLog.getUserId());
    assertThat(dto.getUsername()).isEqualTo(auditLog.getUsername());
    assertThat(dto.getIpAddress()).isEqualTo(auditLog.getIpAddress());
    assertThat(dto.getUserAgent()).isEqualTo(auditLog.getUserAgent());
    assertThat(dto.getRequestMethod()).isEqualTo(auditLog.getRequestMethod());
    assertThat(dto.getRequestUri()).isEqualTo(auditLog.getRequestUri());
    assertThat(dto.getResponseStatus()).isEqualTo(auditLog.getResponseStatus());
    assertThat(dto.getSuccess()).isEqualTo(auditLog.getSuccess());
    assertThat(dto.getTimestamp()).isEqualTo(auditLog.getTimestamp());
  }
  
  @Test
  @DisplayName("Should exclude details field for security")
  void shouldExcludeDetailsFieldForSecurity() {
    // Given
    AuditLog auditLog = new AuditLog();
    auditLog.setId(1L);
    auditLog.setEventType(EventType.LOGIN_SUCCESS);
    auditLog.setEventDescription("Login successful");
    auditLog.setDetails("Sensitive details that should not be exposed");
    auditLog.setUserId(1L);
    auditLog.setUsername("testuser");
    auditLog.setSuccess(true);
    auditLog.setTimestamp(LocalDateTime.now());
    
    // When
    AuditLogDto dto = auditLogMapper.toDto(auditLog);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getEventDescription()).isEqualTo("Login successful");
    // The DTO should not have a details field (security requirement)
  }
  
  @Test
  @DisplayName("Should return null when entity is null")
  void shouldReturnNullWhenEntityIsNull() {
    // When
    AuditLogDto dto = auditLogMapper.toDto(null);
    
    // Then
    assertThat(dto).isNull();
  }
  
  @Test
  @DisplayName("Should map partial entity with only required fields")
  void shouldMapPartialEntityWithOnlyRequiredFields() {
    // Given
    AuditLog auditLog = new AuditLog();
    auditLog.setId(1L);
    auditLog.setEventType(EventType.API_ACCESS);
    auditLog.setEventDescription("API accessed");
    auditLog.setUserId(1L);
    // Leave other fields null
    
    // When
    AuditLogDto dto = auditLogMapper.toDto(auditLog);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getEventType()).isEqualTo(EventType.API_ACCESS);
    assertThat(dto.getEventDescription()).isEqualTo("API accessed");
    assertThat(dto.getUserId()).isEqualTo(1L);
    assertThat(dto.getUsername()).isNull();
    assertThat(dto.getIpAddress()).isNull();
    assertThat(dto.getUserAgent()).isNull();
  }
  
  // ==================== Event Type Mapping Tests ====================
  
  @Test
  @DisplayName("Should map all event types correctly")
  void shouldMapAllEventTypesCorrectly() {
    // Given & When & Then - test that all event types can be mapped correctly
    for (EventType eventType : EventType.values()) {
      AuditLog auditLog = AuditLogTestFixtures.createAuditLog(
          AuditLogTestFixtures.TEST_USER_ID,
          AuditLogTestFixtures.TEST_USERNAME,
          eventType
      );
      
      AuditLogDto dto = auditLogMapper.toDto(auditLog);
      
      assertThat(dto).isNotNull();
      assertThat(dto.getEventType()).isEqualTo(eventType);
    }
  }
  
  // ==================== Complete Entity Mapping Tests ====================
  
  @Test
  @DisplayName("Should map complete entity with all fields populated")
  void shouldMapCompleteEntityWithAllFieldsPopulated() {
    // Given
    AuditLog auditLog = new AuditLog();
    auditLog.setId(99L);
    auditLog.setEventType(EventType.SECURITY_VIOLATION);
    auditLog.setEventDescription("Suspicious activity");
    auditLog.setUserId(123L);
    auditLog.setUsername("hacker");
    auditLog.setIpAddress("203.0.113.100");
    auditLog.setUserAgent("BadBot/1.0");
    auditLog.setRequestMethod("POST");
    auditLog.setRequestUri("/api/v1/admin/delete-all");
    auditLog.setResponseStatus(403);
    auditLog.setSuccess(false);
    auditLog.setTimestamp(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    
    // When
    AuditLogDto dto = auditLogMapper.toDto(auditLog);
    
    // Then
    assertThat(dto.getId()).isEqualTo(99L);
    assertThat(dto.getEventType()).isEqualTo(EventType.SECURITY_VIOLATION);
    assertThat(dto.getEventDescription()).isEqualTo("Suspicious activity");
    assertThat(dto.getUserId()).isEqualTo(123L);
    assertThat(dto.getUsername()).isEqualTo("hacker");
    assertThat(dto.getIpAddress()).isEqualTo("203.0.113.100");
    assertThat(dto.getUserAgent()).isEqualTo("BadBot/1.0");
    assertThat(dto.getRequestMethod()).isEqualTo("POST");
    assertThat(dto.getRequestUri()).isEqualTo("/api/v1/admin/delete-all");
    assertThat(dto.getResponseStatus()).isEqualTo(403);
    assertThat(dto.getSuccess()).isFalse();
    assertThat(dto.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
  }
}
