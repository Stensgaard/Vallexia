package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeSortBy;
import com.vallexia.recipe.entity.enums.RecipeSortOrder;
import com.vallexia.recipe.entity.enums.RestrictionMatchMode;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.validator.ValidCuisineType;
import com.vallexia.common.validator.ValidDietaryRestriction;
import com.vallexia.common.validator.ValidMealCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Search criteria for recipe search operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchCriteria {
    
    private String query; // Text search on name/description
    
    @ValidMealCategory
    private SupportedMealCategory category;
    
    @ValidCuisineType
    private SupportedCuisineType cuisineType;
    
    @ValidDietaryRestriction
    private List<SupportedDietaryRestriction> dietaryRestrictions = new ArrayList<>();
    
    private DifficultyLevel difficultyLevel;
    
    private Integer minPrepTime;
    
    private Integer maxPrepTime;
    
    private Integer minCookTime;
    
    private Integer maxCookTime;
    
    private Integer minTotalTime;
    
    private Integer maxTotalTime;
    
    private BigDecimal minCalories;
    
    private BigDecimal maxCalories;
    
    private Integer minServings;
    
    private Integer maxServings;
    
    private RestrictionMatchMode restrictionMatchMode = RestrictionMatchMode.OR;
    
    private Boolean excludeAllergens = true; // Auto-hide recipes with user's allergies
    
    private RecipeSortBy sortBy = RecipeSortBy.CREATED_DATE;
    
    private RecipeSortOrder sortOrder = RecipeSortOrder.DESC;
}
