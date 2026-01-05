package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.validator.ValidMealCategoryValidator;
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
 * Unit tests for ValidMealCategoryValidator.
 * Tests meal category validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidMealCategoryValidator Unit Tests")
class ValidMealCategoryValidatorTest {

  private ValidMealCategoryValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidMealCategoryValidator();
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
  @DisplayName("Should accept supported meal category enum instances")
  void shouldAcceptSupportedMealCategoryEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedMealCategory.BREAKFAST, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.LUNCH, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.DINNER, null)).isTrue();
    assertThat(validator.isValid(SupportedMealCategory.SNACK, null)).isTrue();
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
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid("INVALID", context)).isFalse();
    assertThat(validator.isValid("MEAL", context)).isFalse();
    assertThat(validator.isValid("FOOD", context)).isFalse();
    assertThat(validator.isValid("BRUNCH", context)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-SupportedMealCategory types")
  void shouldRejectNonStringAndNonEnumTypes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid(123, context)).isFalse();
    assertThat(validator.isValid(true, context)).isFalse();
    assertThat(validator.isValid(new Object(), context)).isFalse();
  }

  // ==================== Collection Tests ====================

  @Test
  @DisplayName("Should accept collections of valid meal category enum instances")
  void shouldAcceptCollectionsOfValidMealCategoryEnumInstances() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(SupportedMealCategory.BREAKFAST, SupportedMealCategory.LUNCH), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of(SupportedMealCategory.DINNER, SupportedMealCategory.SNACK), null)).isTrue();
  }

  @Test
  @DisplayName("Should accept collections with valid meal category string codes")
  void shouldAcceptCollectionsWithValidMealCategoryStringCodes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("BREAKFAST", "LUNCH"), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of("dinner", "snack"), null)).isTrue();
  }

  @Test
  @DisplayName("Should accept empty collections")
  void shouldAcceptEmptyCollections() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of(), null)).isTrue();
  }

  @Test
  @DisplayName("Should accept collections with null elements")
  void shouldAcceptCollectionsWithNullElements() {
    // When/Then
    java.util.List<Object> listWithNull = new java.util.ArrayList<>();
    listWithNull.add(null);
    listWithNull.add(SupportedMealCategory.BREAKFAST);
    assertThat(validator.isValid(listWithNull, null)).isTrue();
  }

  @Test
  @DisplayName("Should reject collections with invalid meal category codes")
  void shouldRejectCollectionsWithInvalidMealCategoryCodes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("INVALID", "BREAKFAST"), context)).isFalse();
    assertThat(validator.isValid(java.util.List.of("LUNCH", "INVALID"), context)).isFalse();
  }

  @Test
  @DisplayName("Should reject collections with invalid types")
  void shouldRejectCollectionsWithInvalidTypes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(123, 456), context)).isFalse();
    assertThat(validator.isValid(java.util.List.of(true, false), context)).isFalse();
  }
}
