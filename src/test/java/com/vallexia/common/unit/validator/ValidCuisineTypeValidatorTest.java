package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.validator.ValidCuisineTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidCuisineTypeValidator.
 * Tests cuisine type validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@DisplayName("ValidCuisineTypeValidator Unit Tests")
class ValidCuisineTypeValidatorTest {

  private ValidCuisineTypeValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidCuisineTypeValidator();
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
  @DisplayName("Should accept supported cuisine type enum instances")
  void shouldAcceptSupportedCuisineTypeEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedCuisineType.ITALIAN, null)).isTrue();
    assertThat(validator.isValid(SupportedCuisineType.MEXICAN, null)).isTrue();
    assertThat(validator.isValid(SupportedCuisineType.JAPANESE, null)).isTrue();
    assertThat(validator.isValid(SupportedCuisineType.THAI, null)).isTrue();
    assertThat(validator.isValid(SupportedCuisineType.INDIAN, null)).isTrue();
    assertThat(validator.isValid(SupportedCuisineType.MEDITERRANEAN, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported cuisine type codes case-insensitively")
  void shouldAcceptSupportedCuisineTypeCodes() {
    // When/Then
    assertThat(validator.isValid("ITALIAN", null)).isTrue();
    assertThat(validator.isValid("italian", null)).isTrue();
    assertThat(validator.isValid("Italian", null)).isTrue();
    assertThat(validator.isValid("MEXICAN", null)).isTrue();
    assertThat(validator.isValid("mexican", null)).isTrue();
    assertThat(validator.isValid("MIDDLE_EASTERN", null)).isTrue();
    assertThat(validator.isValid("middle_eastern", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim cuisine type codes before validation")
  void shouldTrimCuisineTypeCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  ITALIAN  ", null)).isTrue();
    assertThat(validator.isValid("\tMEXICAN\n", null)).isTrue();
    assertThat(validator.isValid("  JAPANESE  ", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid cuisine type codes")
  void shouldRejectUnknownCuisineTypeCodes() {
    // When/Then
    assertThat(validator.isValid("INVALID", null)).isFalse();
    assertThat(validator.isValid("CUISINE", null)).isFalse();
    assertThat(validator.isValid("ITAL", null)).isFalse();
    assertThat(validator.isValid("FOOD", null)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-SupportedCuisineType types")
  void shouldRejectNonStringAndNonEnumTypes() {
    // When/Then
    assertThat(validator.isValid(123, null)).isFalse();
    assertThat(validator.isValid(true, null)).isFalse();
    assertThat(validator.isValid(new Object(), null)).isFalse();
  }

  // ==================== Collection Tests ====================

  @Test
  @DisplayName("Should accept collections of valid cuisine type enum instances")
  void shouldAcceptCollectionsOfValidCuisineTypeEnumInstances() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(SupportedCuisineType.ITALIAN, SupportedCuisineType.MEXICAN), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of(SupportedCuisineType.JAPANESE, SupportedCuisineType.THAI), null)).isTrue();
  }

  @Test
  @DisplayName("Should accept collections with valid cuisine type string codes")
  void shouldAcceptCollectionsWithValidCuisineTypeStringCodes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("ITALIAN", "MEXICAN"), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of("japanese", "thai"), null)).isTrue();
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
    listWithNull.add(SupportedCuisineType.ITALIAN);
    assertThat(validator.isValid(listWithNull, null)).isTrue();
  }

  @Test
  @DisplayName("Should reject collections with invalid cuisine type codes")
  void shouldRejectCollectionsWithInvalidCuisineTypeCodes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("INVALID", "ITALIAN"), null)).isFalse();
    assertThat(validator.isValid(java.util.List.of("MEXICAN", "INVALID"), null)).isFalse();
  }

  @Test
  @DisplayName("Should reject collections with invalid types")
  void shouldRejectCollectionsWithInvalidTypes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(123, 456), null)).isFalse();
    assertThat(validator.isValid(java.util.List.of(true, false), null)).isFalse();
  }
}
