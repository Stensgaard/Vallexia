package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedMealCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedMealCategory enum.
 * Tests meal category lookup and validation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedMealCategory Unit Tests")
class SupportedMealCategoryTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all meal categories")
  void shouldReturnAllMealCategories() {
    // When/Then
    assertThat(SupportedMealCategory.getAll())
        .hasSize(SupportedMealCategory.values().length)
        .containsExactlyInAnyOrder(SupportedMealCategory.values());
  }

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve meal categories case-insensitively")
  void shouldResolveMealCategoriesCaseInsensitively() {
    // When/Then
    assertThat(SupportedMealCategory.fromCode("breakfast"))
        .contains(SupportedMealCategory.BREAKFAST);
    assertThat(SupportedMealCategory.fromCode("BREAKFAST"))
        .contains(SupportedMealCategory.BREAKFAST);
    assertThat(SupportedMealCategory.fromCode("Breakfast"))
        .contains(SupportedMealCategory.BREAKFAST);
    assertThat(SupportedMealCategory.fromCode("lunch"))
        .contains(SupportedMealCategory.LUNCH);
    assertThat(SupportedMealCategory.fromCode("dinner"))
        .contains(SupportedMealCategory.DINNER);
  }

  @Test
  @DisplayName("Should resolve meal categories with whitespace trimming")
  void shouldResolveMealCategoriesWithWhitespaceTrimming() {
    // When/Then
    assertThat(SupportedMealCategory.fromCode(" breakfast "))
        .contains(SupportedMealCategory.BREAKFAST);
    assertThat(SupportedMealCategory.fromCode("  LUNCH  "))
        .contains(SupportedMealCategory.LUNCH);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank codes")
  void shouldReturnEmptyForInvalidCodes() {
    // When/Then
    assertThat(SupportedMealCategory.fromCode("unknown")).isEmpty();
    assertThat(SupportedMealCategory.fromCode("BREAK")).isEmpty();
    assertThat(SupportedMealCategory.fromCode("")).isEmpty();
    assertThat(SupportedMealCategory.fromCode("   ")).isEmpty();
    assertThat(SupportedMealCategory.fromCode(null)).isEmpty();
  }

  // ==================== Display Name Tests ====================

  @Test
  @DisplayName("Should have non-blank display names for all categories")
  void shouldHaveNonBlankDisplayNamesForAllCategories() {
    // Given/When/Then
    for (SupportedMealCategory category : SupportedMealCategory.values()) {
      assertThat(category.getDisplayName())
          .as(category.name() + " display name")
          .isNotBlank();
    }
  }
}
