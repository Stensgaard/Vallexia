package com.vallexia.recipe.service;

import com.vallexia.nutrition.service.NutritionalCalculator;
import com.vallexia.recipe.entity.IngredientNutrition;
import com.vallexia.recipe.entity.NutritionalInfo;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeIngredient;
import com.vallexia.recipe.exception.RecipeValidationException;
import com.vallexia.recipe.repository.IngredientNutritionRepository;
import com.vallexia.recipe.repository.NutritionalInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Service for calculating nutritional information for recipes.
 * Extends existing NutritionalCalculator functionality.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class NutritionalCalculationService {
    
    private final NutritionalCalculator nutritionalCalculator;
    private final NutritionalInfoRepository nutritionalInfoRepository;
    private final IngredientNutritionRepository ingredientNutritionRepository;
    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param nutritionalCalculator the nutritional calculator
     * @param nutritionalInfoRepository the nutritional info repository
     * @param ingredientNutritionRepository the ingredient nutrition repository
     */
    public NutritionalCalculationService(
            NutritionalCalculator nutritionalCalculator,
            NutritionalInfoRepository nutritionalInfoRepository,
            IngredientNutritionRepository ingredientNutritionRepository) {
        this.nutritionalCalculator = nutritionalCalculator;
        this.nutritionalInfoRepository = nutritionalInfoRepository;
        this.ingredientNutritionRepository = ingredientNutritionRepository;
    }
    
    /**
     * Calculate recipe nutrition from ingredient nutritional data.
     * Fetches nutritional information for each ingredient and aggregates based on quantities.
     * 
     * @param ingredients list of recipe ingredients
     * @return aggregated NutritionalInfo
     */
    public NutritionalInfo calculateRecipeNutrition(List<RecipeIngredient> ingredients) {
        log.debug("Calculating nutrition for {} ingredients", ingredients.size());
        
        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFats = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;
        BigDecimal totalSodium = BigDecimal.ZERO;
        BigDecimal totalSugar = BigDecimal.ZERO;
        
        for (RecipeIngredient recipeIngredient : ingredients) {
            if (recipeIngredient.getIngredient() == null || recipeIngredient.getQuantity() == null) {
                // TODO maybe add which ingredient is null and was skipped?
                log.debug("Skipping ingredient with null data");
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
            BigDecimal quantityInGrams = convertToGrams(quantity, unit, nutrition);
            
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
                BigDecimal calculatedCaloriesFromMacros = nutritionalCalculator.calculateCaloriesFromMacros(
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
    }
    
    /**
     * Convert ingredient quantity to grams based on unit and conversion factor.
     * 
     * @param quantity the quantity
     * @param unit the unit (e.g., "g", "kg", "cup", "tsp")
     * @param nutrition the ingredient nutrition with conversion factor
     * @return quantity in grams
     */
    private BigDecimal convertToGrams(BigDecimal quantity, String unit, IngredientNutrition nutrition) {
        // If already in grams
        if ("g".equals(unit) || "gram".equals(unit) || "grams".equals(unit)) {
            return quantity;
        }
        
        // If unit matches the standard unit and has conversion factor
        if (nutrition.getStandardUnit() != null && 
            nutrition.getStandardUnit().equalsIgnoreCase(unit) && 
            nutrition.getConversionFactorToGrams() != null) {
            return quantity.multiply(nutrition.getConversionFactorToGrams());
        }
        
        // Common unit conversions (fallback)
        switch (unit.toLowerCase()) {
            case "kg":
            case "kilogram":
            case "kilograms":
                return quantity.multiply(BigDecimal.valueOf(1000));
            case "mg":
            case "milligram":
            case "milligrams":
                return quantity.divide(BigDecimal.valueOf(1000), DECIMAL_SCALE, ROUNDING_MODE);
            case "oz":
            case "ounce":
            case "ounces":
                return quantity.multiply(BigDecimal.valueOf(28.35)); // 1 oz = 28.35g
            case "lb":
            case "pound":
            case "pounds":
                return quantity.multiply(BigDecimal.valueOf(453.59)); // 1 lb = 453.59g
            case "ml":
            case "milliliter":
            case "milliliters":
                // For liquids, assume 1ml = 1g (approximation)
                return quantity;
            case "l":
            case "liter":
            case "liters":
                return quantity.multiply(BigDecimal.valueOf(1000)); // 1L = 1000ml = 1000g (approx)
            default:
                // If no conversion available and no conversion factor, assume grams
                log.warn("Unknown unit '{}', assuming grams", unit);
                return quantity;
        }
    }
    
    /**
     * Calculate per-serving nutritional information from total values.
     * 
     * @param totalInfo total nutritional info
     * @param servings number of servings
     * @return per-serving nutritional info
     */
    public NutritionalInfo calculatePerServingNutrition(NutritionalInfo totalInfo, Integer servings) {
        if (servings == null || servings <= 0) {
            // TODO throw an custom exception instead of IllegalArgumentException
            throw new IllegalArgumentException("Servings must be greater than 0");
        }
        
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
    }
    
    /**
     * Update recipe nutritional information.
     * Recalculates and saves nutritional info for a recipe.
     * 
     * @param recipe the recipe to update
     */
    public void updateRecipeNutrition(Recipe recipe) {
        log.debug("Updating nutrition for recipe ID {}", recipe.getId());
        
        // If recipe has ingredients, calculate from them
        // Otherwise, keep existing nutritional info if present
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            NutritionalInfo calculatedInfo = calculateRecipeNutrition(recipe.getIngredients());
            calculatedInfo.setRecipe(recipe);
            
            // Check if nutritional info already exists
            NutritionalInfo existingInfo = recipe.getNutritionalInfo();
            if (existingInfo != null) {
                // Update existing
                existingInfo.setCalories(calculatedInfo.getCalories());
                existingInfo.setProtein(calculatedInfo.getProtein());
                existingInfo.setCarbs(calculatedInfo.getCarbs());
                existingInfo.setFats(calculatedInfo.getFats());
                existingInfo.setFiber(calculatedInfo.getFiber());
                existingInfo.setSodium(calculatedInfo.getSodium());
                existingInfo.setSugar(calculatedInfo.getSugar());
                nutritionalInfoRepository.save(existingInfo);
            } else {
                // Create new
                nutritionalInfoRepository.save(calculatedInfo);
            }
        }
        
        log.debug("Nutrition updated for recipe ID {}", recipe.getId());
    }
}
