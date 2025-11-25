package com.vallexia.common.mapper;

import com.vallexia.common.dto.*;

import java.util.List;

/**
 * Utility class for building complete locale configuration DTOs.
 * 
 * <p>This class provides a method to aggregate all locale-related configuration
 * DTOs into a single bundle that can be returned to the frontend. All list
 * parameters can be empty lists, but null lists are not allowed.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class LocaleConfigBuilder {

    private LocaleConfigBuilder() {}

    /**
     * Validates that an object is not null, throwing IllegalArgumentException if it is.
     * 
     * @param obj the object to validate
     * @param paramName the parameter name for error message
     * @param <T> the type of the object
     * @return the object if not null
     * @throws IllegalArgumentException if obj is null
     */
    private static <T> T requireNonNull(T obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " must not be null");
        }
        return obj;
    }

    /**
     * Builds a complete {@link LocaleConfigDto} from all locale-related configuration lists.
     * 
     * <p>This method aggregates all locale configuration DTOs into a single bundle
     * that can be returned to the frontend. All list parameters are optional and
     * can be empty lists, but null lists are not allowed (use empty lists instead).
     * 
     * @param locales list of locale DTOs, must not be null
     * @param countries list of country DTOs, must not be null
     * @param currencies list of currency DTOs, must not be null
     * @param timezones list of timezone DTOs, must not be null
     * @param formattingRules list of formatting rule DTOs, must not be null
     * @param dateFormats list of date format DTOs, must not be null
     * @param measurementSystems list of measurement system DTOs, must not be null
     * @param weightUnits list of weight unit DTOs, must not be null
     * @param volumeUnits list of volume unit DTOs, must not be null
     * @param countUnits list of count unit DTOs, must not be null
     * @param firstDayOfWeek list of first day of week DTOs, must not be null
     * @param mealCategories list of meal category DTOs, must not be null
     * @param dietaryRestrictions list of dietary restriction DTOs, must not be null
     * @param allergies list of allergy DTOs, must not be null
     * @param cuisineTypes list of cuisine type DTOs, must not be null
     * @param difficultyLevels list of difficulty level DTOs, must not be null
     * @param goalTypes list of goal type DTOs, must not be null
     * @param subscriptionStatuses list of subscription status DTOs, must not be null
     * @param mealTypes list of meal type DTOs, must not be null
     * @return the complete locale configuration DTO bundle
     * @throws IllegalArgumentException if any list parameter is null
     */
    public static LocaleConfigDto buildConfig(List<LocaleDto> locales,
                                             List<CountryDto> countries,
                                             List<CurrencyDto> currencies,
                                             List<TimezoneDto> timezones,
                                             List<FormattingRuleDto> formattingRules,
                                             List<DateFormatDto> dateFormats,
                                             List<MeasurementSystemDto> measurementSystems,
                                             List<UnitDto> weightUnits,
                                             List<UnitDto> volumeUnits,
                                             List<UnitDto> countUnits,
                                             List<FirstDayOfWeekDto> firstDayOfWeek,
                                             List<MealCategoryDto> mealCategories,
                                             List<DietaryRestrictionDto> dietaryRestrictions,
                                             List<AllergyDto> allergies,
                                             List<CuisineTypeDto> cuisineTypes,
                                             List<DifficultyLevelDto> difficultyLevels,
                                             List<GoalTypeDto> goalTypes,
                                             List<SubscriptionStatusDto> subscriptionStatuses,
                                             List<MealTypeDto> mealTypes) {
        requireNonNull(locales, "locales");
        requireNonNull(countries, "countries");
        requireNonNull(currencies, "currencies");
        requireNonNull(timezones, "timezones");
        requireNonNull(formattingRules, "formattingRules");
        requireNonNull(dateFormats, "dateFormats");
        requireNonNull(measurementSystems, "measurementSystems");
        requireNonNull(weightUnits, "weightUnits");
        requireNonNull(volumeUnits, "volumeUnits");
        requireNonNull(countUnits, "countUnits");
        requireNonNull(firstDayOfWeek, "firstDayOfWeek");
        requireNonNull(mealCategories, "mealCategories");
        requireNonNull(dietaryRestrictions, "dietaryRestrictions");
        requireNonNull(allergies, "allergies");
        requireNonNull(cuisineTypes, "cuisineTypes");
        requireNonNull(difficultyLevels, "difficultyLevels");
        requireNonNull(goalTypes, "goalTypes");
        requireNonNull(subscriptionStatuses, "subscriptionStatuses");
        requireNonNull(mealTypes, "mealTypes");
        
        return LocaleConfigDto.builder()
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
                .mealTypes(mealTypes)
                .build();
    }
}
