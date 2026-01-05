package com.vallexia.audit.entity.enums;

/**
 * Enum representing different types of audit events tracked in the system.
 * Used for categorizing security and user activity events.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
public enum EventType {
  
  /**
   * Successful user authentication.
   */
  LOGIN_SUCCESS,
  
  /**
   * Failed login attempt.
   */
  LOGIN_FAILURE,
  
  /**
   * User logout event.
   */
  LOGOUT,
  
  /**
   * New user registration.
   */
  REGISTRATION,
  
  /**
   * User profile update.
   */
  PROFILE_UPDATE,
  
  /**
   * Password change event.
   */
  PASSWORD_CHANGE,
  
  /**
   * Account locked due to security policy.
   */
  ACCOUNT_LOCKED,
  
  /**
   * Account unlocked by administrator.
   */
  ACCOUNT_UNLOCKED,
  
  /**
   * JWT token refresh.
   */
  TOKEN_REFRESH,
  
  /**
   * General API access event.
   */
  API_ACCESS,
  
  /**
   * Security policy violation detected.
   */
  SECURITY_VIOLATION,
  
}
