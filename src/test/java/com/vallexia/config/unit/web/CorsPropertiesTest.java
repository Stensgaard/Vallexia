package com.vallexia.config.unit.web;

import com.vallexia.config.web.CorsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CorsProperties validation.
 * Tests CORS configuration validation logic with @PostConstruct.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@DisplayName("CorsProperties Validation Tests")
class CorsPropertiesTest {
  
  private CorsProperties createProperties() {
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    properties.setAllowedMethods(Arrays.asList("GET", "POST"));
    properties.setAllowedHeaders(Arrays.asList("Authorization"));
    properties.setAllowCredentials(true);
    return properties;
  }
  
  @Test
  @DisplayName("Should throw exception when origins is null")
  void shouldThrowExceptionWhenOriginsIsNull() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(null);
    
    // When/Then
    assertThatThrownBy(() -> properties.validateCorsConfiguration())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("CORS allowed origins must be configured");
  }
  
  @Test
  @DisplayName("Should throw exception when origins is empty")
  void shouldThrowExceptionWhenOriginsIsEmpty() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of());
    
    // When/Then
    assertThatThrownBy(() -> properties.validateCorsConfiguration())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("CORS allowed origins must be configured");
  }
  
  @Test
  @DisplayName("Should throw exception when wildcard with credentials")
  void shouldThrowExceptionWhenWildcardWithCredentials() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("*"));
    properties.setAllowedMethods(List.of("GET"));
    properties.setAllowedHeaders(List.of("Authorization"));
    properties.setAllowCredentials(true);
    
    // When/Then
    assertThatThrownBy(() -> properties.validateCorsConfiguration())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot use wildcard origin (*) with credentials enabled");
  }
  
  @Test
  @DisplayName("Should validate successfully when wildcard without credentials")
  void shouldValidateSuccessfullyWhenWildcardWithoutCredentials() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("*"));
    properties.setAllowedMethods(List.of("GET", "POST"));
    properties.setAllowedHeaders(List.of("Authorization"));
    properties.setAllowCredentials(false);
    
    // When - should not throw exception (but logs warning)
    properties.validateCorsConfiguration();
    
    // Then
    assertThat(properties.getAllowedOrigins()).contains("*");
  }
  
  @Test
  @DisplayName("Should throw exception when origins contains empty string")
  void shouldThrowExceptionWhenOriginsContainsEmptyString() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("http://localhost:5173", ""));
    properties.setAllowedMethods(List.of("GET"));
    properties.setAllowedHeaders(List.of("Authorization"));
    
    // When/Then
    assertThatThrownBy(() -> properties.validateCorsConfiguration())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("CORS configuration contains empty origin");
  }
  
  @Test
  @DisplayName("Should throw exception when methods is null")
  void shouldThrowExceptionWhenMethodsIsNull() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("http://localhost:5173"));
    properties.setAllowedMethods(null);
    
    // When/Then
    assertThatThrownBy(() -> properties.validateCorsConfiguration())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("CORS allowed methods must be configured");
  }
  
  @Test
  @DisplayName("Should throw exception when methods is empty")
  void shouldThrowExceptionWhenMethodsIsEmpty() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("http://localhost:5173"));
    properties.setAllowedMethods(List.of());
    
    // When/Then
    assertThatThrownBy(() -> properties.validateCorsConfiguration())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("CORS allowed methods must be configured");
  }
  
  @Test
  @DisplayName("Should validate successfully with valid configuration")
  void shouldValidateSuccessfullyWithValidConfiguration() {
    // Given
    CorsProperties properties = createProperties();
    
    // When - should not throw exception
    properties.validateCorsConfiguration();
    
    // Then - validation passes silently
    assertThat(properties.getAllowedOrigins()).hasSize(1);
    assertThat(properties.getAllowedMethods()).hasSize(2);
  }
  
  @Test
  @DisplayName("Should warn about origin without http or https")
  void shouldWarnAboutOriginWithoutHttpOrHttps() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("localhost:5173"));
    properties.setAllowedMethods(List.of("GET", "POST"));
    properties.setAllowedHeaders(List.of("Authorization"));
    
    // When - should not throw but logs warning
    properties.validateCorsConfiguration();
    
    // Then - validation passes with warning
    assertThat(properties.getAllowedOrigins()).contains("localhost:5173");
  }
}

