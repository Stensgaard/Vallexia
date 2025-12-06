package com.vallexia.recipe.service;

import com.vallexia.nutrition.exception.NutritionalCalculationException;
import com.vallexia.nutrition.service.MacroCalculator;
import com.vallexia.recipe.entity.IngredientNutrition;
import com.vallexia.recipe.entity.NutritionalInfo;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeIngredient;
import com.vallexia.recipe.exception.InvalidRecipeServingsException;
import com.vallexia.recipe.exception.RecipeValidationException;
import com.vallexia.recipe.repository.IngredientNutritionRepository;
import com.vallexia.recipe.repository.NutritionalInfoRepository;
import com.vallexia.recipe.util.UnitConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.vallexia.nutrition.util.NutritionalConstants.*;

/**
 * Service for calculating and managing nutritional information for recipes.
 * Handles recipe-specific nutrition calculations including ingredient aggregation,
 * unit conversions, and per-serving calculations.
 * 
 * <p>This service uses {@link MacroCalculator} for macro-based calorie calculations
 * and validation.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Slf4j
@Service
@Transactional
public class RecipeNutritionService {
    
    private final MacroCalculator macroCalculator;
    private final NutritionalInfoRepository nutritionalInfoRepository;
    private final IngredientNutritionRepository ingredientNutritionRepository;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param macroCalculator the macro calculator for calorie calculations
     * @param nutritionalInfoRepository the nutritional info repository
     * @param ingredientNutritionRepository the ingredient nutrition repository
     */
    public RecipeNutritionService(
            MacroCalculator macroCalculator,
            NutritionalInfoRepository nutritionalInfoRepository,
            IngredientNutritionRepository ingredientNutritionRepository) {
        this.macroCalculator = macroCalculator;
        this.nutritionalInfoRepository = nutritionalInfoRepository;
        this.ingredientNutritionRepository = ingredientNutritionRepository;
    }
    
    /**
     * Calculate recipe nutrition from ingredient nutritional data.
     * Fetches nutritional information for each ingredient and aggregates based on quantities.
     * 
     * @param ingredients list of recipe ingredients
     * @return aggregated NutritionalInfo
     * @throws RecipeValidationException if no nutrition data is available for ingredients
     * @throws NutritionalCalculationException if calculation fails due to arithmetic error
     */
    @Transactional(readOnly = true)
    public NutritionalInfo calculateRecipeNutrition(List<RecipeIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            log.debug("No ingredients provided for nutrition calculation");
            throw new RecipeValidationException(
                "Cannot calculate nutrition: ingredients list is null or empty"
            );
        }
        log.debug("Calculating nutrition for {} ingredients", ingredients.size());
        
        try {
            BigDecimal totalCalories = BigDecimal.ZERO;
            BigDecimal totalProtein = BigDecimal.ZERO;
            BigDecimal totalCarbs = BigDecimal.ZERO;
            BigDecimal totalFats = BigDecimal.ZERO;
            BigDecimal totalFiber = BigDecimal.ZERO;
            BigDecimal totalSodium = BigDecimal.ZERO;
            BigDecimal totalSugar = BigDecimal.ZERO;
            
            for (RecipeIngredient recipeIngredient : ingredients) {
                if (recipeIngredient.getIngredient() == null || recipeIngredient.getQuantity() == null) {
                    // Build informative log message with available data
                    String ingredientInfo = recipeIngredient.getId() != null 
                        ? "RecipeIngredient ID: " + recipeIngredient.getId()
                        : "RecipeIngredient (unsaved)";
                    String missingField = recipeIngredient.getIngredient() == null 
                        ? "ingredient" 
                        : "quantity";
                    StringBuilder details = new StringBuilder();
                    if (recipeIngredient.getUnit() != null) {
                        details.append(", unit: ").append(recipeIngredient.getUnit());
                    }
                    if (recipeIngredient.getDisplayOrder() != null) {
                        details.append(", display order: ").append(recipeIngredient.getDisplayOrder());
                    }
                    
                    log.debug("Skipping {} - missing {} field{}", 
                        ingredientInfo, missingField, details.toString());
                    continue;
                }
                
                Long ingredientId = recipeIngredient.getIngredient().getId();
                Optional<IngredientNutrition> nutritionOpt = ingredientNutritionRepository.findByIngredientId(ingredientId);
                
                if (nutritionOpt.isEmpty()) {
                    log.debug("No nutrition data found for ingredient ID: {}", ingredientId);
                    continue;
                }
                
                IngredientNutrition nutrition = nutritionOpt.get();
                BigDecimal quantity = recipeIngredient.getQuantity();
                String unit = recipeIngredient.getUnit() != null ? recipeIngredient.getUnit().toLowerCase() : "g";
                
                // Convert quantity to grams for calculation
                // Wrap in try-catch to handle invalid units gracefully - skip ingredient instead of failing entire calculation
                BigDecimal quantityInGrams;
                try {
                    quantityInGrams = convertToGrams(quantity, unit, nutrition);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping ingredient ID {} due to invalid unit '{}': {}", 
                        ingredientId, unit, e.getMessage());
                    continue;
                }
                
                // Calculate nutrition for this ingredient quantity (nutrition is per 100g)
                BigDecimal multiplier = quantityInGrams.divide(BigDecimal.valueOf(100), DECIMAL_SCALE, ROUNDING_MODE);
                
                if (nutrition.getCaloriesPer100g() != null) {
                    totalCalories = totalCalories.add(nutrition.getCaloriesPer100g().multiply(multiplier));
                }
                if (nutrition.getProteinPer100g() != null) {
                    totalProtein = totalProtein.add(nutrition.getProteinPer100g().multiply(multiplier));
                }
                if (nutrition.getCarbsPer100g() != null) {
                    totalCarbs = totalCarbs.add(nutrition.getCarbsPer100g().multiply(multiplier));
                }
                if (nutrition.getFatsPer100g() != null) {
                    totalFats = totalFats.add(nutrition.getFatsPer100g().multiply(multiplier));
                }
                if (nutrition.getFiberPer100g() != null) {
                    totalFiber = totalFiber.add(nutrition.getFiberPer100g().multiply(multiplier));
                }
                if (nutrition.getSodiumPer100g() != null) {
                    totalSodium = totalSodium.add(nutrition.getSodiumPer100g().multiply(multiplier));
                }
                if (nutrition.getSugarPer100g() != null) {
                    totalSugar = totalSugar.add(nutrition.getSugarPer100g().multiply(multiplier));
                }
            }
            
            // Validate that we have nutrition data - calories must be > 0
            if (totalCalories.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RecipeValidationException(
                    "Cannot calculate nutrition: no nutrition data available for recipe ingredients. " +
                    "Please ensure all ingredients have nutrition data in the database."
                );
            }
            
            NutritionalInfo nutritionalInfo = new NutritionalInfo();
            nutritionalInfo.setCalories(totalCalories.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            nutritionalInfo.setProtein(totalProtein.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            nutritionalInfo.setCarbs(totalCarbs.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            nutritionalInfo.setFats(totalFats.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            nutritionalInfo.setFiber(totalFiber.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            nutritionalInfo.setSodium(totalSodium.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            nutritionalInfo.setSugar(totalSugar.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            nutritionalInfo.setPerServing(false); // Total values
            
            // Validate calculated calories against macro-based calculation (sanity check)
            // This helps catch data inconsistencies between ingredient nutrition data
            if (totalProtein != null && totalCarbs != null && totalFats != null 
                && totalCalories.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    BigDecimal calculatedCaloriesFromMacros = macroCalculator.calculateCaloriesFromMacros(
                        totalProtein, totalCarbs, totalFats);
                    
                    BigDecimal difference = totalCalories.subtract(calculatedCaloriesFromMacros).abs();
                    BigDecimal percentDifference = difference
                        .divide(totalCalories, DECIMAL_SCALE, ROUNDING_MODE)
                        .multiply(BigDecimal.valueOf(100));
                    
                    // Warn if difference is significant (>5%)
                    if (percentDifference.compareTo(BigDecimal.valueOf(5)) > 0) {
                        log.warn("Nutrition data inconsistency detected: Calculated calories ({}) differ from " +
                                "macro-based calculation ({}) by {}%", 
                                totalCalories, calculatedCaloriesFromMacros, percentDifference);
                    } else {
                        log.debug("Nutrition validation passed: Calories match macro calculation within acceptable range");
                    }
                } catch (Exception e) {
                    log.warn("Unable to validate nutrition calculation: {}", e.getMessage());
                }
            }
            
            log.debug("Nutrition calculation completed: {} calories, {}g protein, {}g carbs, {}g fats",
                    totalCalories, totalProtein, totalCarbs, totalFats);
            return nutritionalInfo;
        } catch (ArithmeticException e) {
            throw new NutritionalCalculationException(
                "Failed to calculate recipe nutrition due to arithmetic error", e
            );
        }
    }
    
    /**
     * Convert ingredient quantity to grams based on unit and conversion factor.
     * Uses UnitConversionUtil for standard conversions, with fallback to ingredient-specific factors.
     * 
     * @param quantity the quantity
     * @param unit the unit (e.g., "g", "kg", "cup", "tsp")
     * @param nutrition the ingredient nutrition with conversion factor
     * @return quantity in grams
     */
    private BigDecimal convertToGrams(BigDecimal quantity, String unit, IngredientNutrition nutrition) {
        if (quantity == null || unit == null) {
            return quantity;
        }
        
        // If unit matches the standard unit and has ingredient-specific conversion factor, use it
        // This takes precedence over standard unit conversions for ingredient-specific cases
        if (nutrition.getStandardUnit() != null && 
            nutrition.getStandardUnit().equalsIgnoreCase(unit) && 
            nutrition.getConversionFactorToGrams() != null) {
            return quantity.multiply(nutrition.getConversionFactorToGrams());
        }
        
        // Try standard weight unit conversion first
        if (UnitConversionUtil.isWeightUnit(unit)) {
            try {
                return UnitConversionUtil.convertToGrams(quantity, unit);
            } catch (IllegalArgumentException e) {
                // Invalid weight unit - log warning and assume grams
                log.warn("Invalid weight unit '{}' for ingredient nutrition calculation, assuming grams. Error: {}", 
                    unit, e.getMessage());
                return quantity;
            }
        }
        
        // For volume units, convert to milliliters then approximate as grams.
        // NOTE: This approximation (1ml ≈ 1g) is only accurate for water at standard temperature.
        // For other liquids (oils, syrups, etc.), this may introduce calculation errors.
        // TODO: Consider implementing ingredient-specific density factors for more accurate conversions.
        if (UnitConversionUtil.isVolumeUnit(unit)) {
            try {
                BigDecimal milliliters = UnitConversionUtil.convertToMilliliters(quantity, unit);
                // Approximate 1ml = 1g (water density approximation)
                return milliliters;
            } catch (IllegalArgumentException e) {
                // Invalid volume unit - log warning and assume grams
                log.warn("Invalid volume unit '{}' for ingredient nutrition calculation, assuming grams. Error: {}", 
                    unit, e.getMessage());
                return quantity;
            }
        }
        
        // Unknown unit - log warning and assume grams
        log.warn("Unknown unit '{}' for ingredient nutrition calculation, assuming grams", unit);
        return quantity;
    }
    
    /**
     * Calculate per-serving nutritional information from total values.
     * 
     * @param totalInfo total nutritional info
     * @param servings number of servings
     * @return per-serving nutritional info
     * @throws InvalidRecipeServingsException if servings is invalid
     * @throws NutritionalCalculationException if calculation fails due to arithmetic error
     */
    @Transactional(readOnly = true)
    public NutritionalInfo calculatePerServingNutrition(NutritionalInfo totalInfo, Integer servings) {
        if (servings == null || servings <= 0) {
            throw new InvalidRecipeServingsException(
                "Servings must be greater than 0, but was: " + servings
            );
        }
        
        try {
            BigDecimal servingsBigDecimal = BigDecimal.valueOf(servings);
            
            NutritionalInfo perServingInfo = new NutritionalInfo();
            
            if (totalInfo.getCalories() != null) {
                perServingInfo.setCalories(
                        totalInfo.getCalories()
                                .divide(servingsBigDecimal, DECIMAL_SCALE, ROUNDING_MODE)
                );
            }
            if (totalInfo.getProtein() != null) {
                perServingInfo.setProtein(
                        totalInfo.getProtein()
                                .divide(servingsBigDecimal, DECIMAL_SCALE, ROUNDING_MODE)
                );
            }
            if (totalInfo.getCarbs() != null) {
                perServingInfo.setCarbs(
                        totalInfo.getCarbs()
                                .divide(servingsBigDecimal, DECIMAL_SCALE, ROUNDING_MODE)
                );
            }
            if (totalInfo.getFats() != null) {
                perServingInfo.setFats(
                        totalInfo.getFats()
                                .divide(servingsBigDecimal, DECIMAL_SCALE, ROUNDING_MODE)
                );
            }
            if (totalInfo.getFiber() != null) {
                perServingInfo.setFiber(
                        totalInfo.getFiber()
                                .divide(servingsBigDecimal, DECIMAL_SCALE, ROUNDING_MODE)
                );
            }
            if (totalInfo.getSodium() != null) {
                perServingInfo.setSodium(
                        totalInfo.getSodium()
                                .divide(servingsBigDecimal, DECIMAL_SCALE, ROUNDING_MODE)
                );
            }
            if (totalInfo.getSugar() != null) {
                perServingInfo.setSugar(
                        totalInfo.getSugar()
                                .divide(servingsBigDecimal, DECIMAL_SCALE, ROUNDING_MODE)
                );
            }
            
            perServingInfo.setPerServing(true);
            
            return perServingInfo;
        } catch (ArithmeticException e) {
            throw new NutritionalCalculationException(
                "Failed to calculate per-serving nutrition due to arithmetic error", e
            );
        }
    }
    
    /**
     * Update recipe nutritional information.
     * Recalculates and saves nutritional info for a recipe.
     * Automatically converts total nutrition to per-serving values.
     * 
     * @param recipe the recipe to update
     */
    public void updateRecipeNutrition(Recipe recipe) {
        log.debug("Updating nutrition for recipe ID {}", recipe.getId());
        
        // If recipe has ingredients, calculate from them
        // Otherwise, keep existing nutritional info if present
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            NutritionalInfo calculatedTotalInfo = calculateRecipeNutrition(recipe.getIngredients());
            
            // Convert to per-serving nutrition
            Integer servings = recipe.getServings() != null && recipe.getServings() > 0 
                ? recipe.getServings() 
                : 1; // Default to 1 serving if not specified
            
            NutritionalInfo perServingInfo = calculatePerServingNutrition(calculatedTotalInfo, servings);
            perServingInfo.setRecipe(recipe);
            
            // Check if nutritional info already exists
            NutritionalInfo existingInfo = recipe.getNutritionalInfo();
            if (existingInfo != null) {
                // Update existing
                existingInfo.setCalories(perServingInfo.getCalories());
                existingInfo.setProtein(perServingInfo.getProtein());
                existingInfo.setCarbs(perServingInfo.getCarbs());
                existingInfo.setFats(perServingInfo.getFats());
                existingInfo.setFiber(perServingInfo.getFiber());
                existingInfo.setSodium(perServingInfo.getSodium());
                existingInfo.setSugar(perServingInfo.getSugar());
                existingInfo.setPerServing(true); // Mark as per serving
                nutritionalInfoRepository.save(existingInfo);
            } else {
                // Create new
                nutritionalInfoRepository.save(perServingInfo);
            }
        }
        
        log.debug("Nutrition updated for recipe ID {} (per serving)", recipe.getId());
    }
}
