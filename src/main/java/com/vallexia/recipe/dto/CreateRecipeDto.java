package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.validator.ValidAllergy;
import com.vallexia.common.validator.ValidCuisineType;
import com.vallexia.common.validator.ValidDietaryRestriction;
import com.vallexia.common.validator.ValidMealCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Transfer Object for creating a new recipe.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeDto {
    
    @NotBlank(message = "Recipe name is required")
    @Size(max = 255, message = "Recipe name must not exceed 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotBlank(message = "Instructions are required")
    private String instructions;
    
    @Min(value = 0, message = "Prep time must be 0 or greater")
    private Integer prepTimeMinutes;
    
    @Min(value = 0, message = "Cook time must be 0 or greater")
    private Integer cookTimeMinutes;
    
    @NotNull(message = "Servings is required")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;
    
    private DifficultyLevel difficultyLevel;
    
    @NotNull(message = "Category is required")
    @ValidMealCategory
    private SupportedMealCategory category;
    
    @ValidCuisineType
    private SupportedCuisineType cuisineType;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    private Boolean isPublic = false;
    
    /**
     * Optional translations for the recipe in different locales.
     * Key is the locale code (e.g., "en", "da"), value is the translation DTO.
     */
    @Valid
    private Map<String, RecipeTranslationDto> translations = new HashMap<>();
    
    @Valid
    private List<IngredientDto> ingredients = new ArrayList<>();
    
    @Valid
    private NutritionalInfoDto nutritionalInfo;
    
    @NotEmpty(message = "At least one tag is required")
    private Set<String> tags = new HashSet<>();
    
    @NotEmpty(message = "At least one dietary restriction is required")
    @ValidDietaryRestriction
    private Set<SupportedDietaryRestriction> dietaryRestrictions = new HashSet<>();
    
    @ValidAllergy
    private Set<SupportedAllergy> allergens = new HashSet<>();
}
