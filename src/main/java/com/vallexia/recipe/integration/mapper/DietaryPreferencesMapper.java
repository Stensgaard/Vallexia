package com.vallexia.recipe.integration.mapper;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.recipe.integration.dto.SpoonacularSearchParams;
import com.vallexia.user.dto.DietaryPreferencesDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting user dietary preferences to Spoonacular API search parameters.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Component
public class DietaryPreferencesMapper {
    
    /**
     * Convert dietary preferences to Spoonacular search parameters.
     * 
     * @param dietaryPreferences the user's dietary preferences
     * @return SpoonacularSearchParams builder
     */
    public SpoonacularSearchParams.SpoonacularSearchParamsBuilder toSearchParams(
            DietaryPreferencesDto dietaryPreferences) {
        
        SpoonacularSearchParams.SpoonacularSearchParamsBuilder builder = 
                SpoonacularSearchParams.builder();
        
        // Map dietary restriction to diet parameter (single value - Spoonacular only supports one)
        if (dietaryPreferences.getRestriction() != null) {
            builder.diet(dietaryPreferences.getRestriction().getSpoonacularValue());
        }
        
        // Map allergies to intolerances parameter (comma-separated)
        if (dietaryPreferences.getAllergies() != null && 
                !dietaryPreferences.getAllergies().isEmpty()) {
            builder.intolerances(dietaryPreferences.getAllergies().stream()
                    .map(SupportedAllergy::getSpoonacularValue)
                    .collect(Collectors.toList()));
        }
        
        // Map preferred cuisines to cuisine parameter (comma-separated)
        if (dietaryPreferences.getPreferredCuisines() != null && 
                !dietaryPreferences.getPreferredCuisines().isEmpty()) {
            builder.cuisine(dietaryPreferences.getPreferredCuisines().stream()
                    .map(SupportedCuisineType::getSpoonacularValue)
                    .collect(Collectors.toList()));
        }
        
        return builder;
    }
    
    /**
     * Build search params with additional options for full recipe information.
     * 
     * @param dietaryPreferences the user's dietary preferences
     * @param number number of results to return
     * @param offset pagination offset
     * @return complete SpoonacularSearchParams
     */
    public SpoonacularSearchParams buildSearchParams(
            DietaryPreferencesDto dietaryPreferences,
            Integer number,
            Integer offset) {
        
        return toSearchParams(dietaryPreferences)
                .number(number != null ? number : 20)
                .offset(offset != null ? offset : 0)
                .addRecipeInformation(true)
                .addRecipeInstructions(true)
                .addRecipeNutrition(true)
                .fillIngredients(true)
                .build();
    }
}
