package com.vallexia.common.mapper;

import com.vallexia.common.dto.*;
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
import com.vallexia.user.entity.enums.GoalType;
import com.vallexia.user.entity.enums.MealType;
import com.vallexia.user.entity.enums.SubscriptionStatus;

/**
 * Mapper utility class for converting domain-specific enums to DTOs.
 * 
 * <p>This mapper provides static methods to convert domain enum types from
 * {@code com.vallexia.user.entity.enums} and {@code com.vallexia.recipe.entity.enums}
 * to their corresponding DTOs. All methods perform null validation on input
 * parameters and will throw {@link IllegalArgumentException} if null values are provided.
 * 
 * <p>This mapper is used by {@link com.vallexia.common.controller.LocaleConfigController}
 * to build locale configuration responses for the frontend.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class DomainEnumMapper {

    private DomainEnumMapper() {}

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
     * Converts a {@link DietaryRestriction} enum to a {@link DietaryRestrictionDto}.
     * 
     * @param restriction the dietary restriction enum to convert, must not be null
     * @return the dietary restriction DTO with code and display name
     * @throws IllegalArgumentException if restriction is null
     */
    public static DietaryRestrictionDto toDietaryRestrictionDto(DietaryRestriction restriction) {
        requireNonNull(restriction, "restriction");
        return DietaryRestrictionDto.builder()
                .code(restriction.name())
                .name(restriction.getDisplayName())
                .build();
    }

    /**
     * Converts an {@link Allergy} enum to an {@link AllergyDto}.
     * 
     * @param allergy the allergy enum to convert, must not be null
     * @return the allergy DTO with code and display name
     * @throws IllegalArgumentException if allergy is null
     */
    public static AllergyDto toAllergyDto(Allergy allergy) {
        requireNonNull(allergy, "allergy");
        return AllergyDto.builder()
                .code(allergy.name())
                .name(allergy.getDisplayName())
                .build();
    }

    /**
     * Converts a {@link CuisineType} enum to a {@link CuisineTypeDto}.
     * 
     * @param cuisineType the cuisine type enum to convert, must not be null
     * @return the cuisine type DTO with code and display name
     * @throws IllegalArgumentException if cuisineType is null
     */
    public static CuisineTypeDto toCuisineTypeDto(CuisineType cuisineType) {
        requireNonNull(cuisineType, "cuisineType");
        return CuisineTypeDto.builder()
                .code(cuisineType.name())
                .name(cuisineType.getDisplayName())
                .build();
    }

    /**
     * Converts a {@link DifficultyLevel} enum to a {@link DifficultyLevelDto}.
     * 
     * @param difficultyLevel the difficulty level enum to convert, must not be null
     * @return the difficulty level DTO with code and display name
     * @throws IllegalArgumentException if difficultyLevel is null
     */
    public static DifficultyLevelDto toDifficultyLevelDto(DifficultyLevel difficultyLevel) {
        requireNonNull(difficultyLevel, "difficultyLevel");
        return DifficultyLevelDto.builder()
                .code(difficultyLevel.name())
                .name(difficultyLevel.getDisplayName())
                .build();
    }

    /**
     * Converts a {@link GoalType} enum to a {@link GoalTypeDto}.
     * 
     * @param goalType the goal type enum to convert, must not be null
     * @return the goal type DTO with code and display name
     * @throws IllegalArgumentException if goalType is null
     */
    public static GoalTypeDto toGoalTypeDto(GoalType goalType) {
        requireNonNull(goalType, "goalType");
        return GoalTypeDto.builder()
                .code(goalType.name())
                .name(goalType.getDisplayName())
                .build();
    }

    /**
     * Converts a {@link SubscriptionStatus} enum to a {@link SubscriptionStatusDto}.
     * 
     * <p>Note: The {@link SubscriptionStatus} enum is a simple enum without a
     * {@code getDisplayName()} method, so both code and name fields use
     * {@code status.name()}. This is intentional as the enum values (FREE, PREMIUM,
     * FAMILY, CANCELLED, EXPIRED) are self-descriptive and do not require separate
     * display names.
     * 
     * @param status the subscription status enum to convert, must not be null
     * @return the subscription status DTO with code and name (both using enum name)
     * @throws IllegalArgumentException if status is null
     */
    public static SubscriptionStatusDto toSubscriptionStatusDto(SubscriptionStatus status) {
        requireNonNull(status, "status");
        return SubscriptionStatusDto.builder()
                .code(status.name())
                .name(status.name())
                .build();
    }

    /**
     * Converts a {@link MealType} enum to a {@link MealTypeDto}.
     * 
     * @param mealType the meal type enum to convert, must not be null
     * @return the meal type DTO with code and display name
     * @throws IllegalArgumentException if mealType is null
     */
    public static MealTypeDto toMealTypeDto(MealType mealType) {
        requireNonNull(mealType, "mealType");
        return MealTypeDto.builder()
                .code(mealType.name())
                .name(mealType.getDisplayName())
                .build();
    }
}
