package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.validator.ValidCurrencyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidCurrencyValidator.
 * Tests currency validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidCurrencyValidator Unit Tests")
class ValidCurrencyValidatorTest {

  private ValidCurrencyValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidCurrencyValidator();
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
  @DisplayName("Should accept supported currency enum instances")
  void shouldAcceptSupportedCurrencyEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedCurrency.USD, null)).isTrue();
    assertThat(validator.isValid(SupportedCurrency.DKK, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported currency codes case-insensitively")
  void shouldAcceptSupportedCurrencyCodes() {
    // When/Then
    assertThat(validator.isValid("usd", null)).isTrue();
    assertThat(validator.isValid("USD", null)).isTrue();
    assertThat(validator.isValid("DKK", null)).isTrue();
    assertThat(validator.isValid("dkk", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim and validate currency codes")
  void shouldTrimAndValidateCurrencyCodes() {
    // When/Then
    assertThat(validator.isValid("  USD  ", null)).isTrue();
    assertThat(validator.isValid("\tDKK\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid currency codes")
  void shouldRejectUnknownCurrencyCodes() {
    // When/Then
    assertThat(validator.isValid("EUR", null)).isFalse();
    assertThat(validator.isValid("GBP", null)).isFalse();
    assertThat(validator.isValid("INVALID", null)).isFalse();
    assertThat(validator.isValid("XX", null)).isFalse();
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
