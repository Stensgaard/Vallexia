package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedVolumeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedVolumeUnit enum.
 * Tests volume unit enumeration and conversion factors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedVolumeUnit Unit Tests")
class SupportedVolumeUnitTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all volume units")
  void shouldReturnAllVolumeUnits() {
    // When/Then
    assertThat(SupportedVolumeUnit.getAll())
        .hasSize(SupportedVolumeUnit.values().length)
        .containsExactlyInAnyOrder(SupportedVolumeUnit.values());
  }

  // ==================== Conversion Factor Tests ====================

  @Test
  @DisplayName("Should have positive conversion factors for all units")
  void shouldHavePositiveConversionFactorsForAllUnits() {
    // Given/When/Then
    for (SupportedVolumeUnit unit : SupportedVolumeUnit.values()) {
      assertThat(unit.getMilliliters())
          .as(unit.name() + " conversion factor")
          .isPositive();
    }
  }

  @Test
  @DisplayName("Should have non-zero conversion factors for all units")
  void shouldHaveNonZeroConversionFactorsForAllUnits() {
    // Given/When/Then
    for (SupportedVolumeUnit unit : SupportedVolumeUnit.values()) {
      assertThat(unit.getMilliliters())
          .as(unit.name() + " conversion factor")
          .isNotEqualByComparingTo(BigDecimal.ZERO);
    }
  }

  @Test
  @DisplayName("Base unit MILLILITER should equal 1")
  void shouldHaveBaseUnitEqualToOne() {
    // When/Then
    assertThat(SupportedVolumeUnit.MILLILITER.getMilliliters())
        .isEqualByComparingTo(BigDecimal.ONE);
  }

  // ==================== Display Value Tests ====================

  @Test
  @DisplayName("Should have non-blank display values for all units")
  void shouldHaveNonBlankDisplayValuesForAllUnits() {
    // Given/When/Then
    for (SupportedVolumeUnit unit : SupportedVolumeUnit.values()) {
      assertThat(unit.getDisplay())
          .as(unit.name() + " display value")
          .isNotBlank();
    }
  }
}
