package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedMeasurementSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedMeasurementSystem enum.
 * Tests measurement system lookup and validation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedMeasurementSystem Unit Tests")
class SupportedMeasurementSystemTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all measurement systems")
  void shouldReturnAllMeasurementSystems() {
    // When/Then
    assertThat(SupportedMeasurementSystem.getAll())
        .hasSize(SupportedMeasurementSystem.values().length)
        .containsExactlyInAnyOrder(SupportedMeasurementSystem.values());
  }

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve measurement systems case-insensitively")
  void shouldResolveMeasurementSystemsCaseInsensitively() {
    // When/Then
    assertThat(SupportedMeasurementSystem.fromCode("metric"))
        .contains(SupportedMeasurementSystem.METRIC);
    assertThat(SupportedMeasurementSystem.fromCode("METRIC"))
        .contains(SupportedMeasurementSystem.METRIC);
    assertThat(SupportedMeasurementSystem.fromCode("Metric"))
        .contains(SupportedMeasurementSystem.METRIC);
    assertThat(SupportedMeasurementSystem.fromCode("imperial"))
        .contains(SupportedMeasurementSystem.IMPERIAL);
    assertThat(SupportedMeasurementSystem.fromCode("IMPERIAL"))
        .contains(SupportedMeasurementSystem.IMPERIAL);
  }

  @Test
  @DisplayName("Should resolve measurement systems with whitespace trimming")
  void shouldResolveMeasurementSystemsWithWhitespaceTrimming() {
    // When/Then
    assertThat(SupportedMeasurementSystem.fromCode(" metric "))
        .contains(SupportedMeasurementSystem.METRIC);
    assertThat(SupportedMeasurementSystem.fromCode("  IMPERIAL  "))
        .contains(SupportedMeasurementSystem.IMPERIAL);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank codes")
  void shouldReturnEmptyForInvalidCodes() {
    // When/Then
    assertThat(SupportedMeasurementSystem.fromCode("unknown")).isEmpty();
    assertThat(SupportedMeasurementSystem.fromCode("SI")).isEmpty();
    assertThat(SupportedMeasurementSystem.fromCode("")).isEmpty();
    assertThat(SupportedMeasurementSystem.fromCode("   ")).isEmpty();
    assertThat(SupportedMeasurementSystem.fromCode(null)).isEmpty();
  }

  // ==================== Display Name Tests ====================

  @Test
  @DisplayName("Should have non-blank display names for all systems")
  void shouldHaveNonBlankDisplayNamesForAllSystems() {
    // Given/When/Then
    for (SupportedMeasurementSystem system : SupportedMeasurementSystem.values()) {
      assertThat(system.getDisplayName())
          .as(system.name() + " display name")
          .isNotBlank();
    }
  }
}
