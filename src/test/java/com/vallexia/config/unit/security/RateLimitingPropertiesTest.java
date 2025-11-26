package com.vallexia.config.unit.security;

import com.vallexia.config.security.RateLimitingProperties;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RateLimitingProperties validation using Bean Validation.
 * Tests rate limiting properties validation with JSR-303 annotations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("RateLimitingProperties Validation Tests")
class RateLimitingPropertiesTest {
  
  private static final Validator VALIDATOR;
  
  static {
    // Validator factory is created once per test class - no need to close in unit tests
    @SuppressWarnings("resource")
    LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
    factory.afterPropertiesSet();
    VALIDATOR = factory.getValidator();
  }
  
  private RateLimitingProperties createValidProperties() {
    RateLimitingProperties properties = new RateLimitingProperties();
    
    RateLimitingProperties.LoginConfig loginConfig = new RateLimitingProperties.LoginConfig();
    loginConfig.setRequests(5);
    loginConfig.setDurationMinutes(1);
    properties.setLogin(loginConfig);
    
    RateLimitingProperties.RegistrationConfig registrationConfig = 
        new RateLimitingProperties.RegistrationConfig();
    registrationConfig.setRequests(3);
    registrationConfig.setDurationMinutes(5);
    properties.setRegistration(registrationConfig);
    
    RateLimitingProperties.GeneralApiConfig generalApiConfig = 
        new RateLimitingProperties.GeneralApiConfig();
    generalApiConfig.setRequests(100);
    generalApiConfig.setDurationMinutes(1);
    properties.setGeneralApi(generalApiConfig);
    
    RateLimitingProperties.RefreshConfig refreshConfig = 
        new RateLimitingProperties.RefreshConfig();
    refreshConfig.setRequests(10);
    refreshConfig.setDurationMinutes(5);
    properties.setRefresh(refreshConfig);
    
    return properties;
  }
  
  @Test
  @DisplayName("Should fail validation when LoginConfig requests is zero")
  void shouldFailValidationWhenLoginConfigRequestsIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getLogin().setRequests(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("login.requests") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when LoginConfig durationMinutes is zero")
  void shouldFailValidationWhenLoginConfigDurationMinutesIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getLogin().setDurationMinutes(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("login.durationMinutes") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should pass validation when LoginConfig has valid values")
  void shouldPassValidationWhenLoginConfigHasValidValues() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    // Filter out login config violations
    long loginViolations = violations.stream()
        .filter(v -> v.getPropertyPath().toString().startsWith("login"))
        .count();
    assertThat(loginViolations).isZero();
  }
  
  @Test
  @DisplayName("Should fail validation when RegistrationConfig requests is zero")
  void shouldFailValidationWhenRegistrationConfigRequestsIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getRegistration().setRequests(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("registration.requests") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when RegistrationConfig durationMinutes is zero")
  void shouldFailValidationWhenRegistrationConfigDurationMinutesIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getRegistration().setDurationMinutes(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("registration.durationMinutes") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when GeneralApiConfig requests is zero")
  void shouldFailValidationWhenGeneralApiConfigRequestsIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getGeneralApi().setRequests(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("generalApi.requests") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when GeneralApiConfig durationMinutes is zero")
  void shouldFailValidationWhenGeneralApiConfigDurationMinutesIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getGeneralApi().setDurationMinutes(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("generalApi.durationMinutes") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when RefreshConfig requests is zero")
  void shouldFailValidationWhenRefreshConfigRequestsIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getRefresh().setRequests(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("refresh.requests") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should fail validation when RefreshConfig durationMinutes is zero")
  void shouldFailValidationWhenRefreshConfigDurationMinutesIsZero() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    properties.getRefresh().setDurationMinutes(0);
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> 
        v.getPropertyPath().toString().equals("refresh.durationMinutes") &&
        v.getMessage().contains("at least 1"));
  }
  
  @Test
  @DisplayName("Should pass validation with all valid nested configs")
  void shouldPassValidationWithAllValidNestedConfigs() {
    // Given
    RateLimitingProperties properties = createValidProperties();
    
    // When
    Set<ConstraintViolation<RateLimitingProperties>> violations = VALIDATOR.validate(properties);
    
    // Then
    assertThat(violations).isEmpty();
  }
  
  @Test
  @DisplayName("Should have correct default values")
  void shouldHaveCorrectDefaultValues() {
    // Given
    RateLimitingProperties properties = new RateLimitingProperties();
    
    // Then
    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getLogin().isEnabled()).isTrue();
    assertThat(properties.getLogin().getRequests()).isEqualTo(5);
    assertThat(properties.getLogin().getDurationMinutes()).isEqualTo(1);
    assertThat(properties.getRegistration().isEnabled()).isTrue();
    assertThat(properties.getRegistration().getRequests()).isEqualTo(3);
    assertThat(properties.getRegistration().getDurationMinutes()).isEqualTo(5);
    assertThat(properties.getGeneralApi().isEnabled()).isTrue();
    assertThat(properties.getGeneralApi().getRequests()).isEqualTo(100);
    assertThat(properties.getGeneralApi().getDurationMinutes()).isEqualTo(1);
    assertThat(properties.getRefresh().isEnabled()).isTrue();
    assertThat(properties.getRefresh().getRequests()).isEqualTo(10);
    assertThat(properties.getRefresh().getDurationMinutes()).isEqualTo(5);
  }
}
