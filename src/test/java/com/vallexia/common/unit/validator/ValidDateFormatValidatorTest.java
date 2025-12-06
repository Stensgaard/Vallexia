package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.validator.ValidDateFormatValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidDateFormatValidator.
 * Tests date format validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidDateFormatValidator Unit Tests")
class ValidDateFormatValidatorTest {

  private ValidDateFormatValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidDateFormatValidator();
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
  @DisplayName("Should accept supported date format enum instances")
  void shouldAcceptSupportedDateFormatEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedDateFormat.MM_DD_YYYY, null)).isTrue();
    assertThat(validator.isValid(SupportedDateFormat.DD_MM_YYYY, null)).isTrue();
    assertThat(validator.isValid(SupportedDateFormat.YYYY_MM_DD, null)).isTrue();
    assertThat(validator.isValid(SupportedDateFormat.DD_MM_YYYY_DOT, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept known codes case-insensitively")
  void shouldAcceptKnownCodes() {
    // When/Then
    assertThat(validator.isValid("MM_DD_YYYY", null)).isTrue();
    assertThat(validator.isValid("mm_dd_yyyy", null)).isTrue();
    assertThat(validator.isValid("DD_MM_YYYY", null)).isTrue();
    assertThat(validator.isValid("YYYY_MM_DD", null)).isTrue();
    assertThat(validator.isValid("DD_MM_YYYY_DOT", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim date format codes before validation")
  void shouldTrimDateFormatCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  MM_DD_YYYY  ", null)).isTrue();
    assertThat(validator.isValid("\tDD_MM_YYYY\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown values")
  void shouldRejectUnknownCodes() {
    // When/Then
    assertThat(validator.isValid("Y2K", null)).isFalse();
    assertThat(validator.isValid("INVALID", null)).isFalse();
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
