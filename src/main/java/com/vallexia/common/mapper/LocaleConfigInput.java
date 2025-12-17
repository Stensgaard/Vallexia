package com.vallexia.common.mapper;

import com.vallexia.common.dto.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Parameter object for building locale configuration.
 * Encapsulates all locale-related configuration lists to reduce method parameter count.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-08
 */
@Getter
@Builder
public class LocaleConfigInput {
    private final List<LocaleDto> locales;
    private final List<CountryDto> countries;
    private final List<CurrencyDto> currencies;
    private final List<TimezoneDto> timezones;
    private final List<FormattingRuleDto> formattingRules;
    private final List<DateFormatDto> dateFormats;
    private final List<MeasurementSystemDto> measurementSystems;
    private final List<UnitDto> weightUnits;
    private final List<UnitDto> volumeUnits;
    private final List<UnitDto> countUnits;
    private final List<FirstDayOfWeekDto> firstDayOfWeek;
    private final List<MealCategoryDto> mealCategories;
    private final List<DietaryRestrictionDto> dietaryRestrictions;
    private final List<AllergyDto> allergies;
    private final List<CuisineTypeDto> cuisineTypes;
    private final List<GoalTypeDto> goalTypes;
    private final List<SubscriptionStatusDto> subscriptionStatuses;
}
