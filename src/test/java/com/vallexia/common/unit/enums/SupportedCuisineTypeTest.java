package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedCuisineType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedCuisineType enum.
 * Tests cuisine type lookup and validation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@DisplayName("SupportedCuisineType Unit Tests")
class SupportedCuisineTypeTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all cuisine types")
  void shouldReturnAllCuisineTypes() {
    // When/Then
    assertThat(SupportedCuisineType.getAll())
        .hasSize(SupportedCuisineType.values().length)
        .containsExactlyInAnyOrder(SupportedCuisineType.values());
  }

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve cuisine types case-insensitively")
  void shouldResolveCuisineTypesCaseInsensitively() {
    // When/Then
    assertThat(SupportedCuisineType.fromCode("italian"))
        .contains(SupportedCuisineType.ITALIAN);
    assertThat(SupportedCuisineType.fromCode("ITALIAN"))
        .contains(SupportedCuisineType.ITALIAN);
    assertThat(SupportedCuisineType.fromCode("Italian"))
        .contains(SupportedCuisineType.ITALIAN);
    assertThat(SupportedCuisineType.fromCode("mexican"))
        .contains(SupportedCuisineType.MEXICAN);
    assertThat(SupportedCuisineType.fromCode("MIDDLE_EASTERN"))
        .contains(SupportedCuisineType.MIDDLE_EASTERN);
  }

  @Test
  @DisplayName("Should resolve cuisine types with whitespace trimming")
  void shouldResolveCuisineTypesWithWhitespaceTrimming() {
    // When/Then
    assertThat(SupportedCuisineType.fromCode(" italian "))
        .contains(SupportedCuisineType.ITALIAN);
    assertThat(SupportedCuisineType.fromCode("  MEXICAN  "))
        .contains(SupportedCuisineType.MEXICAN);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank codes")
  void shouldReturnEmptyForInvalidCodes() {
    // When/Then
    assertThat(SupportedCuisineType.fromCode("unknown")).isEmpty();
    assertThat(SupportedCuisineType.fromCode("ITAL")).isEmpty();
    assertThat(SupportedCuisineType.fromCode("")).isEmpty();
    assertThat(SupportedCuisineType.fromCode("   ")).isEmpty();
    assertThat(SupportedCuisineType.fromCode(null)).isEmpty();
  }

  // ==================== Display Name Tests ====================

  @Test
  @DisplayName("Should have non-blank display names for all cuisine types")
  void shouldHaveNonBlankDisplayNamesForAllCuisineTypes() {
    // Given/When/Then
    for (SupportedCuisineType cuisineType : SupportedCuisineType.values()) {
      assertThat(cuisineType.getDisplayName())
          .as(cuisineType.name() + " display name")
          .isNotBlank();
    }
  }
}
