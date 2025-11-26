package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.validator.ValidLocaleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidLocaleValidator.
 * Tests locale validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidLocaleValidator Unit Tests")
class ValidLocaleValidatorTest {

  private ValidLocaleValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidLocaleValidator();
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
  @DisplayName("Should accept supported locale enum instances")
  void shouldAcceptSupportedLocaleEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedLocale.EN, null)).isTrue();
    assertThat(validator.isValid(SupportedLocale.DA, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported locale codes case-insensitively")
  void shouldAcceptSupportedLocaleCodes() {
    // When/Then
    assertThat(validator.isValid("en", null)).isTrue();
    assertThat(validator.isValid("EN", null)).isTrue();
    assertThat(validator.isValid("da", null)).isTrue();
    assertThat(validator.isValid("DA", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim locale codes before validation")
  void shouldTrimLocaleCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  en  ", null)).isTrue();
    assertThat(validator.isValid("\tda\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid locale codes")
  void shouldRejectUnknownLocaleCodes() {
    // When/Then
    assertThat(validator.isValid("fr", null)).isFalse();
    assertThat(validator.isValid("de", null)).isFalse();
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
