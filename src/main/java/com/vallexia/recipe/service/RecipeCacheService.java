package com.vallexia.recipe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.config.api.SpoonacularProperties;
import com.vallexia.recipe.entity.RecipeCache;
import com.vallexia.recipe.integration.dto.SpoonacularRecipeDto;
import com.vallexia.recipe.repository.RecipeCacheRepository;
import com.vallexia.recipe.repository.RecipeTranslationCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing recipe cache operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional
public class RecipeCacheService {
    
    private final RecipeCacheRepository cacheRepository;
    private final RecipeTranslationCacheRepository translationCacheRepository;
    private final ObjectMapper objectMapper;
    private final SpoonacularProperties properties;
    
    public RecipeCacheService(
            RecipeCacheRepository cacheRepository,
            RecipeTranslationCacheRepository translationCacheRepository,
            ObjectMapper objectMapper,
            SpoonacularProperties properties) {
        this.cacheRepository = cacheRepository;
        this.translationCacheRepository = translationCacheRepository;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }
    
    /**
     * Get cached recipe by Spoonacular ID if not expired.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @return optional recipe cache entry
     */
    @Transactional(readOnly = true)
    public Optional<RecipeCache> getCachedRecipe(Integer spoonacularId) {
        return cacheRepository.findById(spoonacularId)
                .filter(cache -> !cache.isExpired());
    }
    
    /**
     * Get cached search results by search hash if not expired.
     * 
     * @param searchHash the search parameter hash
     * @return list of cached recipes for this search
     */
    @Transactional(readOnly = true)
    public List<RecipeCache> getCachedSearchResults(String searchHash) {
        LocalDateTime now = LocalDateTime.now();
        return cacheRepository.findBySearchHashAndNotExpired(searchHash, now);
    }
    
    /**
     * Save a recipe to cache.
     * Uses REQUIRES_NEW propagation to ensure writes commit even when called from read-only transactions.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @param recipe the recipe DTO
     * @param searchHash optional search hash if this is part of a search result
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRecipe(Integer spoonacularId, SpoonacularRecipeDto recipe, String searchHash) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusHours(properties.getCacheTtlHours());
            
            String recipeJson = objectMapper.writeValueAsString(recipe);
            
            RecipeCache cache = new RecipeCache();
            cache.setSpoonacularId(spoonacularId);
            cache.setSearchHash(searchHash);
            cache.setRecipeData(recipeJson);
            cache.setRecipeName(recipe.getTitle());
            cache.setCuisine(recipe.getCuisines() != null ? 
                    recipe.getCuisines().toArray(new String[0]) : null);
            cache.setDiets(recipe.getDiets() != null ? 
                    recipe.getDiets().toArray(new String[0]) : null);
            cache.setIntolerances(null); // Will be extracted from recipe if needed
            cache.setIngredients(extractIngredientNames(recipe));
            cache.setCachedAt(now);
            cache.setExpiresAt(expiresAt);
            
            cacheRepository.save(cache);
            log.debug("Cached recipe ID: {}", spoonacularId);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize recipe to JSON", e);
            throw new RuntimeException("Failed to cache recipe", e);
        }
    }
    
    /**
     * Save multiple recipes from a search result to cache.
     * Uses REQUIRES_NEW propagation to ensure writes commit even when called from read-only transactions.
     * 
     * @param recipes list of recipes to cache
     * @param searchHash the search parameter hash
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSearchResults(List<SpoonacularRecipeDto> recipes, String searchHash) {
        for (SpoonacularRecipeDto recipe : recipes) {
            saveRecipe(recipe.getId(), recipe, searchHash);
        }
        log.debug("Cached {} recipes for search hash: {}", recipes.size(), searchHash);
    }
    
    /**
     * Search cached recipes by filters.
     * 
     * @param cuisines list of cuisines to filter by
     * @param diets list of diets to filter by
     * @param intolerances list of intolerances to filter by
     * @param ingredients list of ingredients to filter by
     * @return list of matching cached recipes
     */
    @Transactional(readOnly = true)
    public List<RecipeCache> searchCachedRecipes(
            List<String> cuisines,
            List<String> diets,
            List<String> intolerances,
            List<String> ingredients) {
        
        LocalDateTime now = LocalDateTime.now();
        
        // Use empty arrays instead of null to avoid PostgreSQL type inference issues
        String[] cuisineArray = (cuisines != null && !cuisines.isEmpty()) ? 
                cuisines.toArray(new String[0]) : new String[0];
        String[] dietArray = (diets != null && !diets.isEmpty()) ? 
                diets.toArray(new String[0]) : new String[0];
        String[] intoleranceArray = (intolerances != null && !intolerances.isEmpty()) ? 
                intolerances.toArray(new String[0]) : new String[0];
        String[] ingredientArray = (ingredients != null && !ingredients.isEmpty()) ? 
                ingredients.toArray(new String[0]) : new String[0];
        
        return cacheRepository.findMatchingRecipes(
                cuisineArray, dietArray, intoleranceArray, ingredientArray, now);
    }
    
    /**
     * Find all expired cache entries.
     * 
     * @return list of expired entries
     */
    @Transactional(readOnly = true)
    public List<RecipeCache> findExpiredEntries() {
        return cacheRepository.findExpiredEntries(LocalDateTime.now());
    }
    
    /**
     * Clean up expired cache entries and their associated translations.
     * 
     * @return number of recipe cache entries deleted
     */
    public int cleanupExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find expired recipes
        List<RecipeCache> expiredRecipes = cacheRepository.findExpiredEntries(now);
        
        // Delete translations for expired recipes (cascade)
        for (RecipeCache expiredRecipe : expiredRecipes) {
            int deletedTranslations = translationCacheRepository.deleteBySpoonacularId(
                    expiredRecipe.getSpoonacularId());
            if (deletedTranslations > 0) {
                log.debug("Deleted {} translations for expired recipe ID {}", 
                        deletedTranslations, expiredRecipe.getSpoonacularId());
            }
        }
        
        // Delete expired recipe cache entries
        int deleted = cacheRepository.deleteExpiredEntries(now);
        log.info("Cleaned up {} expired recipe cache entries and their translations", deleted);
        return deleted;
    }
    
    /**
     * Clean up expired translation cache entries.
     * 
     * @return number of translation entries deleted
     */
    public int cleanupExpiredTranslations() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = translationCacheRepository.deleteExpiredEntries(now);
        log.debug("Cleaned up {} expired translation cache entries", deleted);
        return deleted;
    }
    
    /**
     * Extract ingredient names from recipe for searchability.
     * 
     * @param recipe the recipe DTO
     * @return array of ingredient names
     */
    private String[] extractIngredientNames(SpoonacularRecipeDto recipe) {
        if (recipe.getExtendedIngredients() == null) {
            return null;
        }
        return recipe.getExtendedIngredients().stream()
                .map(ing -> ing.getNameClean() != null ? ing.getNameClean() : ing.getName())
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}
