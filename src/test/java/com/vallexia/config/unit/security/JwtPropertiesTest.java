package com.vallexia.config.unit.security;

import com.vallexia.config.security.JwtProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtProperties validation using Bean Validation.
 * Tests JWT properties validation with JSR-303 annotations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("JwtProperties Validation Tests")
class JwtPropertiesTest {
  
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
  
  private JwtProperties createValidProperties() {
    JwtProperties properties = new JwtProperties();
    properties.setSecret("a".repeat(32));
    properties.setAccessTokenExpiration(900000L);
    properties.setRefreshTokenExpiration(86400000L);
    return properties;
  }
  
  @Test
  @DisplayName("Should fail validation when secret is null")
  void shouldFailValidationWhenSecretIsNull() {
    // Given
    JwtProperties properties = createValidProperties();
    properties.setSecret(null);
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("secret") &&
        v.getMessage().contains("must not be blank"));
  }
  
  @Test
  @DisplayName("Should fail validation when secret is empty")
  void shouldFailValidationWhenSecretIsEmpty() {
    // Given
    JwtProperties properties = createValidProperties();
    properties.setSecret("");
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("secret") &&
        v.getMessage().contains("must not be blank"));
  }
  
  @Test
  @DisplayName("Should fail validation when secret is too short")
  void shouldFailValidationWhenSecretIsTooShort() {
    // Given
    JwtProperties properties = createValidProperties();
    properties.setSecret("short");
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("secret") &&
        v.getMessage().contains("at least 32 characters"));
  }
  
  @Test
  @DisplayName("Should pass validation when secret is 32 characters")
  void shouldPassValidationWhenSecretIs32Characters() {
    // Given
    JwtProperties properties = createValidProperties();
    properties.setSecret("a".repeat(32));
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isEmpty();
    assertThat(properties.getSecret()).hasSize(32);
  }
  
  @Test
  @DisplayName("Should pass validation when secret is longer than 32 characters")
  void shouldPassValidationWhenSecretIsLongerThan32Characters() {
    // Given
    JwtProperties properties = createValidProperties();
    properties.setSecret("a".repeat(64));
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isEmpty();
    assertThat(properties.getSecret()).hasSize(64);
  }
  
  @Test
  @DisplayName("Should fail validation when accessTokenExpiration is zero")
  void shouldFailValidationWhenAccessTokenExpirationIsZero() {
    // Given
    JwtProperties properties = createValidProperties();
    properties.setAccessTokenExpiration(0L);
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("accessTokenExpiration") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when refreshTokenExpiration is zero")
  void shouldFailValidationWhenRefreshTokenExpirationIsZero() {
    // Given
    JwtProperties properties = createValidProperties();
    properties.setRefreshTokenExpiration(0L);
    Validator validator = createValidator();
    
    // When
    Set<ConstraintViolation<JwtProperties>> violations = validator.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("refreshTokenExpiration") &&
        v.getMessage().contains("at least 1"));
  }
}
