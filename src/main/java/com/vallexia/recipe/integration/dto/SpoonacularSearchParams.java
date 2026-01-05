package com.vallexia.recipe.integration.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Parameters for searching recipes in Spoonacular API.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@Builder
public class SpoonacularSearchParams {
    
    private String query;
    private List<String> includeIngredients;
    private List<String> excludeIngredients;
    private String diet;
    private List<String> intolerances;
    private List<String> cuisine;
    private List<String> excludeCuisine;
    private Integer number;
    private Integer offset;
    private Boolean addRecipeInformation;
    private Boolean addRecipeInstructions;
    private Boolean addRecipeNutrition;
    private Boolean fillIngredients;
}
