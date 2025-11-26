package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.CountryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CountryDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("CountryDto Unit Tests")
class CountryDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build CountryDto with all fields")
  void shouldBuildCountryDtoWithAllFields() {
    // Given
    String code = "US";
    String name = "United States";
    String languageCode = "en";
    String defaultDateFormat = "MM/dd/yyyy";
    String defaultTimezone = "America/New_York";
    String firstDayOfWeek = "SUNDAY";
    String measurementSystem = "IMPERIAL";

    // When
    CountryDto dto = CountryDto.builder()
        .code(code)
        .name(name)
        .languageCode(languageCode)
        .defaultDateFormat(defaultDateFormat)
        .defaultTimezone(defaultTimezone)
        .firstDayOfWeek(firstDayOfWeek)
        .measurementSystem(measurementSystem)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo(code);
    assertThat(dto.getName()).isEqualTo(name);
    assertThat(dto.getLanguageCode()).isEqualTo(languageCode);
    assertThat(dto.getDefaultDateFormat()).isEqualTo(defaultDateFormat);
    assertThat(dto.getDefaultTimezone()).isEqualTo(defaultTimezone);
    assertThat(dto.getFirstDayOfWeek()).isEqualTo(firstDayOfWeek);
    assertThat(dto.getMeasurementSystem()).isEqualTo(measurementSystem);
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given/When
    CountryDto dto1 = CountryDto.builder()
        .code("US")
        .name("United States")
        .languageCode("en")
        .defaultDateFormat("MM/dd/yyyy")
        .defaultTimezone("America/New_York")
        .firstDayOfWeek("SUNDAY")
        .measurementSystem("IMPERIAL")
        .build();
    CountryDto dto2 = CountryDto.builder()
        .code("US")
        .name("United States")
        .languageCode("en")
        .defaultDateFormat("MM/dd/yyyy")
        .defaultTimezone("America/New_York")
        .firstDayOfWeek("SUNDAY")
        .measurementSystem("IMPERIAL")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    CountryDto dto1 = CountryDto.builder()
        .code("US")
        .name("United States")
        .languageCode("en")
        .defaultDateFormat("MM/dd/yyyy")
        .defaultTimezone("America/New_York")
        .firstDayOfWeek("SUNDAY")
        .measurementSystem("IMPERIAL")
        .build();
    CountryDto dto2 = CountryDto.builder()
        .code("DK")
        .name("Denmark")
        .languageCode("da")
        .defaultDateFormat("dd/MM/yyyy")
        .defaultTimezone("Europe/Copenhagen")
        .firstDayOfWeek("MONDAY")
        .measurementSystem("METRIC")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
