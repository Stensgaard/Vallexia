package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedWeightUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedWeightUnit enum.
 * Tests weight unit enumeration and conversion factors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedWeightUnit Unit Tests")
class SupportedWeightUnitTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all weight units")
  void shouldReturnAllWeightUnits() {
    // When/Then
    assertThat(SupportedWeightUnit.getAll())
        .hasSize(SupportedWeightUnit.values().length)
        .containsExactlyInAnyOrder(SupportedWeightUnit.values());
  }

  // ==================== Conversion Factor Tests ====================

  @Test
  @DisplayName("Should have positive conversion factors for all units")
  void shouldHavePositiveConversionFactorsForAllUnits() {
    // Given/When/Then
    for (SupportedWeightUnit unit : SupportedWeightUnit.values()) {
      assertThat(unit.getGrams())
          .as(unit.name() + " conversion factor")
          .isPositive();
    }
  }

  @Test
  @DisplayName("Should have non-zero conversion factors for all units")
  void shouldHaveNonZeroConversionFactorsForAllUnits() {
    // Given/When/Then
    for (SupportedWeightUnit unit : SupportedWeightUnit.values()) {
      assertThat(unit.getGrams())
          .as(unit.name() + " conversion factor")
          .isNotEqualByComparingTo(BigDecimal.ZERO);
    }
  }

  @Test
  @DisplayName("Base unit GRAM should equal 1")
  void shouldHaveBaseUnitEqualToOne() {
    // When/Then
    assertThat(SupportedWeightUnit.GRAM.getGrams())
        .isEqualByComparingTo(BigDecimal.ONE);
  }

  // ==================== Display Value Tests ====================

  @Test
  @DisplayName("Should have non-blank display values for all units")
  void shouldHaveNonBlankDisplayValuesForAllUnits() {
    // Given/When/Then
    for (SupportedWeightUnit unit : SupportedWeightUnit.values()) {
      assertThat(unit.getDisplay())
          .as(unit.name() + " display value")
          .isNotBlank();
    }
  }
}
