package com.vallexia.recipe.integration.mapper;

import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.NutritionalInfoDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.integration.dto.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting Spoonacular API DTOs to internal RecipeDto.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Component
public class SpoonacularMapper {
    
    /**
     * Convert SpoonacularRecipeDto to RecipeDto.
     * 
     * @param spoonacularRecipe the Spoonacular recipe DTO
     * @return RecipeDto
     */
    public RecipeDto toRecipeDto(SpoonacularRecipeDto spoonacularRecipe) {
        if (spoonacularRecipe == null) {
            return null;
        }
        
        RecipeDto dto = new RecipeDto();
        dto.setSpoonacularId(spoonacularRecipe.getId());
        dto.setName(spoonacularRecipe.getTitle());
        dto.setDescription(spoonacularRecipe.getSummary()); // Keep HTML for frontend rendering
        dto.setInstructions(formatInstructions(spoonacularRecipe));
        dto.setPrepTimeMinutes(spoonacularRecipe.getPreparationMinutes());
        dto.setCookTimeMinutes(spoonacularRecipe.getCookingMinutes());
        dto.setTotalTimeMinutes(spoonacularRecipe.getReadyInMinutes());
        dto.setServings(spoonacularRecipe.getServings());
        dto.setCategory(mapDishTypeToCategory(spoonacularRecipe.getDishTypes()));
        dto.setCuisineType(mapCuisine(spoonacularRecipe.getCuisines()));
        dto.setImageUrl(spoonacularRecipe.getImage());
        dto.setIngredients(mapIngredients(spoonacularRecipe.getExtendedIngredients()));
        dto.setDietaryRestrictions(mapDiets(spoonacularRecipe.getDiets()));
        dto.setNutritionalInfo(mapNutrition(spoonacularRecipe.getNutrition()));
        dto.setTags(new HashSet<>(spoonacularRecipe.getDishTypes() != null ? 
                spoonacularRecipe.getDishTypes() : List.of()));
        
        return dto;
    }
    
    /**
     * Format instructions from analyzed instructions or plain text.
     * Preserves step-by-step structure for better translation quality.
     * Each step is on a separate line with clear numbering.
     */
    private String formatInstructions(SpoonacularRecipeDto recipe) {
        if (recipe.getAnalyzedInstructions() != null && !recipe.getAnalyzedInstructions().isEmpty()) {
            // Use analyzed instructions with step-by-step structure
            List<String> formattedSteps = new ArrayList<>();
            
            for (SpoonacularAnalyzedInstructionDto instruction : recipe.getAnalyzedInstructions()) {
                // If instruction has a name (e.g., "Cooking Instructions"), include it
                if (instruction.getName() != null && !instruction.getName().isBlank()) {
                    formattedSteps.add(instruction.getName() + ":");
                }
                
                // Add each step with clear numbering (keep HTML for frontend rendering)
                if (instruction.getSteps() != null) {
                    for (SpoonacularStepDto step : instruction.getSteps()) {
                        // Filter out steps with null, empty, or whitespace-only content
                        // Also filter out steps that are just a single digit (likely data quality issues)
                        String stepContent = step.getStep();
                        if (stepContent != null && 
                            !stepContent.isBlank() && 
                            !stepContent.trim().matches("^\\d+$")) { // Exclude steps that are just numbers
                            formattedSteps.add(step.getNumber() + ". " + stepContent);
                        }
                    }
                }
            }
            
            return String.join("\n\n", formattedSteps);
        }
        // Fall back to plain instructions text (keep HTML for frontend rendering)
        return recipe.getInstructions() != null ? recipe.getInstructions() : "";
    }
    
    /**
     * Map dish types to meal category.
     */
    private SupportedMealCategory mapDishTypeToCategory(List<String> dishTypes) {
        if (dishTypes == null || dishTypes.isEmpty()) {
            return null;
        }
        
        for (String dishType : dishTypes) {
            String lower = dishType.toLowerCase();
            if (lower.contains("breakfast") || lower.contains("morning")) {
                return SupportedMealCategory.BREAKFAST;
            } else if (lower.contains("lunch") || lower.contains("main course")) {
                return SupportedMealCategory.LUNCH;
            } else if (lower.contains("dinner") || lower.contains("main course")) {
                return SupportedMealCategory.DINNER;
            } else if (lower.contains("snack") || lower.contains("appetizer")) {
                return SupportedMealCategory.SNACK;
            }
        }
        
        return SupportedMealCategory.LUNCH; // Default
    }
    
    /**
     * Map cuisine string to SupportedCuisineType enum.
     */
    private SupportedCuisineType mapCuisine(List<String> cuisines) {
        if (cuisines == null || cuisines.isEmpty()) {
            return null;
        }
        
        for (String cuisine : cuisines) {
            java.util.Optional<SupportedCuisineType> type = 
                    SupportedCuisineType.fromSpoonacularValue(cuisine);
            if (type.isPresent()) {
                return type.get();
            }
        }
        
        return null;
    }
    
    /**
     * Map ingredients from Spoonacular format.
     * Deduplicates ingredients based on name, quantity, and unit combination.
     * Keeps first occurrence and preserves original order.
     */
    private List<IngredientDto> mapIngredients(List<SpoonacularIngredientDto> extendedIngredients) {
        if (extendedIngredients == null) {
            return new ArrayList<>();
        }
        
        List<IngredientDto> ingredients = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        int order = 0;
        
        for (SpoonacularIngredientDto ingredient : extendedIngredients) {
            // Get ingredient name
            String name = ingredient.getNameClean() != null ? 
                    ingredient.getNameClean() : ingredient.getName();
            if (name == null) {
                name = "";
            }
            
            // Get quantity as string for comparison
            BigDecimal quantity = BigDecimal.valueOf(ingredient.getAmount() != null ? 
                    ingredient.getAmount() : 0.0);
            String quantityStr = quantity.stripTrailingZeros().toPlainString();
            
            // Get unit, treating null as empty string
            String unit = ingredient.getUnit() != null ? ingredient.getUnit() : "";
            
            // Create unique key for deduplication: name|quantity|unit
            String key = name + "|" + quantityStr + "|" + unit;
            
            // Only add if we haven't seen this combination before
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                
                IngredientDto dto = new IngredientDto();
                dto.setIngredientId(ingredient.getId() != null ? ingredient.getId().longValue() : null);
                dto.setName(name);
                dto.setQuantity(quantity);
                dto.setUnit(unit.isEmpty() ? null : unit);
                dto.setNotes(ingredient.getOriginal());
                dto.setDisplayOrder(order++);
                ingredients.add(dto);
            }
        }
        
        return ingredients;
    }
    
    /**
     * Map diets to SupportedDietaryRestriction enum set.
     */
    private Set<SupportedDietaryRestriction> mapDiets(List<String> diets) {
        if (diets == null || diets.isEmpty()) {
            return new HashSet<>();
        }
        
        return diets.stream()
                .map(SupportedDietaryRestriction::fromSpoonacularValue)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toSet());
    }
    
    /**
     * Map nutrition information.
     */
    private NutritionalInfoDto mapNutrition(SpoonacularNutritionDto nutrition) {
        if (nutrition == null || nutrition.getNutrients() == null) {
            return null;
        }
        
        NutritionalInfoDto dto = new NutritionalInfoDto();
        
        for (SpoonacularNutrientDto nutrient : nutrition.getNutrients()) {
            String name = nutrient.getName().toLowerCase();
            Double amount = nutrient.getAmount();
            
            if (amount == null) {
                continue;
            }
            
            if (name.contains("calories") || name.contains("energy")) {
                dto.setCalories(BigDecimal.valueOf(amount));
            } else if (name.contains("protein")) {
                dto.setProtein(BigDecimal.valueOf(amount));
            } else if (name.contains("carbohydrate") || name.contains("carbs")) {
                dto.setCarbs(BigDecimal.valueOf(amount));
            } else if (name.contains("fat")) {
                dto.setFats(BigDecimal.valueOf(amount));
            } else if (name.contains("fiber")) {
                dto.setFiber(BigDecimal.valueOf(amount));
            } else if (name.contains("sodium")) {
                dto.setSodium(BigDecimal.valueOf(amount));
            } else if (name.contains("sugar")) {
                dto.setSugar(BigDecimal.valueOf(amount));
            }
        }
        
        dto.setPerServing(true); // Spoonacular nutrition is per serving
        
        return dto;
    }
}
