package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeCategory;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
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
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    
    private RecipeCategory category;
    
    private CuisineType cuisineType;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    private Boolean isPublic;
    
    @Valid
    private List<IngredientDto> ingredients;
    
    @Valid
    private NutritionalInfoDto nutritionalInfo;
    
    private Set<String> tags;
    
    private Set<DietaryRestriction> dietaryRestrictions;
    
    private Set<Allergy> allergens;
}
