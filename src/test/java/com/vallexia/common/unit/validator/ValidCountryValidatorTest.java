package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.validator.ValidCountryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidCountryValidator.
 * Tests country validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidCountryValidator Unit Tests")
class ValidCountryValidatorTest {

  private ValidCountryValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidCountryValidator();
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
    // When/Then
    assertThat(validator.isValid("GB", null)).isFalse();
    assertThat(validator.isValid("CA", null)).isFalse();
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
