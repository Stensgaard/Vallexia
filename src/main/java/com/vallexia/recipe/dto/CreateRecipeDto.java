package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeCategory;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object for creating a new recipe.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
    private RecipeCategory category;
    
    private CuisineType cuisineType;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    private Boolean isPublic = false;
    
    @Valid
    private List<IngredientDto> ingredients = new ArrayList<>();
    
    @Valid
    private NutritionalInfoDto nutritionalInfo;
    
    @NotEmpty(message = "At least one tag is required")
    private Set<String> tags = new HashSet<>();
    
    @NotEmpty(message = "At least one dietary restriction is required")
    private Set<DietaryRestriction> dietaryRestrictions = new HashSet<>();
    
    private Set<Allergy> allergens = new HashSet<>();
}
