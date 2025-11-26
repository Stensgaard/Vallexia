package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedCountUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedCountUnit enum.
 * Tests count unit enumeration and lookup functionality.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedCountUnit Unit Tests")
class SupportedCountUnitTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all count units")
  void shouldReturnAllCountUnits() {
    // When/Then
    assertThat(SupportedCountUnit.getAll())
        .hasSize(SupportedCountUnit.values().length)
        .containsExactlyInAnyOrder(SupportedCountUnit.values());
  }

  // ==================== fromDisplay() Tests ====================

  @Test
  @DisplayName("Should resolve count units by exact display match (case-insensitive)")
  void shouldResolveCountUnitsByExactDisplayMatch() {
    // When/Then
    assertThat(SupportedCountUnit.fromDisplay("piece"))
        .contains(SupportedCountUnit.PIECE);
    assertThat(SupportedCountUnit.fromDisplay("PIECE"))
        .contains(SupportedCountUnit.PIECE);
    assertThat(SupportedCountUnit.fromDisplay("Piece"))
        .contains(SupportedCountUnit.PIECE);
    assertThat(SupportedCountUnit.fromDisplay("item"))
        .contains(SupportedCountUnit.ITEM);
    assertThat(SupportedCountUnit.fromDisplay("whole"))
        .contains(SupportedCountUnit.WHOLE);
  }

  @Test
  @DisplayName("Should resolve count units with whitespace trimming")
  void shouldResolveCountUnitsWithWhitespaceTrimming() {
    // When/Then
    assertThat(SupportedCountUnit.fromDisplay(" piece "))
        .contains(SupportedCountUnit.PIECE);
    assertThat(SupportedCountUnit.fromDisplay("  item  "))
        .contains(SupportedCountUnit.ITEM);
    assertThat(SupportedCountUnit.fromDisplay("\twhole\n"))
        .contains(SupportedCountUnit.WHOLE);
  }

  @Test
  @DisplayName("Should resolve PIECE by plural and abbreviation variations")
  void shouldResolvePieceByPluralAndAbbreviationVariations() {
    // When/Then
    assertThat(SupportedCountUnit.fromDisplay("pieces"))
        .contains(SupportedCountUnit.PIECE);
    assertThat(SupportedCountUnit.fromDisplay("pcs"))
        .contains(SupportedCountUnit.PIECE);
    assertThat(SupportedCountUnit.fromDisplay("pc"))
        .contains(SupportedCountUnit.PIECE);
    assertThat(SupportedCountUnit.fromDisplay("PCS"))
        .contains(SupportedCountUnit.PIECE);
  }

  @Test
  @DisplayName("Should resolve ITEM by plural variation")
  void shouldResolveItemByPluralVariation() {
    // When/Then
    assertThat(SupportedCountUnit.fromDisplay("items"))
        .contains(SupportedCountUnit.ITEM);
    assertThat(SupportedCountUnit.fromDisplay("ITEMS"))
        .contains(SupportedCountUnit.ITEM);
  }

  @Test
  @DisplayName("Should resolve WHOLE by plural variation")
  void shouldResolveWholeByPluralVariation() {
    // When/Then
    assertThat(SupportedCountUnit.fromDisplay("wholes"))
        .contains(SupportedCountUnit.WHOLE);
    assertThat(SupportedCountUnit.fromDisplay("WHOLES"))
        .contains(SupportedCountUnit.WHOLE);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank displays")
  void shouldReturnEmptyForInvalidDisplays() {
    // When/Then
    assertThat(SupportedCountUnit.fromDisplay("unknown")).isEmpty();
    assertThat(SupportedCountUnit.fromDisplay("")).isEmpty();
    assertThat(SupportedCountUnit.fromDisplay("   ")).isEmpty();
    assertThat(SupportedCountUnit.fromDisplay(null)).isEmpty();
  }

  // ==================== Display Value Tests ====================

  @Test
  @DisplayName("Should have non-blank display values for all units")
  void shouldHaveNonBlankDisplayValuesForAllUnits() {
    // Given/When/Then
    for (SupportedCountUnit unit : SupportedCountUnit.values()) {
      assertThat(unit.getDisplay())
          .as(unit.name() + " display value")
          .isNotBlank();
    }
  }
}
