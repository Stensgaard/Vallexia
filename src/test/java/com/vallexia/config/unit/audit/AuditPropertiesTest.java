package com.vallexia.config.unit.audit;

import com.vallexia.config.audit.AuditProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuditProperties validation using Bean Validation.
 * Tests audit properties validation with JSR-303 annotations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("AuditProperties Validation Tests")
class AuditPropertiesTest {
  
  private static final Validator VALIDATOR;
  
  static {
    // Validator factory is created once per test class - no need to close in unit tests
    @SuppressWarnings("resource")
    LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
    factory.afterPropertiesSet();
    VALIDATOR = factory.getValidator();
  }
  
  private AuditProperties createValidProperties() {
    AuditProperties properties = new AuditProperties();
    properties.setRetentionDays(90);
    properties.setFallbackLogPath("deployment/logs/audit-fallback.log");
    return properties;
  }
  
  @Test
  @DisplayName("Should fail validation when retentionDays is zero")
  void shouldFailValidationWhenRetentionDaysIsZero() {
    // Given
    AuditProperties properties = createValidProperties();
    properties.setRetentionDays(0);
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("retentionDays") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when retentionDays is negative")
  void shouldFailValidationWhenRetentionDaysIsNegative() {
    // Given
    AuditProperties properties = createValidProperties();
    properties.setRetentionDays(-1);
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("retentionDays") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should pass validation when retentionDays is one or greater")
  void shouldPassValidationWhenRetentionDaysIsOneOrGreater() {
    // Given
    AuditProperties properties = createValidProperties();
    properties.setRetentionDays(1);
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    long retentionDaysViolations = violations.stream()
        .filter(v -> v.getPropertyPath().toString().equals("retentionDays"))
        .count();
    assertThat(retentionDaysViolations).isZero();
  }
  
  @Test
  @DisplayName("Should have default value of 90 for retentionDays")
  void shouldHaveDefaultValueOf90ForRetentionDays() {
    // Given
    AuditProperties properties = new AuditProperties();
    
    // Then
    assertThat(properties.getRetentionDays()).isEqualTo(90);
  }
  
  @Test
  @DisplayName("Should fail validation when fallbackLogPath is null")
  void shouldFailValidationWhenFallbackLogPathIsNull() {
    // Given
    AuditProperties properties = createValidProperties();
    properties.setFallbackLogPath(null);
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("fallbackLogPath") &&
        v.getMessage().contains("must not be blank"));
  }
  
  @Test
  @DisplayName("Should fail validation when fallbackLogPath is blank")
  void shouldFailValidationWhenFallbackLogPathIsBlank() {
    // Given
    AuditProperties properties = createValidProperties();
    properties.setFallbackLogPath("");
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("fallbackLogPath") &&
        v.getMessage().contains("must not be blank"));
  }
  
  @Test
  @DisplayName("Should fail validation when fallbackLogPath is whitespace only")
  void shouldFailValidationWhenFallbackLogPathIsWhitespaceOnly() {
    // Given
    AuditProperties properties = createValidProperties();
    properties.setFallbackLogPath("   ");
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("fallbackLogPath") &&
        v.getMessage().contains("must not be blank"));
  }
  
  @Test
  @DisplayName("Should pass validation when fallbackLogPath is not blank")
  void shouldPassValidationWhenFallbackLogPathIsNotBlank() {
    // Given
    AuditProperties properties = createValidProperties();
    properties.setFallbackLogPath("deployment/logs/audit-fallback.log");
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    long fallbackLogPathViolations = violations.stream()
        .filter(v -> v.getPropertyPath().toString().equals("fallbackLogPath"))
        .count();
    assertThat(fallbackLogPathViolations).isZero();
  }
  
  @Test
  @DisplayName("Should have default value for fallbackLogPath")
  void shouldHaveDefaultValueForFallbackLogPath() {
    // Given
    AuditProperties properties = new AuditProperties();
    
    // Then
    assertThat(properties.getFallbackLogPath()).isEqualTo("deployment/logs/audit-fallback.log");
  }
  
  @Test
  @DisplayName("Should pass validation with all valid values")
  void shouldPassValidationWithAllValidValues() {
    // Given
    AuditProperties properties = createValidProperties();
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isEmpty();
  }
  
  @Test
  @DisplayName("Should fail validation when both retentionDays and fallbackLogPath are invalid")
  void shouldFailValidationWhenBothRetentionDaysAndFallbackLogPathAreInvalid() {
    // Given
    AuditProperties properties = new AuditProperties();
    properties.setRetentionDays(0);
    properties.setFallbackLogPath(null);
    
    // When
    Set<ConstraintViolation<AuditProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).hasSize(2);
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("retentionDays"));
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("fallbackLogPath"));
  }
}
