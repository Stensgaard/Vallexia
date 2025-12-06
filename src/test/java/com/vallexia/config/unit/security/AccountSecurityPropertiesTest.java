package com.vallexia.config.unit.security;

import com.vallexia.config.security.AccountSecurityProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AccountSecurityProperties validation using Bean Validation.
 * Tests account security properties validation with JSR-303 annotations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("AccountSecurityProperties Validation Tests")
class AccountSecurityPropertiesTest {
  
  private static final Validator VALIDATOR;
  
  static {
    // Validator factory is created once per test class - no need to close in unit tests
    @SuppressWarnings("resource")
    LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
    factory.afterPropertiesSet();
    VALIDATOR = factory.getValidator();
  }
  
  private AccountSecurityProperties createValidProperties() {
    AccountSecurityProperties properties = new AccountSecurityProperties();
    properties.setMaxFailedAttempts(5);
    properties.setDurationMinutes(15);
    return properties;
  }
  
  @Test
  @DisplayName("Should fail validation when maxFailedAttempts is zero")
  void shouldFailValidationWhenMaxFailedAttemptsIsZero() {
    // Given
    AccountSecurityProperties properties = createValidProperties();
    properties.setMaxFailedAttempts(0);
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("maxFailedAttempts") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when maxFailedAttempts is negative")
  void shouldFailValidationWhenMaxFailedAttemptsIsNegative() {
    // Given
    AccountSecurityProperties properties = createValidProperties();
    properties.setMaxFailedAttempts(-1);
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("maxFailedAttempts") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should pass validation when maxFailedAttempts is one or greater")
  void shouldPassValidationWhenMaxFailedAttemptsIsOneOrGreater() {
    // Given
    AccountSecurityProperties properties = createValidProperties();
    properties.setMaxFailedAttempts(1);
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    long maxFailedAttemptsViolations = violations.stream()
        .filter(v -> v.getPropertyPath().toString().equals("maxFailedAttempts"))
        .count();
    assertThat(maxFailedAttemptsViolations).isZero();
  }
  
  @Test
  @DisplayName("Should have default value of 5 for maxFailedAttempts")
  void shouldHaveDefaultValueOf5ForMaxFailedAttempts() {
    // Given
    AccountSecurityProperties properties = new AccountSecurityProperties();
    
    // Then
    assertThat(properties.getMaxFailedAttempts()).isEqualTo(5);
  }
  
  @Test
  @DisplayName("Should fail validation when durationMinutes is zero")
  void shouldFailValidationWhenDurationMinutesIsZero() {
    // Given
    AccountSecurityProperties properties = createValidProperties();
    properties.setDurationMinutes(0);
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("durationMinutes") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when durationMinutes is negative")
  void shouldFailValidationWhenDurationMinutesIsNegative() {
    // Given
    AccountSecurityProperties properties = createValidProperties();
    properties.setDurationMinutes(-1);
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("durationMinutes") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should pass validation when durationMinutes is one or greater")
  void shouldPassValidationWhenDurationMinutesIsOneOrGreater() {
    // Given
    AccountSecurityProperties properties = createValidProperties();
    properties.setDurationMinutes(1);
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    long durationMinutesViolations = violations.stream()
        .filter(v -> v.getPropertyPath().toString().equals("durationMinutes"))
        .count();
    assertThat(durationMinutesViolations).isZero();
  }
  
  @Test
  @DisplayName("Should have default value of 15 for durationMinutes")
  void shouldHaveDefaultValueOf15ForDurationMinutes() {
    // Given
    AccountSecurityProperties properties = new AccountSecurityProperties();
    
    // Then
    assertThat(properties.getDurationMinutes()).isEqualTo(15);
  }
  
  @Test
  @DisplayName("Should pass validation with both valid values")
  void shouldPassValidationWithBothValidValues() {
    // Given
    AccountSecurityProperties properties = createValidProperties();
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isEmpty();
  }
  
  @Test
  @DisplayName("Should fail validation when both values are invalid")
  void shouldFailValidationWhenBothValuesAreInvalid() {
    // Given
    AccountSecurityProperties properties = new AccountSecurityProperties();
    properties.setMaxFailedAttempts(0);
    properties.setDurationMinutes(0);
    
    // When
    Set<ConstraintViolation<AccountSecurityProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).hasSize(2);
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("maxFailedAttempts"));
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("durationMinutes"));
  }
}
