package com.vallexia.common.unit.validator;

import com.vallexia.common.validator.ValidSubscriptionStatusValidator;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidSubscriptionStatusValidator.
 * Tests subscription status validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidSubscriptionStatusValidator Unit Tests")
class ValidSubscriptionStatusValidatorTest {

  private ValidSubscriptionStatusValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidSubscriptionStatusValidator();
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
  @DisplayName("Should accept supported subscription status enum instances")
  void shouldAcceptSupportedSubscriptionStatusEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SubscriptionStatus.FREE, null)).isTrue();
    assertThat(validator.isValid(SubscriptionStatus.PREMIUM, null)).isTrue();
    assertThat(validator.isValid(SubscriptionStatus.FAMILY, null)).isTrue();
    assertThat(validator.isValid(SubscriptionStatus.CANCELLED, null)).isTrue();
    assertThat(validator.isValid(SubscriptionStatus.EXPIRED, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported subscription status codes case-insensitively")
  void shouldAcceptSupportedSubscriptionStatusCodes() {
    // When/Then
    assertThat(validator.isValid("FREE", null)).isTrue();
    assertThat(validator.isValid("free", null)).isTrue();
    assertThat(validator.isValid("PREMIUM", null)).isTrue();
    assertThat(validator.isValid("premium", null)).isTrue();
    assertThat(validator.isValid("FAMILY", null)).isTrue();
    assertThat(validator.isValid("family", null)).isTrue();
    assertThat(validator.isValid("CANCELLED", null)).isTrue();
    assertThat(validator.isValid("cancelled", null)).isTrue();
    assertThat(validator.isValid("EXPIRED", null)).isTrue();
    assertThat(validator.isValid("expired", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim subscription status codes before validation")
  void shouldTrimSubscriptionStatusCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  FREE  ", null)).isTrue();
    assertThat(validator.isValid("\tPREMIUM\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid subscription status codes")
  void shouldRejectUnknownSubscriptionStatusCodes() {
    // When/Then
    assertThat(validator.isValid("INVALID", null)).isFalse();
    assertThat(validator.isValid("ACTIVE", null)).isFalse();
    assertThat(validator.isValid("TRIAL", null)).isFalse();
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
