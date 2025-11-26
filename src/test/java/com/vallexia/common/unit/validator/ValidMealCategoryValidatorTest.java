package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.validator.ValidMealCategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidMealCategoryValidator.
 * Tests meal category validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("ValidMealCategoryValidator Unit Tests")
class ValidMealCategoryValidatorTest {

  private ValidMealCategoryValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidMealCategoryValidator();
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
  @DisplayName("Should accept supported meal category enum instances")
  void shouldAcceptSupportedMealCategoryEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedMealCategory.BREAKFAST, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.LUNCH, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.DINNER, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.SNACK, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.DESSERT, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.APPETIZER, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.BEVERAGE, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported meal category codes case-insensitively")
  void shouldAcceptSupportedMealCategoryCodes() {
    // When/Then
    assertThat(validator.isValid("BREAKFAST", null)).isTrue();
    assertThat(validator.isValid("breakfast", null)).isTrue();
    assertThat(validator.isValid("Breakfast", null)).isTrue();
    assertThat(validator.isValid("LUNCH", null)).isTrue();
    assertThat(validator.isValid("lunch", null)).isTrue();
    assertThat(validator.isValid("DINNER", null)).isTrue();
    assertThat(validator.isValid("dinner", null)).isTrue();
    assertThat(validator.isValid("SNACK", null)).isTrue();
    assertThat(validator.isValid("snack", null)).isTrue();
    assertThat(validator.isValid("DESSERT", null)).isTrue();
    assertThat(validator.isValid("dessert", null)).isTrue();
    assertThat(validator.isValid("APPETIZER", null)).isTrue();
    assertThat(validator.isValid("appetizer", null)).isTrue();
    assertThat(validator.isValid("BEVERAGE", null)).isTrue();
    assertThat(validator.isValid("beverage", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim meal category codes before validation")
  void shouldTrimMealCategoryCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  BREAKFAST  ", null)).isTrue();
    assertThat(validator.isValid("\tLUNCH\n", null)).isTrue();
    assertThat(validator.isValid("  DINNER  ", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid meal category codes")
  void shouldRejectUnknownMealCategoryCodes() {
    // When/Then
    assertThat(validator.isValid("INVALID", null)).isFalse();
    assertThat(validator.isValid("MEAL", null)).isFalse();
    assertThat(validator.isValid("FOOD", null)).isFalse();
    assertThat(validator.isValid("BRUNCH", null)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-SupportedMealCategory types")
  void shouldRejectNonStringAndNonEnumTypes() {
    // When/Then
    assertThat(validator.isValid(123, null)).isFalse();
    assertThat(validator.isValid(true, null)).isFalse();
    assertThat(validator.isValid(new Object(), null)).isFalse();
  }
}
