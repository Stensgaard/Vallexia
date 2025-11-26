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
import com.vallexia.common.enums.SupportedWeightUnit;
import com.vallexia.common.enums.SupportedVolumeUnit;
import com.vallexia.common.enums.SupportedCountUnit;
import com.vallexia.common.mapper.DomainEnumMapper;
import com.vallexia.common.mapper.LocaleConfigBuilder;
import com.vallexia.common.mapper.LocaleMapper;
import com.vallexia.common.mapper.UnitMapper;
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.user.entity.enums.GoalType;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LocaleConfigBuilder.
 * Tests building complete locale configuration DTOs with null safety validation.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("LocaleConfigBuilder Unit Tests")
class LocaleConfigBuilderTest {

  // ==================== buildConfig() Tests ====================

  @Test
  @DisplayName("Should build LocaleConfigDto with all lists")
  void shouldBuildLocaleConfigDtoWithAllLists() {
    // Given
    List<LocaleDto> locales = Collections.singletonList(
        LocaleMapper.toLocaleDto(SupportedLocale.EN));
    List<CountryDto> countries = Collections.singletonList(
        LocaleMapper.toCountryDto(SupportedCountry.US));
    List<CurrencyDto> currencies = Collections.singletonList(
        LocaleMapper.toCurrencyDto(SupportedCurrency.USD));
    List<TimezoneDto> timezones = Collections.singletonList(
        LocaleMapper.toTimezoneDto(SupportedTimezone.AMERICA_NEW_YORK));
    List<FormattingRuleDto> formattingRules = Collections.singletonList(
        LocaleMapper.toFormattingRuleDto(SupportedCountry.US));
    List<DateFormatDto> dateFormats = Collections.singletonList(
        LocaleMapper.toDateFormatDto(SupportedDateFormat.MM_DD_YYYY));
    List<MeasurementSystemDto> measurementSystems = Collections.singletonList(
        LocaleMapper.toMeasurementSystemDto(SupportedMeasurementSystem.METRIC));
    List<UnitDto> weightUnits = Collections.singletonList(
        UnitMapper.toWeightUnitDto(SupportedWeightUnit.GRAM));
    List<UnitDto> volumeUnits = Collections.singletonList(
        UnitMapper.toVolumeUnitDto(SupportedVolumeUnit.MILLILITER));
    List<UnitDto> countUnits = Collections.singletonList(
        UnitMapper.toCountUnitDto(SupportedCountUnit.PIECE));
    List<FirstDayOfWeekDto> firstDayOfWeek = Collections.singletonList(
        LocaleMapper.toFirstDayOfWeekDto(SupportedFirstDayOfWeek.MONDAY));
    List<MealCategoryDto> mealCategories = Collections.singletonList(
        LocaleMapper.toMealCategoryDto(SupportedMealCategory.getAll().get(0)));
    List<DietaryRestrictionDto> dietaryRestrictions = Collections.singletonList(
        DomainEnumMapper.toDietaryRestrictionDto(SupportedDietaryRestriction.VEGETARIAN));
    List<AllergyDto> allergies = Collections.singletonList(
        DomainEnumMapper.toAllergyDto(SupportedAllergy.PEANUTS));
    List<CuisineTypeDto> cuisineTypes = Collections.singletonList(
        DomainEnumMapper.toCuisineTypeDto(SupportedCuisineType.ITALIAN));
    List<DifficultyLevelDto> difficultyLevels = Collections.singletonList(
        DomainEnumMapper.toDifficultyLevelDto(DifficultyLevel.EASY));
    List<GoalTypeDto> goalTypes = Collections.singletonList(
        DomainEnumMapper.toGoalTypeDto(GoalType.WEIGHT_LOSS));
    List<SubscriptionStatusDto> subscriptionStatuses = Collections.singletonList(
        DomainEnumMapper.toSubscriptionStatusDto(SubscriptionStatus.PREMIUM));

    // When
    LocaleConfigDto config = LocaleConfigBuilder.buildConfig(
        locales, countries, currencies, timezones, formattingRules,
        dateFormats, measurementSystems, weightUnits, volumeUnits, countUnits,
        firstDayOfWeek, mealCategories, dietaryRestrictions, allergies,
        cuisineTypes, difficultyLevels, goalTypes, subscriptionStatuses);

    // Then
    assertThat(config).isNotNull();
    assertThat(config.getLocales()).isEqualTo(locales);
    assertThat(config.getCountries()).isEqualTo(countries);
    assertThat(config.getCurrencies()).isEqualTo(currencies);
    assertThat(config.getTimezones()).isEqualTo(timezones);
    assertThat(config.getFormattingRules()).isEqualTo(formattingRules);
    assertThat(config.getDateFormats()).isEqualTo(dateFormats);
    assertThat(config.getMeasurementSystems()).isEqualTo(measurementSystems);
    assertThat(config.getWeightUnits()).isEqualTo(weightUnits);
    assertThat(config.getVolumeUnits()).isEqualTo(volumeUnits);
    assertThat(config.getCountUnits()).isEqualTo(countUnits);
    assertThat(config.getFirstDayOfWeek()).isEqualTo(firstDayOfWeek);
    assertThat(config.getMealCategories()).isEqualTo(mealCategories);
    assertThat(config.getDietaryRestrictions()).isEqualTo(dietaryRestrictions);
    assertThat(config.getAllergies()).isEqualTo(allergies);
    assertThat(config.getCuisineTypes()).isEqualTo(cuisineTypes);
    assertThat(config.getDifficultyLevels()).isEqualTo(difficultyLevels);
    assertThat(config.getGoalTypes()).isEqualTo(goalTypes);
    assertThat(config.getSubscriptionStatuses()).isEqualTo(subscriptionStatuses);
  }

  @Test
  @DisplayName("Should build LocaleConfigDto with empty lists")
  void shouldBuildLocaleConfigDtoWithEmptyLists() {
    // Given
    List<LocaleDto> emptyLocales = Collections.emptyList();
    List<CountryDto> emptyCountries = Collections.emptyList();
    List<CurrencyDto> emptyCurrencies = Collections.emptyList();
    List<TimezoneDto> emptyTimezones = Collections.emptyList();
    List<FormattingRuleDto> emptyFormattingRules = Collections.emptyList();
    List<DateFormatDto> emptyDateFormats = Collections.emptyList();
    List<MeasurementSystemDto> emptyMeasurementSystems = Collections.emptyList();
    List<UnitDto> emptyWeightUnits = Collections.emptyList();
    List<UnitDto> emptyVolumeUnits = Collections.emptyList();
    List<UnitDto> emptyCountUnits = Collections.emptyList();
    List<FirstDayOfWeekDto> emptyFirstDayOfWeek = Collections.emptyList();
    List<MealCategoryDto> emptyMealCategories = Collections.emptyList();
    List<DietaryRestrictionDto> emptyDietaryRestrictions = Collections.emptyList();
    List<AllergyDto> emptyAllergies = Collections.emptyList();
    List<CuisineTypeDto> emptyCuisineTypes = Collections.emptyList();
    List<DifficultyLevelDto> emptyDifficultyLevels = Collections.emptyList();
    List<GoalTypeDto> emptyGoalTypes = Collections.emptyList();
    List<SubscriptionStatusDto> emptySubscriptionStatuses = Collections.emptyList();

    // When
    LocaleConfigDto config = LocaleConfigBuilder.buildConfig(
        emptyLocales, emptyCountries, emptyCurrencies, emptyTimezones, emptyFormattingRules,
        emptyDateFormats, emptyMeasurementSystems, emptyWeightUnits, emptyVolumeUnits, emptyCountUnits,
        emptyFirstDayOfWeek, emptyMealCategories, emptyDietaryRestrictions, emptyAllergies,
        emptyCuisineTypes, emptyDifficultyLevels, emptyGoalTypes, emptySubscriptionStatuses);

    // Then
    assertThat(config).isNotNull();
    assertThat(config.getLocales()).isEmpty();
    assertThat(config.getCountries()).isEmpty();
    assertThat(config.getCurrencies()).isEmpty();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when any list parameter is null in buildConfig")
  void shouldThrowIllegalArgumentExceptionWhenAnyListParameterIsNull() {
    // Given
    List<LocaleDto> emptyLocales = Collections.emptyList();
    List<CountryDto> emptyCountries = Collections.emptyList();
    List<CurrencyDto> emptyCurrencies = Collections.emptyList();
    List<TimezoneDto> emptyTimezones = Collections.emptyList();
    List<FormattingRuleDto> emptyFormattingRules = Collections.emptyList();
    List<DateFormatDto> emptyDateFormats = Collections.emptyList();
    List<MeasurementSystemDto> emptyMeasurementSystems = Collections.emptyList();
    List<UnitDto> emptyWeightUnits = Collections.emptyList();
    List<UnitDto> emptyVolumeUnits = Collections.emptyList();
    List<UnitDto> emptyCountUnits = Collections.emptyList();
    List<FirstDayOfWeekDto> emptyFirstDayOfWeek = Collections.emptyList();
    List<MealCategoryDto> emptyMealCategories = Collections.emptyList();
    List<DietaryRestrictionDto> emptyDietaryRestrictions = Collections.emptyList();
    List<AllergyDto> emptyAllergies = Collections.emptyList();
    List<CuisineTypeDto> emptyCuisineTypes = Collections.emptyList();
    List<DifficultyLevelDto> emptyDifficultyLevels = Collections.emptyList();
    List<GoalTypeDto> emptyGoalTypes = Collections.emptyList();
    List<SubscriptionStatusDto> emptySubscriptionStatuses = Collections.emptyList();

    // When/Then - Test first parameter
    assertThatThrownBy(() -> LocaleConfigBuilder.buildConfig(
        null, emptyCountries, emptyCurrencies, emptyTimezones, emptyFormattingRules,
        emptyDateFormats, emptyMeasurementSystems, emptyWeightUnits, emptyVolumeUnits, emptyCountUnits,
        emptyFirstDayOfWeek, emptyMealCategories, emptyDietaryRestrictions, emptyAllergies,
        emptyCuisineTypes, emptyDifficultyLevels, emptyGoalTypes, emptySubscriptionStatuses))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("locales must not be null");

    // When/Then - Test last parameter
    assertThatThrownBy(() -> LocaleConfigBuilder.buildConfig(
        emptyLocales, emptyCountries, emptyCurrencies, emptyTimezones, emptyFormattingRules,
        emptyDateFormats, emptyMeasurementSystems, emptyWeightUnits, emptyVolumeUnits, emptyCountUnits,
        emptyFirstDayOfWeek, emptyMealCategories, emptyDietaryRestrictions, emptyAllergies,
        emptyCuisineTypes, emptyDifficultyLevels, emptyGoalTypes, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("subscriptionStatuses must not be null");
  }
}
