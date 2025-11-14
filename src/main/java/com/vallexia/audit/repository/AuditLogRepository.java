package com.vallexia.audit.repository;

import com.vallexia.audit.entity.AuditLog;
import com.vallexia.audit.entity.enums.EventType;
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
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
     * Find audit logs by username.
     */
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    
    /**
     * Find audit logs by username with pagination.
     */
    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);
    
    /**
     * Find audit logs by event type.
     */
    List<AuditLog> findByEventTypeOrderByTimestampDesc(EventType eventType);
    
    /**
     * Find audit logs by event type with pagination.
     */
    Page<AuditLog> findByEventTypeOrderByTimestampDesc(EventType eventType, Pageable pageable);
    
    /**
     * Find audit logs by IP address.
     */
    List<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress);
    
    /**
     * Find audit logs by IP address with pagination.
     */
    Page<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);
    
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
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.LOGIN_FAILURE AND a.username = :username ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttempts(@Param("username") String username);
    
    /**
     * Find failed login attempts for a user with pagination.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.LOGIN_FAILURE AND a.username = :username ORDER BY a.timestamp DESC")
    Page<AuditLog> findFailedLoginAttempts(@Param("username") String username, Pageable pageable);
    
    /**
     * Find security violations.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.SECURITY_VIOLATION ORDER BY a.timestamp DESC")
    List<AuditLog> findSecurityViolations();
    
    /**
     * Find security violations with pagination.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.SECURITY_VIOLATION ORDER BY a.timestamp DESC")
    Page<AuditLog> findSecurityViolations(Pageable pageable);
    
    /**
     * Find audit logs with pagination.
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    /**
     * Count events by type within date range.
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end GROUP BY a.eventType")
    List<Object[]> countEventsByType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
