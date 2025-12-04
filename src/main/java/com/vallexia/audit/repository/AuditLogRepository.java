package com.vallexia.audit.repository;

import com.vallexia.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit log operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Query constants to avoid duplication.
     * These are used in @Query annotations below.
     */
    interface Queries {
        String FAILED_LOGIN = "SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.LOGIN_FAILURE AND a.username = :username ORDER BY a.timestamp DESC";
        String SECURITY_VIOLATION = "SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.SECURITY_VIOLATION ORDER BY a.timestamp DESC";
    }
    
    /**
     * Find audit logs by user ID.
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    /**
     * Find audit logs by user ID with pagination.
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    /**
     * Find audit logs within date range.
     */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find audit logs within date range with pagination.
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    /**
     * Find failed login attempts for a user.
     */
    @Query(Queries.FAILED_LOGIN)
    List<AuditLog> findFailedLoginAttempts(@Param("username") String username);
    
    /**
     * Find failed login attempts for a user with pagination.
     */
    @Query(Queries.FAILED_LOGIN)
    Page<AuditLog> findFailedLoginAttempts(@Param("username") String username, Pageable pageable);
    
    /**
     * Find security violations.
     */
    @Query(Queries.SECURITY_VIOLATION)
    List<AuditLog> findSecurityViolations();
    
    /**
     * Find security violations with pagination.
     */
    @Query(Queries.SECURITY_VIOLATION)
    Page<AuditLog> findSecurityViolations(Pageable pageable);
    
    /**
     * Find audit logs with pagination.
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
