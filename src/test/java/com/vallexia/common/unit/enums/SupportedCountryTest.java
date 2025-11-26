package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedCountry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedCountry enum.
 * Tests that all country entries expose complete metadata.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedCountry Unit Tests")
class SupportedCountryTest {

  // ==================== Metadata Completeness Tests ====================

  @Test
  @DisplayName("Should expose complete metadata for all countries")
  void shouldExposeCompleteMetadataForAllCountries() {
    // Given/When/Then
    for (SupportedCountry country : SupportedCountry.values()) {
      assertThat(country.getLocale()).as(country.name() + " locale").isNotNull();
      assertThat(country.getDefaultDateFormat()).as(country.name() + " date format").isNotNull();
      assertThat(country.getDefaultTimezone()).as(country.name() + " timezone").isNotNull();
      assertThat(country.getFirstDayOfWeek()).as(country.name() + " first day").isNotNull();
      assertThat(country.getMeasurementSystem()).as(country.name() + " measurement system").isNotNull();
      assertThat(country.getDecimalSeparator()).as(country.name() + " decimal separator").isNotBlank();
      assertThat(country.getThousandsSeparator()).as(country.name() + " thousands separator").isNotBlank();
      assertThat(country.getCurrency()).as(country.name() + " currency enum").isNotNull();
      assertThat(country.getCurrencyCode()).as(country.name() + " currency code").isNotBlank();
    }
  }
}
