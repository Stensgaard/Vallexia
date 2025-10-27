package com.vallexia.audit.controller;

import com.vallexia.audit.entity.AuditLog;
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

/**
 * REST controller for audit log operations.
 * Provides endpoints for querying audit logs with proper access control.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "Operations for querying audit logs and security events")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {
  
  private final AuditService auditService;
  private final AuthenticationHelper authenticationHelper;
  
  /**
   * Constructor for dependency injection.
   * 
   * @param auditService the audit service
   * @param authenticationHelper the authentication helper
   */
  public AuditLogController(AuditService auditService, AuthenticationHelper authenticationHelper) {
    this.auditService = auditService;
    this.authenticationHelper = authenticationHelper;
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
  public ResponseEntity<Page<AuditLog>> getMyAuditLogs(
      @Parameter(description = "Page number (0-based)", example = "0") 
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    // Get the current user's ID from SecurityContext
    Long currentUserId = authenticationHelper.getCurrentUserId();
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getUserAuditLogs(currentUserId, pageable);
    
    return ResponseEntity.ok(auditLogs);
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
  public ResponseEntity<Page<AuditLog>> getUserAuditLogs(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long userId,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getUserAuditLogs(userId, pageable);
    
    return ResponseEntity.ok(auditLogs);
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
  public ResponseEntity<Page<AuditLog>> getAllAuditLogs(
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getAuditLogs(pageable);
    
    return ResponseEntity.ok(auditLogs);
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
  public ResponseEntity<Page<AuditLog>> getFailedLoginAttempts(
      @Parameter(description = "Username to query", example = "john.doe", required = true)
      @RequestParam String username,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getFailedLoginAttempts(username, pageable);
    
    return ResponseEntity.ok(auditLogs);
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
  public ResponseEntity<Page<AuditLog>> getSecurityViolations(
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Number of records per page", example = "20")
      @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLog> auditLogs = auditService.getSecurityViolations(pageable);
    
    return ResponseEntity.ok(auditLogs);
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
  public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
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
    
    return ResponseEntity.ok(auditLogs);
  }
}
