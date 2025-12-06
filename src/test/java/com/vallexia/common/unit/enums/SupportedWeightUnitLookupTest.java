package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedWeightUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedWeightUnit enum lookup methods.
 * Tests fromDisplay lookup functionality and unit classification methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedWeightUnitLookup Unit Tests")
class SupportedWeightUnitLookupTest {

  // ==================== fromDisplay() Tests ====================

  @Test
  @DisplayName("Should find unit by exact display string")
  void shouldFindByExactDisplay() {
    // When/Then
    assertThat(SupportedWeightUnit.fromDisplay("g"))
        .contains(SupportedWeightUnit.GRAM);
    assertThat(SupportedWeightUnit.fromDisplay("kg"))
        .contains(SupportedWeightUnit.KILOGRAM);
    assertThat(SupportedWeightUnit.fromDisplay("oz"))
        .contains(SupportedWeightUnit.OUNCE);
    assertThat(SupportedWeightUnit.fromDisplay("lb"))
        .contains(SupportedWeightUnit.POUND);
  }

  @Test
  @DisplayName("Should be case-insensitive")
  void shouldBeCaseInsensitive() {
    // When/Then
    assertThat(SupportedWeightUnit.fromDisplay("G"))
        .contains(SupportedWeightUnit.GRAM);
    assertThat(SupportedWeightUnit.fromDisplay("KG"))
        .contains(SupportedWeightUnit.KILOGRAM);
    assertThat(SupportedWeightUnit.fromDisplay("Oz"))
        .contains(SupportedWeightUnit.OUNCE);
  }

  @Test
  @DisplayName("Should handle plurals and variations")
  void shouldHandlePlurals() {
    // When/Then
    assertThat(SupportedWeightUnit.fromDisplay("gram"))
        .contains(SupportedWeightUnit.GRAM);
    assertThat(SupportedWeightUnit.fromDisplay("grams"))
        .contains(SupportedWeightUnit.GRAM);
    assertThat(SupportedWeightUnit.fromDisplay("kilogram"))
        .contains(SupportedWeightUnit.KILOGRAM);
    assertThat(SupportedWeightUnit.fromDisplay("kilograms"))
        .contains(SupportedWeightUnit.KILOGRAM);
    assertThat(SupportedWeightUnit.fromDisplay("ounce"))
        .contains(SupportedWeightUnit.OUNCE);
    assertThat(SupportedWeightUnit.fromDisplay("ounces"))
        .contains(SupportedWeightUnit.OUNCE);
    assertThat(SupportedWeightUnit.fromDisplay("pound"))
        .contains(SupportedWeightUnit.POUND);
    assertThat(SupportedWeightUnit.fromDisplay("pounds"))
        .contains(SupportedWeightUnit.POUND);
    assertThat(SupportedWeightUnit.fromDisplay("lbs"))
        .contains(SupportedWeightUnit.POUND);
  }

  @Test
  @DisplayName("Should handle whitespace")
  void shouldHandleWhitespace() {
    // When/Then
    assertThat(SupportedWeightUnit.fromDisplay("  g  "))
        .contains(SupportedWeightUnit.GRAM);
    assertThat(SupportedWeightUnit.fromDisplay(" kg "))
        .contains(SupportedWeightUnit.KILOGRAM);
  }

  @Test
  @DisplayName("Should return empty for null or blank strings")
  void shouldReturnEmptyForInvalidInput() {
    // When/Then
    assertThat(SupportedWeightUnit.fromDisplay(null)).isEmpty();
    assertThat(SupportedWeightUnit.fromDisplay("")).isEmpty();
    assertThat(SupportedWeightUnit.fromDisplay("   ")).isEmpty();
    assertThat(SupportedWeightUnit.fromDisplay("unknown")).isEmpty();
  }

  // ==================== isMetric() Tests ====================

  @Test
  @DisplayName("Should correctly identify metric units")
  void shouldIdentifyMetricUnits() {
    // When/Then
    assertThat(SupportedWeightUnit.GRAM.isMetric()).isTrue();
    assertThat(SupportedWeightUnit.KILOGRAM.isMetric()).isTrue();
    assertThat(SupportedWeightUnit.MILLIGRAM.isMetric()).isTrue();
    assertThat(SupportedWeightUnit.OUNCE.isMetric()).isFalse();
    assertThat(SupportedWeightUnit.POUND.isMetric()).isFalse();
  }

  // ==================== isImperial() Tests ====================

  @Test
  @DisplayName("Should correctly identify imperial units")
  void shouldIdentifyImperialUnits() {
    // When/Then
    assertThat(SupportedWeightUnit.OUNCE.isImperial()).isTrue();
    assertThat(SupportedWeightUnit.POUND.isImperial()).isTrue();
    assertThat(SupportedWeightUnit.GRAM.isImperial()).isFalse();
    assertThat(SupportedWeightUnit.KILOGRAM.isImperial()).isFalse();
    assertThat(SupportedWeightUnit.MILLIGRAM.isImperial()).isFalse();
  }
}
