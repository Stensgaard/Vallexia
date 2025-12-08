package com.vallexia.audit.util;

/**
 * Query constants for audit log repository operations.
 * Moved from AuditLogRepository interface to comply with SonarQube standards.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-08
 */
public final class AuditLogQueries {
    
    private AuditLogQueries() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Query to find failed login attempts for a user.
     */
    public static final String FAILED_LOGIN = 
        "SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.LOGIN_FAILURE AND a.username = :username ORDER BY a.timestamp DESC";
    
    /**
     * Query to find security violations.
     */
    public static final String SECURITY_VIOLATION = 
        "SELECT a FROM AuditLog a WHERE a.eventType = com.vallexia.audit.entity.enums.EventType.SECURITY_VIOLATION ORDER BY a.timestamp DESC";
}
