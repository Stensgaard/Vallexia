package com.vallexia.recipe.util;

import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.repository.IngredientTranslationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for resolving translated content for ingredients based on user locale.
 * Falls back to base name if translation is not available.
 * 
 * Recipe translations are now handled by RecipeLocalizationService using Google Cloud Translation API.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
@Slf4j
@Component
public class TranslationResolver {
    
    private final IngredientTranslationRepository ingredientTranslationRepository;
    
    public TranslationResolver(IngredientTranslationRepository ingredientTranslationRepository) {
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
}
