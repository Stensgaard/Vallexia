package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedTimezone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedTimezone enum.
 * Tests timezone lookup functionality and enumeration methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedTimezone Unit Tests")
class SupportedTimezoneTest {

  // ==================== fromValue() Tests ====================

  @Test
  @DisplayName("Should resolve known timezone values regardless of case")
  void shouldResolveKnownValuesCaseInsensitive() {
    // When/Then
    assertThat(SupportedTimezone.fromValue("america/new_york"))
        .contains(SupportedTimezone.AMERICA_NEW_YORK);
    assertThat(SupportedTimezone.fromValue("AMERICA/LOS_ANGELES"))
        .contains(SupportedTimezone.AMERICA_LOS_ANGELES);
    assertThat(SupportedTimezone.fromValue("utc"))
        .contains(SupportedTimezone.UTC);
    assertThat(SupportedTimezone.fromValue("Europe/Copenhagen"))
        .contains(SupportedTimezone.EUROPE_COPENHAGEN);
  }

  @Test
  @DisplayName("Should resolve exact case matches")
  void shouldResolveExactCaseMatches() {
    // When/Then
    assertThat(SupportedTimezone.fromValue("America/New_York"))
        .contains(SupportedTimezone.AMERICA_NEW_YORK);
    assertThat(SupportedTimezone.fromValue("UTC"))
        .contains(SupportedTimezone.UTC);
    assertThat(SupportedTimezone.fromValue("Europe/Copenhagen"))
        .contains(SupportedTimezone.EUROPE_COPENHAGEN);
  }

  @Test
  @DisplayName("Should trim incoming values")
  void shouldTrimValuesBeforeLookup() {
    // When/Then
    assertThat(SupportedTimezone.fromValue("  America/New_York  "))
        .contains(SupportedTimezone.AMERICA_NEW_YORK);
    assertThat(SupportedTimezone.fromValue("\tUTC\n"))
        .contains(SupportedTimezone.UTC);
  }

  @Test
  @DisplayName("Should ignore default locale casing rules")
  void shouldNotDependOnDefaultLocale() {
    // Given
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    
    try {
      // When/Then
      assertThat(SupportedTimezone.fromValue("america/new_york"))
          .contains(SupportedTimezone.AMERICA_NEW_YORK);
    } finally {
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  @DisplayName("Should return empty for null, blank, or unknown values")
  void shouldReturnEmptyForInvalidInputs() {
    // When/Then
    assertThat(SupportedTimezone.fromValue(null)).isEmpty();
    assertThat(SupportedTimezone.fromValue("   ")).isEmpty();
    assertThat(SupportedTimezone.fromValue("")).isEmpty();
    assertThat(SupportedTimezone.fromValue("America/Chicago")).isEmpty();
    assertThat(SupportedTimezone.fromValue("Invalid/Timezone")).isEmpty();
  }

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all timezone enums")
  void shouldReturnAllTimezoneEnums() {
    // When
    var all = SupportedTimezone.getAll();
    
    // Then
    assertThat(all).hasSize(4);
    assertThat(all).containsExactlyInAnyOrder(
        SupportedTimezone.UTC,
        SupportedTimezone.AMERICA_NEW_YORK,
        SupportedTimezone.AMERICA_LOS_ANGELES,
        SupportedTimezone.EUROPE_COPENHAGEN
    );
  }
}
