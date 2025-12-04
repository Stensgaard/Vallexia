package com.vallexia.audit.mapper;

import com.vallexia.audit.dto.AuditLogDto;
import com.vallexia.audit.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between AuditLog entity and DTO.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {
  
  /**
   * Convert AuditLog entity to AuditLogDto.
   * Note: The details field is intentionally excluded for security reasons.
   * 
   * @param auditLog AuditLog entity
   * @return AuditLogDto
   */
  @Mapping(target = "id", source = "id")
  @Mapping(target = "eventType", source = "eventType")
  @Mapping(target = "eventDescription", source = "eventDescription")
  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "username", source = "username")
  @Mapping(target = "ipAddress", source = "ipAddress")
  @Mapping(target = "userAgent", source = "userAgent")
  @Mapping(target = "requestMethod", source = "requestMethod")
  @Mapping(target = "requestUri", source = "requestUri")
  @Mapping(target = "responseStatus", source = "responseStatus")
  @Mapping(target = "success", source = "success")
  @Mapping(target = "timestamp", source = "timestamp")
  AuditLogDto toDto(AuditLog auditLog);
}
