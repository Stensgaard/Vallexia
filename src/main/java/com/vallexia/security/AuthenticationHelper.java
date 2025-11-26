package com.vallexia.security;

import com.vallexia.auth.exception.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper component for extracting authentication information from Spring Security context.
 * Provides centralized methods for accessing current user details from Authentication objects.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Component
public class AuthenticationHelper {
  
  /**
   * Get the current authentication from SecurityContext.
   * 
   * @return the current authentication or null if not authenticated
   */
  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }
  
  /**
   * Get the current user ID from SecurityContext.
   * 
   * @return the current user's ID or null if not authenticated
   */
  public Long getCurrentUserId() {
    Authentication authentication = getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      return null;
    }
    
    if (authentication.getPrincipal() instanceof UserPrincipal) {
      return ((UserPrincipal) authentication.getPrincipal()).getId();
    }
    
    return null;
  }
  
  /**
   * Get the current username from SecurityContext.
   * 
   * @return the current username or null if not authenticated
   */
  public String getCurrentUsername() {
    Authentication authentication = getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      return null;
    }
    
    if (authentication.getPrincipal() instanceof UserPrincipal) {
      return ((UserPrincipal) authentication.getPrincipal()).getUsername();
    }
    
    return null;
  }
  
  /**
   * Check if the current user has a specific role.
   * 
   * @param role the role to check (e.g., "ROLE_ADMIN")
   * @return true if user has the role, false otherwise
   */
  public boolean hasRole(String role) {
    Authentication authentication = getAuthentication();
    if (authentication == null) {
      return false;
    }
    
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(authority -> authority.equals(role));
  }
  
  /**
   * Check if the current user is authenticated.
   * 
   * @return true if authenticated, false otherwise
   */
  public boolean isAuthenticated() {
    Authentication authentication = getAuthentication();
    return authentication != null && authentication.isAuthenticated();
  }
  
  /**
   * Extract the current user ID from the authentication object.
   * 
   * @param authentication the Spring Security authentication object
   * @return the current user's ID
   * @throws AuthenticationException if authentication is null or principal is null
   */
  public Long getCurrentUserId(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new AuthenticationException("User not authenticated");
    }
    
    if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
      throw new AuthenticationException("Invalid authentication principal type");
    }
    
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return userPrincipal.getId();
  }
  
  /**
   * Extract the current UserPrincipal from the authentication object.
   * 
   * @param authentication the Spring Security authentication object
   * @return the current UserPrincipal
   * @throws AuthenticationException if authentication is null or principal is null
   */
  public UserPrincipal getCurrentUserPrincipal(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new AuthenticationException("User not authenticated");
    }
    
    if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
      throw new AuthenticationException("Invalid authentication principal type");
    }
    
    return (UserPrincipal) authentication.getPrincipal();
  }
  
  /**
   * Extract the current username from the authentication object.
   * 
   * @param authentication the Spring Security authentication object
   * @return the current user's username
   * @throws AuthenticationException if authentication is null or principal is null
   */
  public String getCurrentUsername(Authentication authentication) {
    return getCurrentUserPrincipal(authentication).getUsername();
  }
}
