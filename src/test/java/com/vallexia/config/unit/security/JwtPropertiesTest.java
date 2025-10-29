package com.vallexia.config.unit.security;

import com.vallexia.config.security.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtProperties validation.
 * Tests JWT secret validation logic with @PostConstruct.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@DisplayName("JwtProperties Validation Tests")
class JwtPropertiesTest {
  
  private JwtProperties createProperties() {
    JwtProperties properties = new JwtProperties();
    properties.setAccessTokenExpiration(900000L);
    properties.setRefreshTokenExpiration(86400000L);
    return properties;
  }
  
  @Test
  @DisplayName("Should throw exception when secret is null")
  void shouldThrowExceptionWhenSecretIsNull() {
    // Given
    JwtProperties properties = createProperties();
    properties.setSecret(null);
    
    // When/Then
    assertThatThrownBy(() -> properties.validateSecret())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JWT secret is not configured");
  }
  
  @Test
  @DisplayName("Should throw exception when secret is empty")
  void shouldThrowExceptionWhenSecretIsEmpty() {
    // Given
    JwtProperties properties = createProperties();
    properties.setSecret("");
    
    // When/Then
    assertThatThrownBy(() -> properties.validateSecret())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JWT secret is not configured");
  }
  
  @Test
  @DisplayName("Should throw exception when secret is too short")
  void shouldThrowExceptionWhenSecretIsTooShort() {
    // Given
    JwtProperties properties = createProperties();
    properties.setSecret("short");
    
    // When/Then
    assertThatThrownBy(() -> properties.validateSecret())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JWT secret must be at least 32 characters long");
  }
  
  @Test
  @DisplayName("Should validate successfully when secret is 32 characters")
  void shouldValidateSuccessfullyWhenSecretIs32Characters() {
    // Given
    JwtProperties properties = createProperties();
    properties.setSecret("a".repeat(32));
    
    // When - should not throw exception
    properties.validateSecret();
    
    // Then - validation passes silently
    assertThat(properties.getSecret()).hasSize(32);
  }
  
  @Test
  @DisplayName("Should validate successfully when secret is longer than 32 characters")
  void shouldValidateSuccessfullyWhenSecretIsLongerThan32Characters() {
    // Given
    JwtProperties properties = createProperties();
    properties.setSecret("a".repeat(64));
    
    // When - should not throw exception
    properties.validateSecret();
    
    // Then - validation passes silently
    assertThat(properties.getSecret()).hasSize(64);
  }
}

