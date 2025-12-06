package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedDietaryRestriction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedDietaryRestriction enum.
 * Tests dietary restriction lookup and validation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@DisplayName("SupportedDietaryRestriction Unit Tests")
class SupportedDietaryRestrictionTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all dietary restrictions")
  void shouldReturnAllDietaryRestrictions() {
    // When/Then
    assertThat(SupportedDietaryRestriction.getAll())
        .hasSize(SupportedDietaryRestriction.values().length)
        .containsExactlyInAnyOrder(SupportedDietaryRestriction.values());
  }

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve dietary restrictions case-insensitively")
  void shouldResolveDietaryRestrictionsCaseInsensitively() {
    // When/Then
    assertThat(SupportedDietaryRestriction.fromCode("vegetarian"))
        .contains(SupportedDietaryRestriction.VEGETARIAN);
    assertThat(SupportedDietaryRestriction.fromCode("VEGETARIAN"))
        .contains(SupportedDietaryRestriction.VEGETARIAN);
    assertThat(SupportedDietaryRestriction.fromCode("Vegetarian"))
        .contains(SupportedDietaryRestriction.VEGETARIAN);
    assertThat(SupportedDietaryRestriction.fromCode("vegan"))
        .contains(SupportedDietaryRestriction.VEGAN);
    assertThat(SupportedDietaryRestriction.fromCode("GLUTEN_FREE"))
        .contains(SupportedDietaryRestriction.GLUTEN_FREE);
  }

  @Test
  @DisplayName("Should resolve dietary restrictions with whitespace trimming")
  void shouldResolveDietaryRestrictionsWithWhitespaceTrimming() {
    // When/Then
    assertThat(SupportedDietaryRestriction.fromCode(" vegetarian "))
        .contains(SupportedDietaryRestriction.VEGETARIAN);
    assertThat(SupportedDietaryRestriction.fromCode("  VEGAN  "))
        .contains(SupportedDietaryRestriction.VEGAN);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank codes")
  void shouldReturnEmptyForInvalidCodes() {
    // When/Then
    assertThat(SupportedDietaryRestriction.fromCode("unknown")).isEmpty();
    assertThat(SupportedDietaryRestriction.fromCode("VEGET")).isEmpty();
    assertThat(SupportedDietaryRestriction.fromCode("")).isEmpty();
    assertThat(SupportedDietaryRestriction.fromCode("   ")).isEmpty();
    assertThat(SupportedDietaryRestriction.fromCode(null)).isEmpty();
  }

  // ==================== Display Name Tests ====================

  @Test
  @DisplayName("Should have non-blank display names for all dietary restrictions")
  void shouldHaveNonBlankDisplayNamesForAllDietaryRestrictions() {
    // Given/When/Then
    for (SupportedDietaryRestriction restriction : SupportedDietaryRestriction.values()) {
      assertThat(restriction.getDisplayName())
          .as(restriction.name() + " display name")
          .isNotBlank();
    }
  }
}
