package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedVolumeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedVolumeUnit enum lookup methods.
 * Tests fromDisplay lookup functionality.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedVolumeUnitLookup Unit Tests")
class SupportedVolumeUnitLookupTest {

  // ==================== fromDisplay() Tests ====================

  @Test
  @DisplayName("Should find unit by exact display string")
  void shouldFindByExactDisplay() {
    // When/Then
    assertThat(SupportedVolumeUnit.fromDisplay("cup"))
        .contains(SupportedVolumeUnit.CUP);
    assertThat(SupportedVolumeUnit.fromDisplay("tbsp"))
        .contains(SupportedVolumeUnit.TABLESPOON);
    assertThat(SupportedVolumeUnit.fromDisplay("tsp"))
        .contains(SupportedVolumeUnit.TEASPOON);
    assertThat(SupportedVolumeUnit.fromDisplay("ml"))
        .contains(SupportedVolumeUnit.MILLILITER);
    assertThat(SupportedVolumeUnit.fromDisplay("l"))
        .contains(SupportedVolumeUnit.LITER);
    assertThat(SupportedVolumeUnit.fromDisplay("fl oz"))
        .contains(SupportedVolumeUnit.FLUID_OUNCE);
  }

  @Test
  @DisplayName("Should be case-insensitive")
  void shouldBeCaseInsensitive() {
    // When/Then
    assertThat(SupportedVolumeUnit.fromDisplay("CUP"))
        .contains(SupportedVolumeUnit.CUP);
    assertThat(SupportedVolumeUnit.fromDisplay("ML"))
        .contains(SupportedVolumeUnit.MILLILITER);
    assertThat(SupportedVolumeUnit.fromDisplay("Fl Oz"))
        .contains(SupportedVolumeUnit.FLUID_OUNCE);
  }

  @Test
  @DisplayName("Should handle plurals and variations")
  void shouldHandlePlurals() {
    // When/Then
    assertThat(SupportedVolumeUnit.fromDisplay("cups"))
        .contains(SupportedVolumeUnit.CUP);
    assertThat(SupportedVolumeUnit.fromDisplay("tablespoon"))
        .contains(SupportedVolumeUnit.TABLESPOON);
    assertThat(SupportedVolumeUnit.fromDisplay("tablespoons"))
        .contains(SupportedVolumeUnit.TABLESPOON);
    assertThat(SupportedVolumeUnit.fromDisplay("teaspoon"))
        .contains(SupportedVolumeUnit.TEASPOON);
    assertThat(SupportedVolumeUnit.fromDisplay("teaspoons"))
        .contains(SupportedVolumeUnit.TEASPOON);
    assertThat(SupportedVolumeUnit.fromDisplay("milliliter"))
        .contains(SupportedVolumeUnit.MILLILITER);
    assertThat(SupportedVolumeUnit.fromDisplay("milliliters"))
        .contains(SupportedVolumeUnit.MILLILITER);
    assertThat(SupportedVolumeUnit.fromDisplay("liter"))
        .contains(SupportedVolumeUnit.LITER);
    assertThat(SupportedVolumeUnit.fromDisplay("liters"))
        .contains(SupportedVolumeUnit.LITER);
    assertThat(SupportedVolumeUnit.fromDisplay("fluid ounce"))
        .contains(SupportedVolumeUnit.FLUID_OUNCE);
    assertThat(SupportedVolumeUnit.fromDisplay("fluid ounces"))
        .contains(SupportedVolumeUnit.FLUID_OUNCE);
  }

  @Test
  @DisplayName("Should handle whitespace")
  void shouldHandleWhitespace() {
    // When/Then
    assertThat(SupportedVolumeUnit.fromDisplay("  cup  "))
        .contains(SupportedVolumeUnit.CUP);
    assertThat(SupportedVolumeUnit.fromDisplay(" ml "))
        .contains(SupportedVolumeUnit.MILLILITER);
  }

  @Test
  @DisplayName("Should return empty for null or blank strings")
  void shouldReturnEmptyForInvalidInput() {
    // When/Then
    assertThat(SupportedVolumeUnit.fromDisplay(null)).isEmpty();
    assertThat(SupportedVolumeUnit.fromDisplay("")).isEmpty();
    assertThat(SupportedVolumeUnit.fromDisplay("   ")).isEmpty();
    assertThat(SupportedVolumeUnit.fromDisplay("unknown")).isEmpty();
  }
}
