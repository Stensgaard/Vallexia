package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeCategory;
import com.vallexia.recipe.entity.enums.RecipeSortBy;
import com.vallexia.recipe.entity.enums.RecipeSortOrder;
import com.vallexia.recipe.entity.enums.RestrictionMatchMode;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Search criteria for recipe search operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchCriteria {
    
    private String query; // Text search on name/description
    
    private RecipeCategory category;
    
    private CuisineType cuisineType;
    
    private List<DietaryRestriction> dietaryRestrictions = new ArrayList<>();
    
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
