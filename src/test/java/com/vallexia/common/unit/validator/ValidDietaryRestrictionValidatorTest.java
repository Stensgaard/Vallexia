package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.validator.ValidDietaryRestrictionValidator;
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
 * Unit tests for ValidDietaryRestrictionValidator.
 * Tests dietary restriction validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidDietaryRestrictionValidator Unit Tests")
class ValidDietaryRestrictionValidatorTest {

  private ValidDietaryRestrictionValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidDietaryRestrictionValidator();
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
  @DisplayName("Should accept supported dietary restriction enum instances")
  void shouldAcceptSupportedDietaryRestrictionEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedDietaryRestriction.VEGETARIAN, null)).isTrue();
    assertThat(validator.isValid(SupportedDietaryRestriction.VEGAN, null)).isTrue();
    assertThat(validator.isValid(SupportedDietaryRestriction.GLUTEN_FREE, null)).isTrue();
    assertThat(validator.isValid(SupportedDietaryRestriction.KETOGENIC, null)).isTrue();
    assertThat(validator.isValid(SupportedDietaryRestriction.PALEO, null)).isTrue();
    assertThat(validator.isValid(SupportedDietaryRestriction.PALEO, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported dietary restriction codes case-insensitively")
  void shouldAcceptSupportedDietaryRestrictionCodes() {
    // When/Then
    assertThat(validator.isValid("VEGETARIAN", null)).isTrue();
    assertThat(validator.isValid("vegetarian", null)).isTrue();
    assertThat(validator.isValid("Vegetarian", null)).isTrue();
    assertThat(validator.isValid("VEGAN", null)).isTrue();
    assertThat(validator.isValid("vegan", null)).isTrue();
    assertThat(validator.isValid("GLUTEN_FREE", null)).isTrue();
    assertThat(validator.isValid("gluten_free", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim dietary restriction codes before validation")
  void shouldTrimDietaryRestrictionCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  VEGETARIAN  ", null)).isTrue();
    assertThat(validator.isValid("\tVEGAN\n", null)).isTrue();
    assertThat(validator.isValid("  KETOGENIC  ", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid dietary restriction codes")
  void shouldRejectUnknownDietaryRestrictionCodes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid("INVALID", context)).isFalse();
    assertThat(validator.isValid("RESTRICTION", context)).isFalse();
    assertThat(validator.isValid("VEGET", context)).isFalse();
    assertThat(validator.isValid("FREE", context)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-SupportedDietaryRestriction types")
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
  @DisplayName("Should accept collections of valid dietary restriction enum instances")
  void shouldAcceptCollectionsOfValidDietaryRestrictionEnumInstances() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(SupportedDietaryRestriction.VEGETARIAN, SupportedDietaryRestriction.VEGAN), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of(SupportedDietaryRestriction.GLUTEN_FREE, SupportedDietaryRestriction.KETOGENIC), null)).isTrue();
  }

  @Test
  @DisplayName("Should accept collections with valid dietary restriction string codes")
  void shouldAcceptCollectionsWithValidDietaryRestrictionStringCodes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("VEGETARIAN", "VEGAN"), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of("gluten_free", "ketogenic"), null)).isTrue();
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
    listWithNull.add(SupportedDietaryRestriction.VEGETARIAN);
    assertThat(validator.isValid(listWithNull, null)).isTrue();
  }

  @Test
  @DisplayName("Should reject collections with invalid dietary restriction codes")
  void shouldRejectCollectionsWithInvalidDietaryRestrictionCodes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("INVALID", "VEGETARIAN"), context)).isFalse();
    assertThat(validator.isValid(java.util.List.of("VEGAN", "INVALID"), context)).isFalse();
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
