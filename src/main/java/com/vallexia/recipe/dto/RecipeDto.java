package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeCategory;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object for recipe responses.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    
    private Long id;
    
    private Long creatorId;
    
    private String creatorUsername;
    
    private String name;
    
    private String description;
    
    private String instructions;
    
    private Integer prepTimeMinutes;
    
    private Integer cookTimeMinutes;
    
    private Integer totalTimeMinutes;
    
    private Integer servings;
    
    private DifficultyLevel difficultyLevel;
    
    private RecipeCategory category;
    
    private CuisineType cuisineType;
    
    private String imageUrl;
    
    private Boolean isPublic;
    
    private String baseLocale;
    
    private NutritionalInfoDto nutritionalInfo;
    
    private List<IngredientDto> ingredients = new ArrayList<>();
    
    private Set<String> tags = new HashSet<>();
    
    private Set<DietaryRestriction> dietaryRestrictions = new HashSet<>();
    
    private Set<Allergy> allergens = new HashSet<>();
    
    private Boolean isFavorite;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
