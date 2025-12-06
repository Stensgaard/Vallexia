package com.vallexia.recipe.util;

import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeTranslation;
import com.vallexia.recipe.repository.IngredientTranslationRepository;
import com.vallexia.recipe.repository.RecipeTranslationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for resolving translated content for recipes and ingredients based on user locale.
 * Falls back to base locale if translation is not available.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Slf4j
@Component
public class TranslationResolver {
    
    private final RecipeTranslationRepository recipeTranslationRepository;
    private final IngredientTranslationRepository ingredientTranslationRepository;
    
    public TranslationResolver(
            RecipeTranslationRepository recipeTranslationRepository,
            IngredientTranslationRepository ingredientTranslationRepository) {
        this.recipeTranslationRepository = recipeTranslationRepository;
        this.ingredientTranslationRepository = ingredientTranslationRepository;
    }
    
    /**
     * Validate and normalize user locale.
     * Returns the validated locale code or defaults to English if invalid.
     * 
     * @param userLocale the user's locale string (can be null, empty, or blank)
     * @return validated locale code, or "en" as default
     */
    private String validateAndNormalizeLocale(String userLocale) {
        return SupportedLocale.fromCode(userLocale)
            .map(SupportedLocale::getCode)
            .orElse(SupportedLocale.EN.getCode());
    }
    
    /**
     * Resolve recipe content (name, description, instructions) for a given locale.
     * Falls back to base locale if translation is not available.
     * 
     * @param recipe the recipe entity
     * @param userLocale the user's preferred locale
     * @return RecipeContent containing resolved name, description, and instructions
     */
    public RecipeContent resolveRecipeContent(Recipe recipe, String userLocale) {
        if (recipe == null) {
            return null;
        }
        
        // Validate user locale
        String locale = validateAndNormalizeLocale(userLocale);
        
        // If user locale matches base locale, return base content
        if (locale.equals(recipe.getBaseLocale())) {
            return new RecipeContent(
                recipe.getName(),
                recipe.getDescription(),
                recipe.getInstructions()
            );
        }
        
        // Try to find translation
        Optional<RecipeTranslation> translation = recipeTranslationRepository
            .findByRecipeIdAndLocale(recipe.getId(), locale);
        
        if (translation.isPresent()) {
            RecipeTranslation t = translation.get();
            return new RecipeContent(
                t.getName(),
                t.getDescription(),
                t.getInstructions()
            );
        }
        
        // Fall back to base locale
        return new RecipeContent(
            recipe.getName(),
            recipe.getDescription(),
            recipe.getInstructions()
        );
    }
    
    /**
     * Resolve ingredient name for a given locale.
     * Falls back to base name if translation is not available.
     * 
     * @param ingredient the ingredient entity
     * @param userLocale the user's preferred locale
     * @return the translated ingredient name or base name
     */
    public String resolveIngredientName(Ingredient ingredient, String userLocale) {
        if (ingredient == null) {
            return null;
        }
        
        // Validate user locale
        String locale = validateAndNormalizeLocale(userLocale);
        
        // Try to find translation
        Optional<com.vallexia.recipe.entity.IngredientTranslation> translation = 
            ingredientTranslationRepository.findByIngredientIdAndLocale(ingredient.getId(), locale);
        
        if (translation.isPresent()) {
            return translation.get().getName();
        }
        
        // Fall back to base name
        return ingredient.getName();
    }
    
    /**
     * Record to hold resolved recipe content.
     */
    public record RecipeContent(String name, String description, String instructions) {}
}
