package com.vallexia.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.FavoriteRecipe;
import com.vallexia.recipe.entity.RecipeCache;
import com.vallexia.recipe.exception.RecipeAlreadyFavoritedException;
import com.vallexia.recipe.integration.client.SpoonacularApiClient;
import com.vallexia.recipe.integration.dto.SpoonacularRecipeDto;
import com.vallexia.recipe.integration.exception.SpoonacularApiException;
import com.vallexia.recipe.integration.mapper.SpoonacularMapper;
import com.vallexia.recipe.repository.FavoriteRecipeRepository;
import com.vallexia.user.entity.User;
import com.vallexia.user.service.UserSettingsService;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final UserRepository userRepository;
    private final SpoonacularApiClient spoonacularApiClient;
    private final SpoonacularMapper spoonacularMapper;
    private final RecipeCacheService cacheService;
    private final RecipeLocalizationService recipeLocalizationService;
    private final UserSettingsService userSettingsService;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param favoriteRecipeRepository the favorite recipe repository
     * @param userRepository the user repository
     * @param spoonacularApiClient the Spoonacular API client
     * @param spoonacularMapper the Spoonacular mapper
     * @param cacheService the recipe cache service
     * @param recipeLocalizationService the recipe localization service
     * @param userSettingsService the user settings service
     * @param objectMapper the object mapper for JSON deserialization
     */
    public FavoriteRecipeService(
            FavoriteRecipeRepository favoriteRecipeRepository,
            UserRepository userRepository,
            SpoonacularApiClient spoonacularApiClient,
            SpoonacularMapper spoonacularMapper,
            RecipeCacheService cacheService,
            RecipeLocalizationService recipeLocalizationService,
            UserSettingsService userSettingsService,
            ObjectMapper objectMapper) {
        this.favoriteRecipeRepository = favoriteRecipeRepository;
        this.userRepository = userRepository;
        this.spoonacularApiClient = spoonacularApiClient;
        this.spoonacularMapper = spoonacularMapper;
        this.cacheService = cacheService;
        this.recipeLocalizationService = recipeLocalizationService;
        this.userSettingsService = userSettingsService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Add recipe to user's favorites.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @param userId the user ID
     * @throws SpoonacularApiException if recipe not found in Spoonacular
     */
    public void addFavorite(Integer spoonacularId, Long userId) {
        log.info("Adding recipe ID {} to favorites for user ID {}", spoonacularId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // Check if already favorited
        if (isFavorite(spoonacularId, userId)) {
            log.debug("Recipe ID {} already in favorites for user ID {}", spoonacularId, userId);
            throw new RecipeAlreadyFavoritedException(
                "Recipe with ID " + spoonacularId + " is already in your favorites"
            );
        }
        
        // Verify recipe exists in Spoonacular (will throw exception if not found)
        try {
            spoonacularApiClient.getRecipeById(spoonacularId);
        } catch (SpoonacularApiException e) {
            log.error("Recipe not found in Spoonacular: {}", spoonacularId);
            throw new com.vallexia.recipe.exception.RecipeNotFoundException(
                "Recipe not found with id: " + spoonacularId);
        }
        
        FavoriteRecipe favoriteRecipe = new FavoriteRecipe();
        favoriteRecipe.setUser(user);
        favoriteRecipe.setSpoonacularId(spoonacularId);
        
        favoriteRecipeRepository.save(favoriteRecipe);
        
        log.info("Recipe ID {} added to favorites for user ID {}", spoonacularId, userId);
    }
    
    /**
     * Remove recipe from user's favorites.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @param userId the user ID
     */
    public void removeFavorite(Integer spoonacularId, Long userId) {
        if (spoonacularId == null || userId == null) {
            throw new IllegalArgumentException("Recipe ID and User ID cannot be null");
        }
        
        log.info("Removing recipe ID {} from favorites for user ID {}", spoonacularId, userId);
        
        if (!isFavorite(spoonacularId, userId)) {
            log.debug("Recipe ID {} is not in favorites for user ID {}", spoonacularId, userId);
            return; // Idempotent operation - no error if already removed
        }
        
        favoriteRecipeRepository.deleteByUserIdAndSpoonacularId(userId, spoonacularId);
        
        log.info("Recipe ID {} removed from favorites for user ID {}", spoonacularId, userId);
    }
    
    /**
     * Get user's favorite recipes.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return Page of favorite recipes
     */
    @Transactional(readOnly = true)
    public Page<RecipeDto> getUserFavorites(Long userId, Pageable pageable) {
        log.debug("Getting favorites for user ID {}", userId);
        
        // Get favorites
        Page<FavoriteRecipe> favorites = favoriteRecipeRepository.findByUserId(userId, pageable);
        
        if (favorites.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        // Extract Spoonacular IDs
        List<Integer> spoonacularIds = favorites.stream()
                .map(FavoriteRecipe::getSpoonacularId)
                .collect(Collectors.toList());
        
        // Try to get from cache first, then API
        List<RecipeDto> recipeDtos = new ArrayList<>();
        List<Integer> missingIds = new ArrayList<>();
        
        // Get user locale for translation
        String userLocale = userSettingsService.getUserLocale(userId);
        
        for (Integer id : spoonacularIds) {
            Optional<RecipeCache> cached = cacheService.getCachedRecipe(id);
            if (cached.isPresent()) {
                try {
                    SpoonacularRecipeDto recipeDto = objectMapper
                            .readValue(cached.get().getRecipeData(), SpoonacularRecipeDto.class);
                    RecipeDto dto = spoonacularMapper.toRecipeDto(recipeDto);
                    dto.setIsFavorite(true);
                    
                    // Enrich with translations
                    dto = recipeLocalizationService.enrichWithTranslations(dto, id, userLocale);
                    
                    recipeDtos.add(dto);
                } catch (Exception e) {
                    log.warn("Failed to deserialize cached recipe: {}", id, e);
                    missingIds.add(id);
                }
            } else {
                missingIds.add(id);
            }
        }
        
        // Fetch missing recipes from API
        if (!missingIds.isEmpty()) {
            try {
                List<SpoonacularRecipeDto> apiRecipes = spoonacularApiClient.getRecipesBulk(missingIds);
                for (SpoonacularRecipeDto recipe : apiRecipes) {
                    RecipeDto dto = spoonacularMapper.toRecipeDto(recipe);
                    dto.setIsFavorite(true);
                    
                    // Cache the recipe
                    cacheService.saveRecipe(recipe.getId(), recipe, null);
                    
                    // Enrich with translations
                    dto = recipeLocalizationService.enrichWithTranslations(
                            dto, recipe.getId(), userLocale);
                    
                    recipeDtos.add(dto);
                }
            } catch (SpoonacularApiException e) {
                log.error("Failed to fetch recipes from Spoonacular", e);
            }
        }
        
        return new PageImpl<>(recipeDtos, pageable, favorites.getTotalElements());
    }

    /**
     * Check if recipe is favorited by user.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @param userId the user ID
     * @return true if favorited, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Integer spoonacularId, Long userId) {
        return favoriteRecipeRepository.existsByUserIdAndSpoonacularId(userId, spoonacularId);
    }
}
