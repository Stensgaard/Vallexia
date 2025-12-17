package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.validator.ValidMeasurementSystemValidator;
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
 * Unit tests for ValidMeasurementSystemValidator.
 * Tests measurement system validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidMeasurementSystemValidator Unit Tests")
class ValidMeasurementSystemValidatorTest {

  private ValidMeasurementSystemValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidMeasurementSystemValidator();
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
  @DisplayName("Should accept supported measurement system enum instances")
  void shouldAcceptSupportedMeasurementSystemEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedMeasurementSystem.METRIC, null)).isTrue();
    assertThat(validator.isValid(SupportedMeasurementSystem.IMPERIAL, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported codes case-insensitively")
  void shouldAcceptSupportedCodes() {
    // When/Then
    assertThat(validator.isValid("metric", null)).isTrue();
    assertThat(validator.isValid("METRIC", null)).isTrue();
    assertThat(validator.isValid("imperial", null)).isTrue();
    assertThat(validator.isValid("IMPERIAL", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim measurement system codes before validation")
  void shouldTrimMeasurementSystemCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  METRIC  ", null)).isTrue();
    assertThat(validator.isValid("\tIMPERIAL\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid measurement system codes")
  void shouldRejectUnknownMeasurementSystemCodes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid("INVALID", context)).isFalse();
    assertThat(validator.isValid("CUSTOM", context)).isFalse();
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
