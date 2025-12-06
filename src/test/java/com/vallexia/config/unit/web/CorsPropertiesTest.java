package com.vallexia.config.unit.web;

import com.vallexia.config.web.CorsProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CorsProperties validation.
 * Tests CORS configuration validation using Bean Validation and @PostConstruct.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("CorsProperties Validation Tests")
class CorsPropertiesTest {
  
  private static final Validator VALIDATOR;
  
  static {
    // Validator factory is created once per test class - no need to close in unit tests
    @SuppressWarnings("resource")
    LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
    factory.afterPropertiesSet();
    VALIDATOR = factory.getValidator();
  }
  
  private Validator createValidator() {
    return VALIDATOR;
  }
  
  private CorsProperties createValidProperties() {
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    properties.setAllowedMethods(Arrays.asList("GET", "POST"));
    properties.setAllowedHeaders(Arrays.asList("Authorization"));
    properties.setAllowCredentials(true);
    return properties;
  }
  
  @Test
  @DisplayName("Should fail Bean Validation when origins is null")
  void shouldFailBeanValidationWhenOriginsIsNull() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(null);
    properties.setAllowedMethods(List.of("GET"));
    properties.setAllowedHeaders(List.of("Authorization"));
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<CorsProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("allowedOrigins") &&
        v.getMessage().contains("CORS allowed origins must be configured"));
  }
  
  @Test
  @DisplayName("Should fail Bean Validation when origins is empty")
  void shouldFailBeanValidationWhenOriginsIsEmpty() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of());
    properties.setAllowedMethods(List.of("GET"));
    properties.setAllowedHeaders(List.of("Authorization"));
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<CorsProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("allowedOrigins") &&
        v.getMessage().contains("CORS allowed origins must be configured"));
  }
  
  @Test
  @DisplayName("Should fail Bean Validation when methods is null")
  void shouldFailBeanValidationWhenMethodsIsNull() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("http://localhost:5173"));
    properties.setAllowedMethods(null);
    properties.setAllowedHeaders(List.of("Authorization"));
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<CorsProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("allowedMethods") &&
        v.getMessage().contains("CORS allowed methods must be configured"));
  }
  
  @Test
  @DisplayName("Should fail Bean Validation when methods is empty")
  void shouldFailBeanValidationWhenMethodsIsEmpty() {
    // Given
    CorsProperties properties = new CorsProperties();
    properties.setAllowedOrigins(List.of("http://localhost:5173"));
    properties.setAllowedMethods(List.of());
    properties.setAllowedHeaders(List.of("Authorization"));
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<CorsProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("allowedMethods") &&
        v.getMessage().contains("CORS allowed methods must be configured"));
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
  @DisplayName("Should validate successfully with valid configuration")
  void shouldValidateSuccessfullyWithValidConfiguration() {
    // Given
    CorsProperties properties = createValidProperties();
    
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
