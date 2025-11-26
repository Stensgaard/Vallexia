package com.vallexia.common.unit.validator;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.validator.ValidAllergyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidAllergyValidator.
 * Tests allergy validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@DisplayName("ValidAllergyValidator Unit Tests")
class ValidAllergyValidatorTest {

  private ValidAllergyValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidAllergyValidator();
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
  @DisplayName("Should accept supported allergy enum instances")
  void shouldAcceptSupportedAllergyEnumInstances() {
    // When/Then
    assertThat(validator.isValid(SupportedAllergy.PEANUTS, null)).isTrue();
    assertThat(validator.isValid(SupportedAllergy.MILK, null)).isTrue();
    assertThat(validator.isValid(SupportedAllergy.EGGS, null)).isTrue();
    assertThat(validator.isValid(SupportedAllergy.TREE_NUTS, null)).isTrue();
    assertThat(validator.isValid(SupportedAllergy.SOY, null)).isTrue();
    assertThat(validator.isValid(SupportedAllergy.WHEAT, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported allergy codes case-insensitively")
  void shouldAcceptSupportedAllergyCodes() {
    // When/Then
    assertThat(validator.isValid("PEANUTS", null)).isTrue();
    assertThat(validator.isValid("peanuts", null)).isTrue();
    assertThat(validator.isValid("Peanuts", null)).isTrue();
    assertThat(validator.isValid("MILK", null)).isTrue();
    assertThat(validator.isValid("milk", null)).isTrue();
    assertThat(validator.isValid("TREE_NUTS", null)).isTrue();
    assertThat(validator.isValid("tree_nuts", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim allergy codes before validation")
  void shouldTrimAllergyCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  PEANUTS  ", null)).isTrue();
    assertThat(validator.isValid("\tMILK\n", null)).isTrue();
    assertThat(validator.isValid("  EGGS  ", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid allergy codes")
  void shouldRejectUnknownAllergyCodes() {
    // When/Then
    assertThat(validator.isValid("INVALID", null)).isFalse();
    assertThat(validator.isValid("ALLERGY", null)).isFalse();
    assertThat(validator.isValid("PEANUT", null)).isFalse();
    assertThat(validator.isValid("NUTS", null)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-SupportedAllergy types")
  void shouldRejectNonStringAndNonEnumTypes() {
    // When/Then
    assertThat(validator.isValid(123, null)).isFalse();
    assertThat(validator.isValid(true, null)).isFalse();
    assertThat(validator.isValid(new Object(), null)).isFalse();
  }

  // ==================== Collection Tests ====================

  @Test
  @DisplayName("Should accept collections of valid allergy enum instances")
  void shouldAcceptCollectionsOfValidAllergyEnumInstances() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(SupportedAllergy.PEANUTS, SupportedAllergy.MILK), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of(SupportedAllergy.EGGS, SupportedAllergy.SOY), null)).isTrue();
  }

  @Test
  @DisplayName("Should accept collections with valid allergy string codes")
  void shouldAcceptCollectionsWithValidAllergyStringCodes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("PEANUTS", "MILK"), null)).isTrue();
    assertThat(validator.isValid(java.util.List.of("eggs", "soy"), null)).isTrue();
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
    listWithNull.add(SupportedAllergy.PEANUTS);
    assertThat(validator.isValid(listWithNull, null)).isTrue();
  }

  @Test
  @DisplayName("Should reject collections with invalid allergy codes")
  void shouldRejectCollectionsWithInvalidAllergyCodes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of("INVALID", "PEANUTS"), null)).isFalse();
    assertThat(validator.isValid(java.util.List.of("PEANUTS", "INVALID"), null)).isFalse();
  }

  @Test
  @DisplayName("Should reject collections with invalid types")
  void shouldRejectCollectionsWithInvalidTypes() {
    // When/Then
    assertThat(validator.isValid(java.util.Set.of(123, 456), null)).isFalse();
    assertThat(validator.isValid(java.util.List.of(true, false), null)).isFalse();
  }
}
