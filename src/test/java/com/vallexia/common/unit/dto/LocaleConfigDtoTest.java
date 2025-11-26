package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.AllergyDto;
import com.vallexia.common.dto.CountryDto;
import com.vallexia.common.dto.CuisineTypeDto;
import com.vallexia.common.dto.CurrencyDto;
import com.vallexia.common.dto.DateFormatDto;
import com.vallexia.common.dto.DietaryRestrictionDto;
import com.vallexia.common.dto.DifficultyLevelDto;
import com.vallexia.common.dto.FirstDayOfWeekDto;
import com.vallexia.common.dto.FormattingRuleDto;
import com.vallexia.common.dto.GoalTypeDto;
import com.vallexia.common.dto.LocaleConfigDto;
import com.vallexia.common.dto.LocaleDto;
import com.vallexia.common.dto.MealCategoryDto;
import com.vallexia.common.dto.MeasurementSystemDto;
import com.vallexia.common.dto.SubscriptionStatusDto;
import com.vallexia.common.dto.TimezoneDto;
import com.vallexia.common.dto.UnitDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LocaleConfigDto.
 * Tests builder pattern and immutability with multiple list fields.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("LocaleConfigDto Unit Tests")
class LocaleConfigDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build LocaleConfigDto with all fields")
  void shouldBuildLocaleConfigDtoWithAllFields() {
    // Given
    List<LocaleDto> locales = List.of(
        LocaleDto.builder().code("en").name("English").build()
    );
    List<CountryDto> countries = List.of(
        CountryDto.builder()
            .code("US")
            .name("United States")
            .languageCode("en")
            .defaultDateFormat("MM/dd/yyyy")
            .defaultTimezone("America/New_York")
            .firstDayOfWeek("SUNDAY")
            .measurementSystem("IMPERIAL")
            .build()
    );
    List<CurrencyDto> currencies = List.of(
        CurrencyDto.builder().code("USD").name("US Dollar").build()
    );
    List<TimezoneDto> timezones = List.of(
        TimezoneDto.builder().value("America/New_York").label("Eastern Time").build()
    );
    List<FormattingRuleDto> formattingRules = List.of(
        FormattingRuleDto.builder()
            .countryCode("US")
            .countryName("United States")
            .decimalSeparator(".")
            .thousandsSeparator(",")
            .currencyCode("USD")
            .build()
    );
    List<DateFormatDto> dateFormats = List.of(
        DateFormatDto.builder()
            .code("MM_DD_YYYY")
            .format("MM/DD/YYYY")
            .tokens(List.of())
            .build()
    );
    List<MeasurementSystemDto> measurementSystems = List.of(
        MeasurementSystemDto.builder().code("METRIC").name("Metric").build()
    );
    List<UnitDto> weightUnits = List.of(
        UnitDto.builder().code("GRAM").display("g").conversion(null).build()
    );
    List<UnitDto> volumeUnits = List.of(
        UnitDto.builder().code("MILLILITER").display("ml").conversion(null).build()
    );
    List<UnitDto> countUnits = List.of(
        UnitDto.builder().code("PIECE").display("piece").conversion(null).build()
    );
    List<FirstDayOfWeekDto> firstDayOfWeek = List.of(
        FirstDayOfWeekDto.builder().code("SUNDAY").name("Sunday").build()
    );
    List<MealCategoryDto> mealCategories = List.of(
        MealCategoryDto.builder().code("BREAKFAST").name("Breakfast").build()
    );
    List<DietaryRestrictionDto> dietaryRestrictions = List.of(
        DietaryRestrictionDto.builder().code("VEGAN").name("Vegan").build()
    );
    List<AllergyDto> allergies = List.of(
        AllergyDto.builder().code("PEANUTS").name("Peanuts").build()
    );
    List<CuisineTypeDto> cuisineTypes = List.of(
        CuisineTypeDto.builder().code("ITALIAN").name("Italian").build()
    );
    List<DifficultyLevelDto> difficultyLevels = List.of(
        DifficultyLevelDto.builder().code("EASY").name("Easy").build()
    );
    List<GoalTypeDto> goalTypes = List.of(
        GoalTypeDto.builder().code("WEIGHT_LOSS").name("Weight Loss").build()
    );
    List<SubscriptionStatusDto> subscriptionStatuses = List.of(
        SubscriptionStatusDto.builder().code("FREE").name("Free").build()
    );

    // When
    LocaleConfigDto dto = LocaleConfigDto.builder()
        .locales(locales)
        .countries(countries)
        .currencies(currencies)
        .timezones(timezones)
        .formattingRules(formattingRules)
        .dateFormats(dateFormats)
        .measurementSystems(measurementSystems)
        .weightUnits(weightUnits)
        .volumeUnits(volumeUnits)
        .countUnits(countUnits)
        .firstDayOfWeek(firstDayOfWeek)
        .mealCategories(mealCategories)
        .dietaryRestrictions(dietaryRestrictions)
        .allergies(allergies)
        .cuisineTypes(cuisineTypes)
        .difficultyLevels(difficultyLevels)
        .goalTypes(goalTypes)
        .subscriptionStatuses(subscriptionStatuses)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getLocales()).isEqualTo(locales);
    assertThat(dto.getCountries()).isEqualTo(countries);
    assertThat(dto.getCurrencies()).isEqualTo(currencies);
    assertThat(dto.getTimezones()).isEqualTo(timezones);
    assertThat(dto.getFormattingRules()).isEqualTo(formattingRules);
    assertThat(dto.getDateFormats()).isEqualTo(dateFormats);
    assertThat(dto.getMeasurementSystems()).isEqualTo(measurementSystems);
    assertThat(dto.getWeightUnits()).isEqualTo(weightUnits);
    assertThat(dto.getVolumeUnits()).isEqualTo(volumeUnits);
    assertThat(dto.getCountUnits()).isEqualTo(countUnits);
    assertThat(dto.getFirstDayOfWeek()).isEqualTo(firstDayOfWeek);
    assertThat(dto.getMealCategories()).isEqualTo(mealCategories);
    assertThat(dto.getDietaryRestrictions()).isEqualTo(dietaryRestrictions);
    assertThat(dto.getAllergies()).isEqualTo(allergies);
    assertThat(dto.getCuisineTypes()).isEqualTo(cuisineTypes);
    assertThat(dto.getDifficultyLevels()).isEqualTo(difficultyLevels);
    assertThat(dto.getGoalTypes()).isEqualTo(goalTypes);
    assertThat(dto.getSubscriptionStatuses()).isEqualTo(subscriptionStatuses);
  }

  @Test
  @DisplayName("Should build LocaleConfigDto with empty lists")
  void shouldBuildLocaleConfigDtoWithEmptyLists() {
    // When
    LocaleConfigDto dto = LocaleConfigDto.builder()
        .locales(List.of())
        .countries(List.of())
        .currencies(List.of())
        .timezones(List.of())
        .formattingRules(List.of())
        .dateFormats(List.of())
        .measurementSystems(List.of())
        .weightUnits(List.of())
        .volumeUnits(List.of())
        .countUnits(List.of())
        .firstDayOfWeek(List.of())
        .mealCategories(List.of())
        .dietaryRestrictions(List.of())
        .allergies(List.of())
        .cuisineTypes(List.of())
        .difficultyLevels(List.of())
        .goalTypes(List.of())
        .subscriptionStatuses(List.of())
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getLocales()).isEmpty();
    assertThat(dto.getCountries()).isEmpty();
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given
    List<LocaleDto> locales = List.of(
        LocaleDto.builder().code("en").name("English").build()
    );

    // When
    LocaleConfigDto dto1 = LocaleConfigDto.builder()
        .locales(locales)
        .countries(List.of())
        .currencies(List.of())
        .timezones(List.of())
        .formattingRules(List.of())
        .dateFormats(List.of())
        .measurementSystems(List.of())
        .weightUnits(List.of())
        .volumeUnits(List.of())
        .countUnits(List.of())
        .firstDayOfWeek(List.of())
        .mealCategories(List.of())
        .dietaryRestrictions(List.of())
        .allergies(List.of())
        .cuisineTypes(List.of())
        .difficultyLevels(List.of())
        .goalTypes(List.of())
        .subscriptionStatuses(List.of())
        .build();
    LocaleConfigDto dto2 = LocaleConfigDto.builder()
        .locales(locales)
        .countries(List.of())
        .currencies(List.of())
        .timezones(List.of())
        .formattingRules(List.of())
        .dateFormats(List.of())
        .measurementSystems(List.of())
        .weightUnits(List.of())
        .volumeUnits(List.of())
        .countUnits(List.of())
        .firstDayOfWeek(List.of())
        .mealCategories(List.of())
        .dietaryRestrictions(List.of())
        .allergies(List.of())
        .cuisineTypes(List.of())
        .difficultyLevels(List.of())
        .goalTypes(List.of())
        .subscriptionStatuses(List.of())
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }
}
