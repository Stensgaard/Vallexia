package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.common.validator.ValidTimezoneValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidTimezoneValidator.
 * Tests timezone validation with null safety, enum instances, string values, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidTimezoneValidator Unit Tests")
class ValidTimezoneValidatorTest {

  private ValidTimezoneValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidTimezoneValidator();
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
  @DisplayName("Should accept supported timezone enum instances")
  void shouldAcceptSupportedTimezoneEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedTimezone.UTC, null)).isTrue();
    assertThat(validator.isValid(SupportedTimezone.AMERICA_NEW_YORK, null)).isTrue();
    assertThat(validator.isValid(SupportedTimezone.AMERICA_LOS_ANGELES, null)).isTrue();
    assertThat(validator.isValid(SupportedTimezone.EUROPE_COPENHAGEN, null)).isTrue();
  }

  // ==================== String Value Tests ====================

  @Test
  @DisplayName("Should accept supported timezone values case-insensitively")
  void shouldAcceptSupportedTimezoneValues() {
    // When/Then
    assertThat(validator.isValid("america/new_york", null)).isTrue();
    assertThat(validator.isValid("AMERICA/LOS_ANGELES", null)).isTrue();
    assertThat(validator.isValid("UTC", null)).isTrue();
    assertThat(validator.isValid("Europe/Copenhagen", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim and validate timezone values with whitespace")
  void shouldTrimAndValidateTimezoneValues() {
    // When/Then
    assertThat(validator.isValid("  America/New_York  ", null)).isTrue();
    assertThat(validator.isValid("\tUTC\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid timezone values")
  void shouldRejectUnknownTimezoneValues() {
    // When/Then
    assertThat(validator.isValid("America/Chicago", null)).isFalse();
    assertThat(validator.isValid("Invalid/Timezone", null)).isFalse();
    assertThat(validator.isValid("not-a-timezone", null)).isFalse();
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
