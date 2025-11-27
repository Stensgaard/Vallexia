package com.vallexia.security.unit;

import com.vallexia.auth.exception.AuthenticationException;
import com.vallexia.security.AuthenticationHelper;
import com.vallexia.security.UserPrincipal;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for AuthenticationHelper.
 * Tests type safety, null handling, and authentication context extraction.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("AuthenticationHelper Unit Tests")
class AuthenticationHelperTest {
  
  private AuthenticationHelper authenticationHelper;
  
  @BeforeEach
  void setUp() {
    authenticationHelper = new AuthenticationHelper();
    SecurityContextHolder.clearContext();
  }
  
  // ==================== Type Safety Tests ====================
  
  @Test
  @DisplayName("Should throw AuthenticationException when principal is not UserPrincipal in getCurrentUserId")
  void shouldThrowExceptionWhenPrincipalIsNotUserPrincipalInGetCurrentUserId() {
    // Given - authentication with non-UserPrincipal
    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
        "testuser", "password", Collections.emptyList());
    Authentication auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, Collections.emptyList());
    
    // When/Then
    assertThatThrownBy(() -> authenticationHelper.getCurrentUserId(auth))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid authentication principal type");
  }
  
  // ==================== Null Handling Tests ====================
  
  @Test
  @DisplayName("Should throw AuthenticationException when authentication is null in getCurrentUserId")
  void shouldThrowExceptionWhenAuthenticationIsNullInGetCurrentUserId() {
    // When/Then
    assertThatThrownBy(() -> authenticationHelper.getCurrentUserId(null))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("User not authenticated");
  }
  
  @Test
  @DisplayName("Should throw AuthenticationException when principal is null in getCurrentUserId")
  void shouldThrowExceptionWhenPrincipalIsNullInGetCurrentUserId() {
    // Given - authentication with null principal
    Authentication auth = new UsernamePasswordAuthenticationToken(
        null, null, Collections.emptyList());
    
    // When/Then
    assertThatThrownBy(() -> authenticationHelper.getCurrentUserId(auth))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("User not authenticated");
  }
  
  @Test
  @DisplayName("Should return null when not authenticated in getCurrentUserId")
  void shouldReturnNullWhenNotAuthenticatedInGetCurrentUserId() {
    // When - no authentication in context
    Long userId = authenticationHelper.getCurrentUserId();
    
    // Then
    assertThat(userId).isNull();
  }
  
  // ==================== Role Checking Tests ====================
  
  @Test
  @DisplayName("Should return true when user has the specified role")
  void shouldReturnTrueWhenUserHasRole() {
    // Given
    User user = UserTestFixtures.createUser();
    UserPrincipal userPrincipal = UserPrincipal.create(user);
    Authentication auth = new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    
    // When
    boolean hasRole = authenticationHelper.hasRole("ROLE_USER");
    
    // Then
    assertThat(hasRole).isTrue();
  }
  
  @Test
  @DisplayName("Should return false when user does not have the specified role")
  void shouldReturnFalseWhenUserDoesNotHaveRole() {
    // Given
    User user = UserTestFixtures.createUser();
    UserPrincipal userPrincipal = UserPrincipal.create(user);
    Authentication auth = new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    
    // When
    boolean hasRole = authenticationHelper.hasRole("ROLE_ADMIN");
    
    // Then
    assertThat(hasRole).isFalse();
  }
  
  @Test
  @DisplayName("Should return false when not authenticated in hasRole")
  void shouldReturnFalseWhenNotAuthenticatedInHasRole() {
    // When - no authentication in context
    boolean hasRole = authenticationHelper.hasRole("ROLE_USER");
    
    // Then
    assertThat(hasRole).isFalse();
  }
  
}
