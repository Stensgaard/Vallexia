package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedFirstDayOfWeek enum.
 * Tests first day of week lookup functionality.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedFirstDayOfWeek Unit Tests")
class SupportedFirstDayOfWeekTest {

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve known codes regardless of case")
  void shouldResolveKnownCodesCaseInsensitive() {
    // When/Then
    assertThat(SupportedFirstDayOfWeek.fromCode("monday"))
        .contains(SupportedFirstDayOfWeek.MONDAY);
    assertThat(SupportedFirstDayOfWeek.fromCode("SUNDAY"))
        .contains(SupportedFirstDayOfWeek.SUNDAY);
  }

  @Test
  @DisplayName("Should trim incoming values")
  void shouldTrimValuesBeforeLookup() {
    // When/Then
    assertThat(SupportedFirstDayOfWeek.fromCode("  sunday  "))
        .contains(SupportedFirstDayOfWeek.SUNDAY);
  }

  @Test
  @DisplayName("Should ignore default locale casing rules")
  void shouldNotDependOnDefaultLocale() {
    // Given
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    
    try {
      // When/Then
      assertThat(SupportedFirstDayOfWeek.fromCode("monday"))
          .contains(SupportedFirstDayOfWeek.MONDAY);
    } finally {
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  @DisplayName("Should return empty for null, blank, or unknown values")
  void shouldReturnEmptyForInvalidInputs() {
    // When/Then
    assertThat(SupportedFirstDayOfWeek.fromCode(null)).isEmpty();
    assertThat(SupportedFirstDayOfWeek.fromCode("   ")).isEmpty();
    assertThat(SupportedFirstDayOfWeek.fromCode("tuesday")).isEmpty();
  }
}
