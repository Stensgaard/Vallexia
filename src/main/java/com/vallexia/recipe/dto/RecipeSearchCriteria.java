package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.DifficultyLevel;
import com.vallexia.recipe.entity.RecipeCategory;
import com.vallexia.user.entity.CuisineType;
import com.vallexia.user.entity.DietaryRestriction;
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
    
    private List<String> ingredients = new ArrayList<>(); // Search by ingredient names
    
    private SortBy sortBy = SortBy.CREATED_DATE;
    
    private SortOrder sortOrder = SortOrder.DESC;
    
    // TODO extract these enums to a separate class
    public enum SortBy {
        NAME,
        CREATED_DATE,
        PREP_TIME,
        COOK_TIME,
        TOTAL_TIME,
        CALORIES,
        SERVINGS
    }
    
    public enum SortOrder {
        ASC,
        DESC
    }
}
