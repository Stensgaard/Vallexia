package com.vallexia.audit.controller;

import com.vallexia.audit.dto.AuditLogDto;
import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.mapper.AuditLogMapper;
import com.vallexia.audit.service.AuditService;
import com.vallexia.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// BUG: getting own audit logs contain many null values
/* an example of a profile update audit log
      "id": 149,
      "eventType": "PROFILE_UPDATE",
      "eventDescription": "User settings updated for user ID: 45",
      "userId": 45,
      "username": null,
      "ipAddress": null,
      "userAgent": null,
      "requestMethod": null,
      "requestUri": null,
      "responseStatus": null,
      "success": true,
      "timestamp": "2025-12-12T08:49:26.917771"
    },
*/
// BUG: failed login dont trigger a audit log 
// {{baseUrl}}/api/{{apiVersion}}/audit-logs/failed-logins?username=
// {{registeredUsername}}&page=0&size=20 reutrns none
// TODO trigger a security violation audit log for testing api in the setup folder in audit
// TODO trigger audit log within the date range in the setup folder in audit for testing

// TODO make api tests for all audit log endpoints

// TODO do users need to be able to view their own audit logs?

/**
 * REST controller for audit log operations.
 * Provides endpoints for querying audit logs with proper access control.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "Operations for querying audit logs and security events")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {
  
  private final AuditService auditService;
  private final AuthenticationHelper authenticationHelper;
  private final AuditLogMapper auditLogMapper;
  
  /**
   * Constructor for dependency injection.
   * 
   * @param auditService the audit service
   * @param authenticationHelper the authentication helper
   * @param auditLogMapper the audit log mapper
   */
  public AuditLogController(AuditService auditService, AuthenticationHelper authenticationHelper, 
                            AuditLogMapper auditLogMapper) {
    this.auditService = auditService;
    this.authenticationHelper = authenticationHelper;
    this.auditLogMapper = auditLogMapper;
  }
  
  /**
   * Get audit logs for the current user.
   * Users can only view their own audit logs.
   * 
   * @param page page number (0-based)
   * @param size page size
   * @return page of audit logs
   */
  @Operation(
      summary = "Get my audit logs",
      description = "Retrieve paginated audit logs for the currently authenticated user"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/me")
  public ResponseEntity<Page<AuditLogDto>> getMyAuditLogs(
      @Parameter(description = "Page number (0-based)", example = "0") 
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    // Get the current user's ID from SecurityContext
    Long currentUserId = authenticationHelper.getCurrentUserId();
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getUserAuditLogs(currentUserId, pageable);
    Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);
    
    return ResponseEntity.ok(auditLogDtos);
  }
  
  /**
   * Get audit logs for a specific user (admin only).
   * 
   * @param userId user ID
   * @param page page number (0-based)
   * @param size page size
   * @return page of audit logs
   */
  @Operation(
      summary = "Get audit logs for specific user (Admin only)",
      description = "Retrieve paginated audit logs for a specific user. Requires ADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
      @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AuditLogDto>> getUserAuditLogs(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long userId,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getUserAuditLogs(userId, pageable);
    Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);
    
    return ResponseEntity.ok(auditLogDtos);
  }
  
  /**
   * Get all audit logs (admin only).
   * 
   * @param page page number (0-based)
   * @param size page size
   * @return page of audit logs
   */
  @Operation(
      summary = "Get all audit logs (Admin only)",
      description = "Retrieve all audit logs from the system with pagination. Requires ADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
      @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AuditLogDto>> getAllAuditLogs(
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getAuditLogs(pageable);
    Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);
    
    return ResponseEntity.ok(auditLogDtos);
  }
  
  /**
   * Get failed login attempts for a user (admin only).
   * 
   * @param username username to query
   * @param page page number (0-based)
   * @param size page size
   * @return page of failed login attempts
   */
  @Operation(
      summary = "Get failed login attempts (Admin only)",
      description = "Retrieve failed login attempts for a specific username. Requires ADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved failed login attempts"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
      @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/failed-logins")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AuditLogDto>> getFailedLoginAttempts(
      @Parameter(description = "Username to query", example = "john.doe", required = true)
      @RequestParam String username,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getFailedLoginAttempts(username, pageable);
    Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);
    
    return ResponseEntity.ok(auditLogDtos);
  }
  
  /**
   * Get security violations (admin only).
   * 
   * @param page page number (0-based)
   * @param size page size
   * @return page of security violations
   */
  @Operation(
      summary = "Get security violations (Admin only)",
      description = "Retrieve all security violation events. Requires ADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved security violations"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
      @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/security-violations")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AuditLogDto>> getSecurityViolations(
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getSecurityViolations(pageable);
    Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);
    
    return ResponseEntity.ok(auditLogDtos);
  }
  
  /**
   * Get audit logs within a date range (admin only).
   * 
   * @param start start date and time
   * @param end end date and time
   * @param page page number (0-based)
   * @param size page size
   * @return page of audit logs
   */
  @Operation(
      summary = "Get audit logs by date range (Admin only)",
      description = "Retrieve audit logs within a specific date and time range. Requires ADMIN role."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs"),
      @ApiResponse(responseCode = "400", description = "Bad request - invalid date format"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
      @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/date-range")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AuditLogDto>> getAuditLogsByDateRange(
      @Parameter(description = "Start date and time in ISO 8601 format", example = "2024-01-01T00:00:00", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
      @Parameter(description = "End date and time in ISO 8601 format", example = "2024-01-31T23:59:59", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(start, end, pageable);
    Page<AuditLogDto> auditLogDtos = auditLogs.map(auditLogMapper::toDto);
    
    return ResponseEntity.ok(auditLogDtos);
  }
}
