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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vallexia.nutrition.util.NutritionalConstants.*;

// TODO Rework this class to work with spoonacular API?
// TODO why is favorite recipe needed here?

/**
 * Service for scaling recipes to different serving sizes.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RecipeScalingService {
    
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final FavoriteRecipeService favoriteRecipeService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param recipeRepository the recipe repository
     * @param recipeMapper the recipe mapper
     * @param favoriteRecipeService the favorite recipe service
     */
    public RecipeScalingService(
            RecipeRepository recipeRepository, 
            RecipeMapper recipeMapper,
            FavoriteRecipeService favoriteRecipeService) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.favoriteRecipeService = favoriteRecipeService;
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
        if (currentServings == null || currentServings <= 0) {
            throw new InvalidRecipeServingsException(
                "Recipe has invalid servings: " + currentServings
            );
        }
        
        boolean isFavorite = userId != null && favoriteRecipeService.isFavorite(recipeId, userId);
        RecipeDto scaledRecipe = recipeMapper.toRecipeDto(recipe, isFavorite);
        
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
     * Calculate the scale factor for scaling between serving sizes.
     * 
     * @param currentServings current number of servings
     * @param targetServings target number of servings
     * @return scale factor as BigDecimal
     */
    private BigDecimal calculateScaleFactor(Integer currentServings, Integer targetServings) {
        return BigDecimal.valueOf(targetServings)
                .divide(BigDecimal.valueOf(currentServings), DECIMAL_SCALE, ROUNDING_MODE);
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
        
        BigDecimal scaleFactor = calculateScaleFactor(currentServings, targetServings);
        
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
        
        BigDecimal scaleFactor = calculateScaleFactor(currentServings, targetServings);
        NutritionalInfoDto dto = recipeMapper.toNutritionalInfoDto(nutritionalInfo);
        
        // Scale all nutritional values
        scaleNutritionalValue(NutritionalInfoDto::getCalories, NutritionalInfoDto::setCalories, dto, scaleFactor);
        scaleNutritionalValue(NutritionalInfoDto::getProtein, NutritionalInfoDto::setProtein, dto, scaleFactor);
        scaleNutritionalValue(NutritionalInfoDto::getCarbs, NutritionalInfoDto::setCarbs, dto, scaleFactor);
        scaleNutritionalValue(NutritionalInfoDto::getFats, NutritionalInfoDto::setFats, dto, scaleFactor);
        scaleNutritionalValue(NutritionalInfoDto::getFiber, NutritionalInfoDto::setFiber, dto, scaleFactor);
        scaleNutritionalValue(NutritionalInfoDto::getSodium, NutritionalInfoDto::setSodium, dto, scaleFactor);
        scaleNutritionalValue(NutritionalInfoDto::getSugar, NutritionalInfoDto::setSugar, dto, scaleFactor);
        
        return dto;
    }
    
    /**
     * Scale a single nutritional value by the scale factor.
     * 
     * @param getter function to get the nutritional value
     * @param setter function to set the nutritional value
     * @param dto the nutritional info DTO
     * @param scaleFactor the scale factor to apply
     */
    private void scaleNutritionalValue(
            Function<NutritionalInfoDto, BigDecimal> getter,
            BiConsumer<NutritionalInfoDto, BigDecimal> setter,
            NutritionalInfoDto dto,
            BigDecimal scaleFactor) {
        BigDecimal value = getter.apply(dto);
        if (value != null) {
            setter.accept(dto, value.multiply(scaleFactor)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE));
        }
    }
}
