package com.vallexia.recipe.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.user.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// TODO rework this class to work with spoonacular API

/**
 * Service for managing recipe CRUD operations and business logic.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Slf4j
@Service
@Transactional
public class RecipeService {
    
    private static final String RECIPE_NOT_FOUND_MSG = "Recipe not found with id: %d";
    
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final FavoriteRecipeService favoriteRecipeService;
    private final UserSettingsService userSettingsService;
    private final RecipeLocalizationService recipeLocalizationService;
    
    /**
     * Constructor for dependency injection.
     */
    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeMapper recipeMapper,
            FavoriteRecipeService favoriteRecipeService,
            UserSettingsService userSettingsService,
            RecipeLocalizationService recipeLocalizationService) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.favoriteRecipeService = favoriteRecipeService;
        this.userSettingsService = userSettingsService;
        this.recipeLocalizationService = recipeLocalizationService;
    }
    
    /**
     * Get recipe by ID.
     * 
     * @param id recipe ID
     * @param userId current user ID (for favorite check and locale resolution)
     * @return recipe DTO with translated content based on user's locale
     * @throws RecipeNotFoundException if recipe not found
     */
    @Transactional(readOnly = true)
    public RecipeDto getRecipeById(Long id, Long userId) {
        log.debug("Getting recipe ID {} for user ID {}", id, userId);
        
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(String.format(RECIPE_NOT_FOUND_MSG, id)));
        
        // Return enriched DTO with translations and favorite status
        return enrichAndMapRecipe(recipe, userId);
    }
    
    /**
     * Get all recipes.
     * 
     * @param pageable pagination information
     * @param userId current user ID (for favorite check and locale resolution, can be null)
     * @return page of recipes with translated content
     */
    @Transactional(readOnly = true)
    public Page<RecipeDto> getRecipes(Pageable pageable, Long userId) {
        log.debug("Getting all recipes for user ID {}", userId);
        
        Page<Recipe> recipes = recipeRepository.findAll(pageable);
        
        return recipes.map(recipe -> enrichAndMapRecipe(recipe, userId));
    }
    
    /**
     * Enrich recipe entity with translations and map to DTO with favorite status.
     * Centralizes the common pattern of checking favorite status, mapping to DTO, and enriching with translations.
     * 
     * @param recipe the recipe entity
     * @param userId the current user ID (can be null)
     * @return enriched RecipeDto with translated content based on user's locale
     */
    private RecipeDto enrichAndMapRecipe(Recipe recipe, Long userId) {
        String userLocale = userSettingsService.getUserLocale(userId);
        boolean isFavorite = userId != null && favoriteRecipeService.isFavorite(recipe.getId(), userId);
        RecipeDto dto = recipeMapper.toRecipeDto(recipe, isFavorite);
        return recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    }
    
}
