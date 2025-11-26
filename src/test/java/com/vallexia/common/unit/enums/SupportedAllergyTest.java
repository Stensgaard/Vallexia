package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedAllergy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedAllergy enum.
 * Tests allergy lookup and validation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@DisplayName("SupportedAllergy Unit Tests")
class SupportedAllergyTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all allergies")
  void shouldReturnAllAllergies() {
    // When/Then
    assertThat(SupportedAllergy.getAll())
        .hasSize(SupportedAllergy.values().length)
        .containsExactlyInAnyOrder(SupportedAllergy.values());
  }

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve allergies case-insensitively")
  void shouldResolveAllergiesCaseInsensitively() {
    // When/Then
    assertThat(SupportedAllergy.fromCode("peanuts"))
        .contains(SupportedAllergy.PEANUTS);
    assertThat(SupportedAllergy.fromCode("PEANUTS"))
        .contains(SupportedAllergy.PEANUTS);
    assertThat(SupportedAllergy.fromCode("Peanuts"))
        .contains(SupportedAllergy.PEANUTS);
    assertThat(SupportedAllergy.fromCode("milk"))
        .contains(SupportedAllergy.MILK);
    assertThat(SupportedAllergy.fromCode("TREE_NUTS"))
        .contains(SupportedAllergy.TREE_NUTS);
  }

  @Test
  @DisplayName("Should resolve allergies with whitespace trimming")
  void shouldResolveAllergiesWithWhitespaceTrimming() {
    // When/Then
    assertThat(SupportedAllergy.fromCode(" peanuts "))
        .contains(SupportedAllergy.PEANUTS);
    assertThat(SupportedAllergy.fromCode("  MILK  "))
        .contains(SupportedAllergy.MILK);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank codes")
  void shouldReturnEmptyForInvalidCodes() {
    // When/Then
    assertThat(SupportedAllergy.fromCode("unknown")).isEmpty();
    assertThat(SupportedAllergy.fromCode("PEANUT")).isEmpty();
    assertThat(SupportedAllergy.fromCode("")).isEmpty();
    assertThat(SupportedAllergy.fromCode("   ")).isEmpty();
    assertThat(SupportedAllergy.fromCode(null)).isEmpty();
  }

  // ==================== Display Name Tests ====================

  @Test
  @DisplayName("Should have non-blank display names for all allergies")
  void shouldHaveNonBlankDisplayNamesForAllAllergies() {
    // Given/When/Then
    for (SupportedAllergy allergy : SupportedAllergy.values()) {
      assertThat(allergy.getDisplayName())
          .as(allergy.name() + " display name")
          .isNotBlank();
    }
  }
}
