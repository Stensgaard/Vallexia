package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.validator.ValidCountryValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ValidCountryValidator.
 * Tests country validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidCountryValidator Unit Tests")
class ValidCountryValidatorTest {

  private ValidCountryValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidCountryValidator();
    validator.initialize(null);
  }

  /**
   * Creates a mocked ConstraintValidatorContext for testing validation failures.
   * 
   * @return a mocked context with proper method chaining setup
   */
  private ConstraintValidatorContext createMockContext() {
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    ConstraintValidatorContext.ConstraintViolationBuilder builder = 
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    
    doNothing().when(context).disableDefaultConstraintViolation();
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    doReturn(context).when(builder).addConstraintViolation();
    
    return context;
  }

  // ==================== Null and Empty Value Tests ====================

  @Test
  @DisplayName("Should allow null values")
  void shouldAllowNullValues() {
    // When/Then
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  @DisplayName("Should allow empty or whitespace-only strings")
  void shouldAllowEmptyOrWhitespaceStrings() {
    // When/Then
    assertThat(validator.isValid("", null)).isTrue();
    assertThat(validator.isValid("   ", null)).isTrue();
  }

  // ==================== Enum Instance Tests ====================

  @Test
  @DisplayName("Should accept supported country enum instances")
  void shouldAcceptSupportedCountryEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedCountry.US, null)).isTrue();
    assertThat(validator.isValid(SupportedCountry.DK, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported country codes case-insensitively")
  void shouldAcceptSupportedCountryCodes() {
    // When/Then
    assertThat(validator.isValid("US", null)).isTrue();
    assertThat(validator.isValid("us", null)).isTrue();
    assertThat(validator.isValid("DK", null)).isTrue();
    assertThat(validator.isValid("dk", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim country codes before validation")
  void shouldTrimCountryCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  US  ", null)).isTrue();
    assertThat(validator.isValid("\tDK\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid country codes")
  void shouldRejectUnknownCountryCodes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid("GB", context)).isFalse();
    assertThat(validator.isValid("CA", context)).isFalse();
    assertThat(validator.isValid("INVALID", context)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-enum types")
  void shouldRejectNonStringAndNonEnumTypes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid(123, context)).isFalse();
    assertThat(validator.isValid(true, context)).isFalse();
    assertThat(validator.isValid(new Object(), context)).isFalse();
  }
}
