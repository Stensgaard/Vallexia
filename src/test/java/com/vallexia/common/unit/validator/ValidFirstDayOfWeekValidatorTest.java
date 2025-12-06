package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.validator.ValidFirstDayOfWeekValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidFirstDayOfWeekValidator.
 * Tests first day of week validation with null safety, enum instances, string inputs, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidFirstDayOfWeekValidator Unit Tests")
class ValidFirstDayOfWeekValidatorTest {

  private ValidFirstDayOfWeekValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidFirstDayOfWeekValidator();
    validator.initialize(null);
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
  @DisplayName("Should accept supported first day of week enum instances")
  void shouldAcceptSupportedFirstDayOfWeekEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedFirstDayOfWeek.SUNDAY, null)).isTrue();
    assertThat(validator.isValid(SupportedFirstDayOfWeek.MONDAY, null)).isTrue();
  }

  // ==================== String Input Tests ====================

  @Test
  @DisplayName("Should accept string inputs case-insensitively")
  void shouldAcceptStringInputs() {
    // When/Then
    assertThat(validator.isValid("sunday", null)).isTrue();
    assertThat(validator.isValid("MONDAY", null)).isTrue();
    assertThat(validator.isValid("Monday", null)).isTrue();
    assertThat(validator.isValid("SUNDAY", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim first day of week codes before validation")
  void shouldTrimFirstDayOfWeekCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  SUNDAY  ", null)).isTrue();
    assertThat(validator.isValid("\tMONDAY\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid values")
  void shouldRejectUnknownValues() {
    // When/Then
    assertThat(validator.isValid("tuesday", null)).isFalse();
    assertThat(validator.isValid("wednesday", null)).isFalse();
    assertThat(validator.isValid("invalid", null)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-enum types")
  void shouldRejectNonStringAndNonEnumTypes() {
    // When/Then
    assertThat(validator.isValid(123, null)).isFalse();
    assertThat(validator.isValid(true, null)).isFalse();
    assertThat(validator.isValid(new Object(), null)).isFalse();
  }
}
