package com.vallexia.common.mapper;

import com.vallexia.common.dto.*;

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
     * @param input the locale configuration input containing all required lists
     * @return the complete locale configuration DTO bundle
     * @throws IllegalArgumentException if any list in the input is null
     */
    public static LocaleConfigDto buildConfig(LocaleConfigInput input) {
        requireNonNull(input, "input");
        requireNonNull(input.getLocales(), "locales");
        requireNonNull(input.getCountries(), "countries");
        requireNonNull(input.getCurrencies(), "currencies");
        requireNonNull(input.getTimezones(), "timezones");
        requireNonNull(input.getFormattingRules(), "formattingRules");
        requireNonNull(input.getDateFormats(), "dateFormats");
        requireNonNull(input.getMeasurementSystems(), "measurementSystems");
        requireNonNull(input.getWeightUnits(), "weightUnits");
        requireNonNull(input.getVolumeUnits(), "volumeUnits");
        requireNonNull(input.getCountUnits(), "countUnits");
        requireNonNull(input.getFirstDayOfWeek(), "firstDayOfWeek");
        requireNonNull(input.getMealCategories(), "mealCategories");
        requireNonNull(input.getDietaryRestrictions(), "dietaryRestrictions");
        requireNonNull(input.getAllergies(), "allergies");
        requireNonNull(input.getCuisineTypes(), "cuisineTypes");
        requireNonNull(input.getGoalTypes(), "goalTypes");
        requireNonNull(input.getSubscriptionStatuses(), "subscriptionStatuses");
        
        return LocaleConfigDto.builder()
                .locales(input.getLocales())
                .countries(input.getCountries())
                .currencies(input.getCurrencies())
                .timezones(input.getTimezones())
                .formattingRules(input.getFormattingRules())
                .dateFormats(input.getDateFormats())
                .measurementSystems(input.getMeasurementSystems())
                .weightUnits(input.getWeightUnits())
                .volumeUnits(input.getVolumeUnits())
                .countUnits(input.getCountUnits())
                .firstDayOfWeek(input.getFirstDayOfWeek())
                .mealCategories(input.getMealCategories())
                .dietaryRestrictions(input.getDietaryRestrictions())
                .allergies(input.getAllergies())
                .cuisineTypes(input.getCuisineTypes())
                .goalTypes(input.getGoalTypes())
                .subscriptionStatuses(input.getSubscriptionStatuses())
                .build();
    }
}
