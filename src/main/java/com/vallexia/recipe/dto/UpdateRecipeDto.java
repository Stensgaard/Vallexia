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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object for updating an existing recipe.
 * All fields are optional for partial updates.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeDto {
    
    @Size(max = 255, message = "Recipe name must not exceed 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    private String instructions;
    
    @Min(value = 0, message = "Prep time must be 0 or greater")
    private Integer prepTimeMinutes;
    
    @Min(value = 0, message = "Cook time must be 0 or greater")
    private Integer cookTimeMinutes;
    
    @Min(value = 0, message = "Total time must be 0 or greater")
    private Integer totalTimeMinutes;
    
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;
    
    private DifficultyLevel difficultyLevel;
    
    @ValidMealCategory
    private SupportedMealCategory category;
    
    @ValidCuisineType
    private SupportedCuisineType cuisineType;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    private Boolean isPublic;
    
    @Valid
    private List<IngredientDto> ingredients;
    
    @Valid
    private NutritionalInfoDto nutritionalInfo;
    
    private Set<String> tags;
    
    @ValidDietaryRestriction
    private Set<SupportedDietaryRestriction> dietaryRestrictions;
    
    @ValidAllergy
    private Set<SupportedAllergy> allergens;
}
