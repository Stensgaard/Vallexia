package com.vallexia.common.unit.mapper;

import com.vallexia.common.dto.*;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.common.mapper.LocaleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LocaleMapper.
 * Tests enum-to-DTO mapping with null safety validation for locale and regional configuration enums.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("LocaleMapper Unit Tests")
class LocaleMapperTest {

  // ==================== toLocaleDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedLocale to LocaleDto")
  void shouldMapSupportedLocaleToLocaleDto() {
    // Given
    SupportedLocale locale = SupportedLocale.EN;

    // When
    LocaleDto dto = LocaleMapper.toLocaleDto(locale);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("en");
    assertThat(dto.getName()).isEqualTo("EN");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when locale is null")
  void shouldThrowIllegalArgumentExceptionWhenLocaleIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toLocaleDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("supportedLocale must not be null");
  }

  // ==================== toCountryDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedCountry to CountryDto")
  void shouldMapSupportedCountryToCountryDto() {
    // Given
    SupportedCountry country = SupportedCountry.US;

    // When
    CountryDto dto = LocaleMapper.toCountryDto(country);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("US");
    assertThat(dto.getName()).isEqualTo("United States");
    assertThat(dto.getLanguageCode()).isNotNull();
    assertThat(dto.getDefaultDateFormat()).isNotNull();
    assertThat(dto.getDefaultTimezone()).isNotNull();
    assertThat(dto.getFirstDayOfWeek()).isNotNull();
    assertThat(dto.getMeasurementSystem()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when country is null")
  void shouldThrowIllegalArgumentExceptionWhenCountryIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toCountryDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("country must not be null");
  }

  // ==================== toCurrencyDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedCurrency to CurrencyDto")
  void shouldMapSupportedCurrencyToCurrencyDto() {
    // Given
    SupportedCurrency currency = SupportedCurrency.USD;

    // When
    CurrencyDto dto = LocaleMapper.toCurrencyDto(currency);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("USD");
    assertThat(dto.getName()).isEqualTo("US Dollar");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when currency is null")
  void shouldThrowIllegalArgumentExceptionWhenCurrencyIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toCurrencyDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("currency must not be null");
  }

  // ==================== toTimezoneDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedTimezone to TimezoneDto")
  void shouldMapSupportedTimezoneToTimezoneDto() {
    // Given
    SupportedTimezone timezone = SupportedTimezone.AMERICA_NEW_YORK;

    // When
    TimezoneDto dto = LocaleMapper.toTimezoneDto(timezone);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getValue()).isNotNull();
    assertThat(dto.getLabel()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when timezone is null")
  void shouldThrowIllegalArgumentExceptionWhenTimezoneIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toTimezoneDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("timezone must not be null");
  }

  // ==================== toFormattingRuleDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedCountry to FormattingRuleDto")
  void shouldMapSupportedCountryToFormattingRuleDto() {
    // Given
    SupportedCountry country = SupportedCountry.US;

    // When
    FormattingRuleDto dto = LocaleMapper.toFormattingRuleDto(country);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCountryCode()).isEqualTo("US");
    assertThat(dto.getCountryName()).isEqualTo("United States");
    assertThat(dto.getDecimalSeparator()).isNotNull();
    assertThat(dto.getThousandsSeparator()).isNotNull();
    assertThat(dto.getCurrencyCode()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when country is null for formatting rule")
  void shouldThrowIllegalArgumentExceptionWhenCountryIsNullForFormattingRule() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toFormattingRuleDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("country must not be null");
  }

  // ==================== toDateFormatDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedDateFormat to DateFormatDto")
  void shouldMapSupportedDateFormatToDateFormatDto() {
    // Given
    SupportedDateFormat format = SupportedDateFormat.MM_DD_YYYY;

    // When
    DateFormatDto dto = LocaleMapper.toDateFormatDto(format);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("MM_DD_YYYY");
    assertThat(dto.getFormat()).isNotNull();
    assertThat(dto.getTokens()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when date format is null")
  void shouldThrowIllegalArgumentExceptionWhenDateFormatIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toDateFormatDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("format must not be null");
  }

  // ==================== toMeasurementSystemDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedMeasurementSystem to MeasurementSystemDto")
  void shouldMapSupportedMeasurementSystemToMeasurementSystemDto() {
    // Given
    SupportedMeasurementSystem system = SupportedMeasurementSystem.METRIC;

    // When
    MeasurementSystemDto dto = LocaleMapper.toMeasurementSystemDto(system);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("METRIC");
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when measurement system is null")
  void shouldThrowIllegalArgumentExceptionWhenMeasurementSystemIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toMeasurementSystemDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("measurementSystem must not be null");
  }

  // ==================== toFirstDayOfWeekDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedFirstDayOfWeek to FirstDayOfWeekDto")
  void shouldMapSupportedFirstDayOfWeekToFirstDayOfWeekDto() {
    // Given
    SupportedFirstDayOfWeek day = SupportedFirstDayOfWeek.MONDAY;

    // When
    FirstDayOfWeekDto dto = LocaleMapper.toFirstDayOfWeekDto(day);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("MONDAY");
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when first day of week is null")
  void shouldThrowIllegalArgumentExceptionWhenFirstDayOfWeekIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toFirstDayOfWeekDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("day must not be null");
  }

  // ==================== toMealCategoryDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedMealCategory to MealCategoryDto")
  void shouldMapSupportedMealCategoryToMealCategoryDto() {
    // Given
    SupportedMealCategory category = SupportedMealCategory.getAll().get(0);

    // When
    MealCategoryDto dto = LocaleMapper.toMealCategoryDto(category);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isNotNull();
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when meal category is null")
  void shouldThrowIllegalArgumentExceptionWhenMealCategoryIsNull() {
    // When/Then
    assertThatThrownBy(() -> LocaleMapper.toMealCategoryDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("category must not be null");
  }
}
