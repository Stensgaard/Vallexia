package com.vallexia.recipe.dto;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.enums.SupportedMealCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object for recipe responses.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    
    private Integer spoonacularId;
    
    private String name;
    
    private String description;
    
    private String instructions;
    
    private Integer prepTimeMinutes;
    
    private Integer cookTimeMinutes;
    
    private Integer totalTimeMinutes;
    
    private Integer servings;
    
    private SupportedMealCategory category;
    
    private SupportedCuisineType cuisineType;
    
    private String imageUrl;
    
    private String baseLocale;
    
    private NutritionalInfoDto nutritionalInfo;
    
    private List<IngredientDto> ingredients = new ArrayList<>();
    
    private Set<String> tags = new HashSet<>();
    
    private Set<SupportedDietaryRestriction> dietaryRestrictions = new HashSet<>();
    
    private Set<SupportedAllergy> allergens = new HashSet<>();
    
    private Boolean isFavorite;
}
