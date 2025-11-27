package com.vallexia.recipe.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.FavoriteRecipe;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.exception.RecipeAlreadyFavoritedException;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.FavoriteRecipeRepository;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.recipe.repository.IngredientRepository;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user favorite recipes.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Slf4j
@Service
@Transactional
public class FavoriteRecipeService {
    
    private final FavoriteRecipeRepository favoriteRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;
    private final AuditService auditService;
    private final UserSettingsService userSettingsService;
    private final TranslationResolver translationResolver;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param favoriteRecipeRepository the favorite recipe repository
     * @param recipeRepository the recipe repository
     * @param ingredientRepository the ingredient repository
     * @param userRepository the user repository
     * @param recipeMapper the recipe mapper
     * @param auditService the audit service
     * @param userSettingsService the user settings service (for locale resolution)
     * @param translationResolver the translation resolver
     */
    public FavoriteRecipeService(
            FavoriteRecipeRepository favoriteRecipeRepository,
            RecipeRepository recipeRepository,
            IngredientRepository ingredientRepository,
            UserRepository userRepository,
            RecipeMapper recipeMapper,
            AuditService auditService,
            UserSettingsService userSettingsService,
            TranslationResolver translationResolver) {
        this.favoriteRecipeRepository = favoriteRecipeRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
        this.recipeMapper = recipeMapper;
        this.auditService = auditService;
        this.userSettingsService = userSettingsService;
        this.translationResolver = translationResolver;
    }
    
    /**
     * Add recipe to user's favorites.
     * 
     * @param recipeId the recipe ID
     * @param userId the user ID
     * @throws RecipeNotFoundException if recipe not found
     */
    public void addFavorite(Long recipeId, Long userId) {
        log.info("Adding recipe ID {} to favorites for user ID {}", recipeId, userId);
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + recipeId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // Check if already favorited
        if (favoriteRecipeRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
            log.debug("Recipe ID {} already in favorites for user ID {}", recipeId, userId);
            throw new RecipeAlreadyFavoritedException(
                "Recipe with ID " + recipeId + " is already in your favorites"
            );
        }
        
        FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
        favoriteRecipe.setUser(user);
        favoriteRecipe.setRecipe(recipe);
        
        favoriteRecipeRepository.save(favoriteRecipe);
        
        // Audit log
        auditService.logEvent(
            EventType.RECIPE_FAVORITED,
            userId,
            String.format("Recipe ID %d favorited by user ID %d", recipeId, userId)
        );
        
        log.info("Recipe ID {} added to favorites for user ID {}", recipeId, userId);
    }
    
    /**
     * Remove recipe from user's favorites.
     * 
     * @param recipeId the recipe ID
     * @param userId the user ID
     */
    public void removeFavorite(Long recipeId, Long userId) {
        log.info("Removing recipe ID {} from favorites for user ID {}", recipeId, userId);
        
        favoriteRecipeRepository.deleteByUserIdAndRecipeId(userId, recipeId);
        
        // Audit log
        auditService.logEvent(
            EventType.RECIPE_UNFAVORITED,
            userId,
            String.format("Recipe ID %d unfavorited by user ID %d", recipeId, userId)
        );
        
        log.info("Recipe ID {} removed from favorites for user ID {}", recipeId, userId);
    }
    
    /**
     * Get user's favorite recipes.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return Page of favorite recipes with translated content
     */
    @Transactional(readOnly = true)
    public Page<RecipeDto> getUserFavorites(Long userId, Pageable pageable) {
        log.debug("Getting favorites for user ID {}", userId);
        
        // Get user's locale for translation resolution
        String userLocale = SupportedLocale.EN.getCode(); // Default to English
        try {
            String locale = userSettingsService.getUserSettings(userId).getLanguage();
            if (SupportedLocale.fromCode(locale).isPresent()) {
                userLocale = locale;
            }
        } catch (Exception e) {
            log.debug("Could not fetch user locale for user ID {}: {}", userId, e.getMessage());
        }
        final String finalUserLocale = userLocale;
        
        // Get favorites (no locale filtering)
        Page<FavoriteRecipe> favorites = favoriteRecipeRepository.findByUserId(userId, pageable);
        
        return favorites.map(favorite -> {
            Recipe recipe = favorite.getRecipe();
            RecipeDto dto = recipeMapper.toRecipeDto(recipe, true); // Always true since these are favorites
            
            // Resolve translations
            TranslationResolver.RecipeContent content = translationResolver.resolveRecipeContent(recipe, finalUserLocale);
            dto.setName(content.name());
            dto.setDescription(content.description());
            dto.setInstructions(content.instructions());
            
            // Resolve ingredient names
            if (dto.getIngredients() != null) {
                for (com.vallexia.recipe.dto.IngredientDto ingredientDto : dto.getIngredients()) {
                    if (ingredientDto.getIngredientId() != null) {
                        Ingredient ingredient = ingredientRepository.findById(ingredientDto.getIngredientId())
                            .orElse(null);
                        if (ingredient != null) {
                            String translatedName = translationResolver.resolveIngredientName(ingredient, finalUserLocale);
                            ingredientDto.setName(translatedName);
                        }
                    }
                }
            }
            
            return dto;
        });
    }
    
    /**
     * Check if recipe is favorited by user.
     * 
     * @param recipeId the recipe ID
     * @param userId the user ID
     * @return true if favorited, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long recipeId, Long userId) {
        return favoriteRecipeRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }
}
