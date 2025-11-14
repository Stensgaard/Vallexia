package com.vallexia.recipe.dto;

import com.vallexia.recipe.entity.DifficultyLevel;
import com.vallexia.recipe.entity.RecipeCategory;
import com.vallexia.user.entity.CuisineType;
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
    
    private NutritionalInfoDto nutritionalInfo;
    
    private List<IngredientDto> ingredients = new ArrayList<>();
    
    private Set<String> tags = new HashSet<>();
    
    private Boolean isFavorite;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
