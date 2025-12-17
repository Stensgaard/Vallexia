package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.validator.ValidCurrencyValidator;
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
 * Unit tests for ValidCurrencyValidator.
 * Tests currency validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidCurrencyValidator Unit Tests")
class ValidCurrencyValidatorTest {

  private ValidCurrencyValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidCurrencyValidator();
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
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid("EUR", context)).isFalse();
    assertThat(validator.isValid("GBP", context)).isFalse();
    assertThat(validator.isValid("INVALID", context)).isFalse();
    assertThat(validator.isValid("XX", context)).isFalse();
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
