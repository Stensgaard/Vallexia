package com.vallexia.audit.repository;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.util.AuditLogQueries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    @Query(AuditLogQueries.FAILED_LOGIN)
    List<AuditLog> findFailedLoginAttempts(@Param("username") String username);
    
    /**
     * Find failed login attempts for a user with pagination.
     */
    @Query(AuditLogQueries.FAILED_LOGIN)
    Page<AuditLog> findFailedLoginAttempts(@Param("username") String username, Pageable pageable);
    
    /**
     * Find security violations.
     */
    @Query(AuditLogQueries.SECURITY_VIOLATION)
    List<AuditLog> findSecurityViolations();
    
    /**
     * Find security violations with pagination.
     */
    @Query(AuditLogQueries.SECURITY_VIOLATION)
    Page<AuditLog> findSecurityViolations(Pageable pageable);
    
    /**
     * Find audit logs with pagination.
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    /**
     * Efficiently delete audit logs older than the specified cutoff date.
     * Uses direct SQL DELETE for better performance on large datasets.
     * 
     * @param cutoffDate the cutoff date - logs older than this will be deleted
     * @return number of records deleted
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count audit logs older than the specified cutoff date.
     * Used for logging and monitoring purposes.
     * 
     * @param cutoffDate the cutoff date
     * @return number of records that would be deleted
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    long countByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
