package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * DTO for locale configuration options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Bundle of localization configuration used by the frontend")
public class LocaleConfigDto {

    List<LocaleDto> locales;
    List<CountryDto> countries;
    List<CurrencyDto> currencies;
    List<TimezoneDto> timezones;
    List<FormattingRuleDto> formattingRules;
    List<DateFormatDto> dateFormats;
    List<MeasurementSystemDto> measurementSystems;
    List<UnitDto> weightUnits;
    List<UnitDto> volumeUnits;
    List<UnitDto> countUnits;
    List<FirstDayOfWeekDto> firstDayOfWeek;
    List<MealCategoryDto> mealCategories;
    List<DietaryRestrictionDto> dietaryRestrictions;
    List<AllergyDto> allergies;
    List<CuisineTypeDto> cuisineTypes;
    List<DifficultyLevelDto> difficultyLevels;
    List<GoalTypeDto> goalTypes;
    List<SubscriptionStatusDto> subscriptionStatuses;
    List<MealTypeDto> mealTypes;
}
