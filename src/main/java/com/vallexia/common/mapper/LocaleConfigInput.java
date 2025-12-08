package com.vallexia.common.mapper;

import com.vallexia.common.dto.*;

import java.util.List;

/**
 * Parameter object for building locale configuration.
 * Encapsulates all locale-related configuration lists to reduce method parameter count.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-08
 */
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
    private final List<DifficultyLevelDto> difficultyLevels;
    private final List<GoalTypeDto> goalTypes;
    private final List<SubscriptionStatusDto> subscriptionStatuses;

    public LocaleConfigInput(List<LocaleDto> locales,
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
                            List<SubscriptionStatusDto> subscriptionStatuses) {
        this.locales = locales;
        this.countries = countries;
        this.currencies = currencies;
        this.timezones = timezones;
        this.formattingRules = formattingRules;
        this.dateFormats = dateFormats;
        this.measurementSystems = measurementSystems;
        this.weightUnits = weightUnits;
        this.volumeUnits = volumeUnits;
        this.countUnits = countUnits;
        this.firstDayOfWeek = firstDayOfWeek;
        this.mealCategories = mealCategories;
        this.dietaryRestrictions = dietaryRestrictions;
        this.allergies = allergies;
        this.cuisineTypes = cuisineTypes;
        this.difficultyLevels = difficultyLevels;
        this.goalTypes = goalTypes;
        this.subscriptionStatuses = subscriptionStatuses;
    }

    public List<LocaleDto> getLocales() {
        return locales;
    }

    public List<CountryDto> getCountries() {
        return countries;
    }

    public List<CurrencyDto> getCurrencies() {
        return currencies;
    }

    public List<TimezoneDto> getTimezones() {
        return timezones;
    }

    public List<FormattingRuleDto> getFormattingRules() {
        return formattingRules;
    }

    public List<DateFormatDto> getDateFormats() {
        return dateFormats;
    }

    public List<MeasurementSystemDto> getMeasurementSystems() {
        return measurementSystems;
    }

    public List<UnitDto> getWeightUnits() {
        return weightUnits;
    }

    public List<UnitDto> getVolumeUnits() {
        return volumeUnits;
    }

    public List<UnitDto> getCountUnits() {
        return countUnits;
    }

    public List<FirstDayOfWeekDto> getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public List<MealCategoryDto> getMealCategories() {
        return mealCategories;
    }

    public List<DietaryRestrictionDto> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public List<AllergyDto> getAllergies() {
        return allergies;
    }

    public List<CuisineTypeDto> getCuisineTypes() {
        return cuisineTypes;
    }

    public List<DifficultyLevelDto> getDifficultyLevels() {
        return difficultyLevels;
    }

    public List<GoalTypeDto> getGoalTypes() {
        return goalTypes;
    }

    public List<SubscriptionStatusDto> getSubscriptionStatuses() {
        return subscriptionStatuses;
    }
}
