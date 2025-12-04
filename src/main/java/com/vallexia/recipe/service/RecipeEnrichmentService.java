package com.vallexia.recipe.service;

import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.repository.IngredientRepository;
import com.vallexia.recipe.util.TranslationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for enriching RecipeDto with translations and ingredient names.
 * Handles batch loading of ingredients to avoid N+1 query problems.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Slf4j
@Service
public class RecipeEnrichmentService {
    
    private final TranslationResolver translationResolver;
    private final IngredientRepository ingredientRepository;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param translationResolver the translation resolver
     * @param ingredientRepository the ingredient repository
     */
    public RecipeEnrichmentService(
            TranslationResolver translationResolver,
            IngredientRepository ingredientRepository) {
        this.translationResolver = translationResolver;
        this.ingredientRepository = ingredientRepository;
    }
    
    /**
     * Enrich RecipeDto with translations and ingredient names for a given locale.
     * 
     * @param dto the recipe DTO to enrich
     * @param recipe the recipe entity
     * @param userLocale the user's locale
     * @return enriched RecipeDto
     */
    public RecipeDto enrichWithTranslations(RecipeDto dto, Recipe recipe, String userLocale) {
        // Resolve recipe content translations
        TranslationResolver.RecipeContent content = translationResolver.resolveRecipeContent(recipe, userLocale);
        dto.setName(content.name());
        dto.setDescription(content.description());
        dto.setInstructions(content.instructions());
        
        // Resolve ingredient names (optimized batch loading)
        enrichIngredientNames(dto, userLocale);
        
        return dto;
    }
    
    /**
     * Enrich ingredient names in RecipeDto with translations.
     * Uses batch loading to avoid N+1 queries.
     * 
     * @param dto the recipe DTO containing ingredients
     * @param userLocale the user's locale
     */
    private void enrichIngredientNames(RecipeDto dto, String userLocale) {
        if (dto.getIngredients() == null || dto.getIngredients().isEmpty()) {
            return;
        }
        
        // Collect ingredient IDs
        List<Long> ingredientIds = dto.getIngredients().stream()
            .map(IngredientDto::getIngredientId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        
        if (ingredientIds.isEmpty()) {
            return;
        }
        
        // Batch load all ingredients
        Map<Long, Ingredient> ingredientsMap = ingredientRepository.findAllById(ingredientIds)
            .stream()
            .collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        
        // Apply translations
        for (IngredientDto ingredientDto : dto.getIngredients()) {
            if (ingredientDto.getIngredientId() != null) {
                Ingredient ingredient = ingredientsMap.get(ingredientDto.getIngredientId());
                if (ingredient != null) {
                    String translatedName = translationResolver.resolveIngredientName(ingredient, userLocale);
                    ingredientDto.setName(translatedName);
                }
            }
        }
    }
}
