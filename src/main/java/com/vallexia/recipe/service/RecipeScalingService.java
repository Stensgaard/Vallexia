package com.vallexia.recipe.service;

import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.NutritionalInfoDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.NutritionalInfo;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeIngredient;
import com.vallexia.recipe.exception.InvalidRecipeServingsException;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for scaling recipes to different serving sizes.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RecipeScalingService {
    
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param recipeRepository the recipe repository
     * @param recipeMapper the recipe mapper
     */
    public RecipeScalingService(RecipeRepository recipeRepository, RecipeMapper recipeMapper) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
    }
    
    /**
     * Scale a recipe to a target number of servings.
     * 
     * @param recipeId the recipe ID
     * @param targetServings the target number of servings
     * @param userId the user ID (for favorite check)
     * @return scaled RecipeDto
     * @throws RecipeNotFoundException if recipe not found
     * @throws InvalidRecipeServingsException if target servings is invalid
     */
    public RecipeDto scaleRecipe(Long recipeId, Integer targetServings, Long userId) {
        log.info("Scaling recipe ID {} from current servings to {} servings", recipeId, targetServings);
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + recipeId));
        
        if (targetServings <= 0) {
            throw new InvalidRecipeServingsException(
                "Target servings must be greater than 0, but was: " + targetServings
            );
        }
        
        Integer currentServings = recipe.getServings();
        RecipeDto scaledRecipe = recipeMapper.toRecipeDto(recipe, false); // Favorite check handled separately
        
        // Scale ingredients
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            List<IngredientDto> scaledIngredients = scaleIngredientQuantities(
                    recipe.getIngredients(), 
                    currentServings, 
                    targetServings
            );
            scaledRecipe.setIngredients(scaledIngredients);
        }
        
        // Scale nutritional info
        if (recipe.getNutritionalInfo() != null) {
            NutritionalInfoDto scaledNutrition = scaleNutritionalInfo(
                    recipe.getNutritionalInfo(), 
                    currentServings, 
                    targetServings
            );
            scaledRecipe.setNutritionalInfo(scaledNutrition);
        }
        
        // Update servings
        scaledRecipe.setServings(targetServings);
        
        log.debug("Recipe scaled successfully from {} to {} servings", currentServings, targetServings);
        return scaledRecipe;
    }
    
    /**
     * Scale ingredient quantities proportionally.
     * 
     * @param ingredients list of recipe ingredients
     * @param currentServings current number of servings
     * @param targetServings target number of servings
     * @return list of scaled ingredient DTOs
     */
    public List<IngredientDto> scaleIngredientQuantities(
            List<RecipeIngredient> ingredients, 
            Integer currentServings, 
            Integer targetServings) {
        
        BigDecimal scaleFactor = BigDecimal.valueOf(targetServings)
                .divide(BigDecimal.valueOf(currentServings), DECIMAL_SCALE, ROUNDING_MODE);
        
        return ingredients.stream()
                .map(ri -> {
                    IngredientDto dto = recipeMapper.toIngredientDto(ri);
                    BigDecimal scaledQuantity = ri.getQuantity()
                            .multiply(scaleFactor)
                            .setScale(DECIMAL_SCALE, ROUNDING_MODE);
                    dto.setQuantity(scaledQuantity);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Scale nutritional information proportionally.
     * 
     * @param nutritionalInfo nutritional info to scale
     * @param currentServings current number of servings
     * @param targetServings target number of servings
     * @return scaled nutritional info DTO
     */
    public NutritionalInfoDto scaleNutritionalInfo(
            NutritionalInfo nutritionalInfo, 
            Integer currentServings, 
            Integer targetServings) {
        
        BigDecimal scaleFactor = BigDecimal.valueOf(targetServings)
                .divide(BigDecimal.valueOf(currentServings), DECIMAL_SCALE, ROUNDING_MODE);
        
        NutritionalInfoDto dto = recipeMapper.toNutritionalInfoDto(nutritionalInfo);
        
        // Scale all nutritional values
        if (dto.getCalories() != null) {
            dto.setCalories(dto.getCalories()
                    .multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
        if (dto.getProtein() != null) {
            dto.setProtein(dto.getProtein()
                    .multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
        if (dto.getCarbs() != null) {
            dto.setCarbs(dto.getCarbs()
                    .multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
        if (dto.getFats() != null) {
            dto.setFats(dto.getFats()
                    .multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
        if (dto.getFiber() != null) {
            dto.setFiber(dto.getFiber()
                    .multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
        if (dto.getSodium() != null) {
            dto.setSodium(dto.getSodium()
                    .multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
        if (dto.getSugar() != null) {
            dto.setSugar(dto.getSugar()
                    .multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
        
        return dto;
    }
}
