package com.vallexia.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.RecipeCache;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.integration.client.SpoonacularApiClient;
import com.vallexia.recipe.integration.dto.SpoonacularRecipeDto;
import com.vallexia.recipe.integration.dto.SpoonacularSearchParams;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.recipe.integration.dto.SpoonacularSearchResponseDto;
import com.vallexia.recipe.integration.exception.SpoonacularApiException;
import com.vallexia.recipe.integration.mapper.SpoonacularMapper;
import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.service.DietaryPreferencesService;
import com.vallexia.user.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing recipe operations with Spoonacular API integration.
 * Recipes are read-only and sourced from Spoonacular API with caching.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional
public class RecipeService {
    
    private final SpoonacularApiClient spoonacularApiClient;
    private final SpoonacularMapper spoonacularMapper;
    private final RecipeCacheService cacheService;
    private final FavoriteRecipeService favoriteRecipeService;
    private final DietaryPreferencesService dietaryPreferencesService;
    private final RecipeLocalizationService recipeLocalizationService;
    private final UserSettingsService userSettingsService;
    private final ObjectMapper objectMapper;
    
    public RecipeService(
            SpoonacularApiClient spoonacularApiClient,
            SpoonacularMapper spoonacularMapper,
            RecipeCacheService cacheService,
            FavoriteRecipeService favoriteRecipeService,
            DietaryPreferencesService dietaryPreferencesService,
            RecipeLocalizationService recipeLocalizationService,
            UserSettingsService userSettingsService,
            ObjectMapper objectMapper) {
        this.spoonacularApiClient = spoonacularApiClient;
        this.spoonacularMapper = spoonacularMapper;
        this.cacheService = cacheService;
        this.favoriteRecipeService = favoriteRecipeService;
        this.dietaryPreferencesService = dietaryPreferencesService;
        this.recipeLocalizationService = recipeLocalizationService;
        this.userSettingsService = userSettingsService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Get recipe by Spoonacular ID.
     * 
     * @param spoonacularId the Spoonacular recipe ID
     * @param userId current user ID (for favorite check, can be null)
     * @return recipe DTO
     * @throws RecipeNotFoundException if recipe not found
     */
    @Transactional(readOnly = true)
    public RecipeDto getRecipeById(Integer spoonacularId, Long userId) {
        log.debug("Getting recipe ID {} for user ID {}", spoonacularId, userId);
        
        // Get user locale for translation
        String userLocale = userSettingsService.getUserLocale(userId);
        
        // Try cache first
        Optional<RecipeCache> cached = cacheService.getCachedRecipe(spoonacularId);
        
        if (cached.isPresent()) {
            try {
                SpoonacularRecipeDto recipeDto = objectMapper.readValue(
                        cached.get().getRecipeData(), SpoonacularRecipeDto.class);
                
                // Check if cached recipe has ingredients - if not, fetch fresh from API
                if (recipeDto.getExtendedIngredients() == null || recipeDto.getExtendedIngredients().isEmpty()) {
                    log.debug("Cached recipe {} missing ingredients, fetching fresh from API", spoonacularId);
                    // Fall through to API fetch below
                } else {
                    RecipeDto dto = spoonacularMapper.toRecipeDto(recipeDto);
                    enrichWithFavoriteStatus(dto, userId);
                    
                    // Enrich with translations
                    dto = recipeLocalizationService.enrichWithTranslations(dto, spoonacularId, userLocale);
                    
                    return dto;
                }
            } catch (Exception e) {
                log.warn("Failed to deserialize cached recipe: {}", spoonacularId, e);
            }
        }
        
        // Fetch from API
        try {
            SpoonacularRecipeDto recipe = spoonacularApiClient.getRecipeById(spoonacularId);
            RecipeDto dto = spoonacularMapper.toRecipeDto(recipe);
            enrichWithFavoriteStatus(dto, userId);
            
            // Cache the recipe
            cacheService.saveRecipe(spoonacularId, recipe, null);
            
            // Enrich with translations
            dto = recipeLocalizationService.enrichWithTranslations(dto, spoonacularId, userLocale);
            
            return dto;
        } catch (SpoonacularApiException e) {
            log.error("Failed to get recipe from Spoonacular: {}", spoonacularId, e);
            throw new RecipeNotFoundException("Recipe not found with id: " + spoonacularId);
        }
    }
    
    /**
     * Search recipes with filters.
     * 
     * @param params search parameters
     * @param userId current user ID (for favorite check and dietary preferences, can be null)
     * @return page of recipe DTOs
     */
    @Transactional(readOnly = true)
    public Page<RecipeDto> searchRecipes(SpoonacularSearchParams params, Long userId) {
        log.debug("Searching recipes for user ID {}", userId);
        try {
            // Generate search hash for caching
            String searchHash = generateSearchHash(params);
            
            // Get user locale for translation
            String userLocale = userSettingsService.getUserLocale(userId);
            
            // Check search cache first
            List<RecipeCache> cachedSearchResults = cacheService.getCachedSearchResults(searchHash);
            if (!cachedSearchResults.isEmpty()) {
                log.debug("Found {} cached search results", cachedSearchResults.size());
                List<RecipeDto> dtos = cachedSearchResults.stream()
                        .map(cache -> {
                            try {
                                SpoonacularRecipeDto recipeDto = objectMapper.readValue(
                                        cache.getRecipeData(), SpoonacularRecipeDto.class);
                                RecipeDto dto = spoonacularMapper.toRecipeDto(recipeDto);
                                enrichWithFavoriteStatus(dto, userId);
                                
                                // Enrich with translations
                                dto = recipeLocalizationService.enrichWithTranslations(
                                        dto, cache.getSpoonacularId(), userLocale);
                                
                                return dto;
                            } catch (Exception e) {
                                log.warn("Failed to deserialize cached recipe: {}", cache.getSpoonacularId(), e);
                                return null;
                            }
                        })
                        .filter(dto -> dto != null)
                        .collect(Collectors.toList());
                
                // Apply pagination
                int start = params.getOffset() != null ? params.getOffset() : 0;
                int end = Math.min(start + (params.getNumber() != null ? params.getNumber() : 20), dtos.size());
                List<RecipeDto> paginated = dtos.isEmpty() || start >= dtos.size() ? List.of() : dtos.subList(Math.min(start, dtos.size()), end);
                
                return new PageImpl<>(paginated, 
                        PageRequest.of(start / (params.getNumber() != null ? params.getNumber() : 20), 
                                params.getNumber() != null ? params.getNumber() : 20),
                        dtos.size());
            }
            
            // If search cache miss, try individual cache
            List<String> cuisines = params.getCuisine();
            List<String> diets = params.getDiet() != null ? List.of(params.getDiet()) : null;
            List<String> intolerances = params.getIntolerances();
            List<String> ingredients = params.getIncludeIngredients();
            
            List<RecipeCache> cachedRecipes = cacheService.searchCachedRecipes(
                    cuisines, diets, intolerances, ingredients);
        
            if (!cachedRecipes.isEmpty() && cachedRecipes.size() >= (params.getNumber() != null ? params.getNumber() : 20)) {
                log.debug("Found {} matching recipes in cache", cachedRecipes.size());
                List<RecipeDto> dtos = cachedRecipes.stream()
                        .limit(params.getNumber() != null ? params.getNumber() : 20)
                        .map(cache -> {
                            try {
                                SpoonacularRecipeDto recipeDto = objectMapper.readValue(
                                        cache.getRecipeData(), SpoonacularRecipeDto.class);
                                RecipeDto dto = spoonacularMapper.toRecipeDto(recipeDto);
                                enrichWithFavoriteStatus(dto, userId);
                                
                                // Enrich with translations
                                dto = recipeLocalizationService.enrichWithTranslations(
                                        dto, cache.getSpoonacularId(), userLocale);
                                
                                return dto;
                            } catch (Exception e) {
                                log.warn("Failed to deserialize cached recipe: {}", cache.getSpoonacularId(), e);
                                return null;
                            }
                        })
                        .filter(dto -> dto != null)
                        .collect(Collectors.toList());
                
                return new PageImpl<>(dtos, 
                        PageRequest.of(params.getOffset() != null ? params.getOffset() / (params.getNumber() != null ? params.getNumber() : 20) : 0,
                                params.getNumber() != null ? params.getNumber() : 20),
                        cachedRecipes.size());
            }
        
            // Call Spoonacular API
            // With fillIngredients=true, addRecipeInformation=true, addRecipeInstructions=true, and addRecipeNutrition=true,
            // the complexSearch returns recipe data. Full details will be fetched on-demand when user clicks a recipe.
            SpoonacularSearchResponseDto response = spoonacularApiClient.searchRecipes(params);
            
            // Handle null or empty results
            if (response.getResults() == null || response.getResults().isEmpty()) {
                log.debug("Spoonacular API returned no results");
                return new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            }
            
            // Build DTOs from search results and cache them
            // Note: Search results may not have complete data (ingredients/instructions), but we cache what we have.
            // Full details will be fetched via getRecipeById when user clicks on a recipe.
            List<RecipeDto> dtos = new ArrayList<>();
            for (SpoonacularRecipeDto recipe : response.getResults()) {
                RecipeDto dto = spoonacularMapper.toRecipeDto(recipe);
                enrichWithFavoriteStatus(dto, userId);
                
                // Cache the recipe data from search (may be incomplete, but useful for search results display)
                cacheService.saveRecipe(recipe.getId(), recipe, searchHash);
                
                // Enrich with translations
                dto = recipeLocalizationService.enrichWithTranslations(dto, recipe.getId(), userLocale);
                
                dtos.add(dto);
            }
            
            // Cache search results association
            log.debug("Cached {} recipes from search for search hash: {}", response.getResults().size(), searchHash);
            
            // Apply pagination
            int offset = params.getOffset() != null ? params.getOffset() : 0;
            int number = params.getNumber() != null ? params.getNumber() : 20;
            int start = Math.min(offset, dtos.size());
            int end = Math.min(start + number, dtos.size());
            List<RecipeDto> paginated = dtos.isEmpty() || start >= dtos.size() ? List.of() : dtos.subList(start, end);
            
            // Prevent division by zero
            int pageNumber = number > 0 ? offset / number : 0;
            return new PageImpl<>(paginated,
                    PageRequest.of(pageNumber, number),
                    response.getTotalResults() != null ? response.getTotalResults() : dtos.size());
            
        } catch (SpoonacularApiException e) {
            log.error("Failed to search recipes from Spoonacular", e);
            return new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        }
    }
    
    /**
     * Search recipes using user's dietary preferences.
     * 
     * @param query optional search query
     * @param includeIngredients optional ingredients to include
     * @param excludeIngredients optional ingredients to exclude
     * @param diet optional diet type
     * @param intolerances optional intolerances
     * @param cuisine optional cuisines
     * @param excludeCuisine optional cuisines to exclude
     * @param page page number (0-indexed)
     * @param size page size
     * @param userId current user ID (for dietary preferences and favorite check)
     * @return page of recipe DTOs
     */
    @Transactional(readOnly = true)
    public Page<RecipeDto> searchRecipes(
            String query,
            List<String> includeIngredients,
            List<String> excludeIngredients,
            String diet,
            List<String> intolerances,
            List<String> cuisine,
            List<String> excludeCuisine,
            int page,
            int size,
            Long userId) {
        
        // Get user's dietary preferences if available
        DietaryPreferencesDto userPreferences = null;
        if (userId != null) {
            try {
                userPreferences = dietaryPreferencesService.getDietaryPreferences(userId);
            } catch (Exception e) {
                log.debug("Could not load dietary preferences for user: {}", userId);
            }
        }
        
        // Build search params
        SpoonacularSearchParams.SpoonacularSearchParamsBuilder builder = 
                SpoonacularSearchParams.builder()
                        .query(query)
                        .includeIngredients(includeIngredients)
                        .excludeIngredients(excludeIngredients)
                        .diet(diet)
                        .intolerances(intolerances)
                        .cuisine(cuisine)
                        .excludeCuisine(excludeCuisine)
                        .number(size)
                        .offset(page * size)
                        .addRecipeInformation(true)
                        .addRecipeInstructions(true)
                        .addRecipeNutrition(true)
                        .fillIngredients(true);
        
        // Apply user's dietary preferences if not overridden
        if (userPreferences != null) {
            // Only apply diet if not provided and profile has restriction
            if (diet == null || diet.isBlank()) {
                if (userPreferences.getRestriction() != null) {
                    builder.diet(userPreferences.getRestriction().getSpoonacularValue());
                }
            }
            // Only apply intolerances if not provided (null or empty) and profile has allergies
            if ((intolerances == null || intolerances.isEmpty()) && 
                    userPreferences.getAllergies() != null && 
                    !userPreferences.getAllergies().isEmpty()) {
                builder.intolerances(userPreferences.getAllergies().stream()
                        .filter(allergy -> allergy != null)
                        .map(SupportedAllergy::getSpoonacularValue)
                        .filter(value -> value != null && !value.isBlank())
                        .collect(Collectors.toList()));
            }
            // Only apply cuisine if not provided (null or empty) and profile has cuisines
            if ((cuisine == null || cuisine.isEmpty()) && 
                    userPreferences.getPreferredCuisines() != null && 
                    !userPreferences.getPreferredCuisines().isEmpty()) {
                builder.cuisine(userPreferences.getPreferredCuisines().stream()
                        .filter(cuisineType -> cuisineType != null)
                        .map(SupportedCuisineType::getSpoonacularValue)
                        .filter(value -> value != null && !value.isBlank())
                        .collect(Collectors.toList()));
            }
        }
        
        SpoonacularSearchParams searchParams = builder.build();
        
        return searchRecipes(searchParams, userId);
    }
    
    /**
     * Enrich recipe DTO with favorite status.
     */
    private void enrichWithFavoriteStatus(RecipeDto dto, Long userId) {
        if (userId != null && dto.getSpoonacularId() != null) {
            boolean isFavorite = favoriteRecipeService.isFavorite(dto.getSpoonacularId(), userId);
            dto.setIsFavorite(isFavorite);
        } else {
            dto.setIsFavorite(false);
        }
    }
    
    /**
     * Generate hash for search parameters to use as cache key.
     * Excludes pagination (number, offset) and API flags to ensure same search criteria
     * produces the same hash regardless of pagination or data inclusion flags.
     */
    private String generateSearchHash(SpoonacularSearchParams params) {
        try {
            // Create a hash object with only search criteria (exclude pagination and API flags)
            Map<String, Object> hashParams = new HashMap<>();
            if (params.getQuery() != null) {
                hashParams.put("query", params.getQuery());
            }
            if (params.getIncludeIngredients() != null) {
                hashParams.put("includeIngredients", params.getIncludeIngredients());
            }
            if (params.getExcludeIngredients() != null) {
                hashParams.put("excludeIngredients", params.getExcludeIngredients());
            }
            if (params.getDiet() != null) {
                hashParams.put("diet", params.getDiet());
            }
            if (params.getIntolerances() != null) {
                hashParams.put("intolerances", params.getIntolerances());
            }
            if (params.getCuisine() != null) {
                hashParams.put("cuisine", params.getCuisine());
            }
            if (params.getExcludeCuisine() != null) {
                hashParams.put("excludeCuisine", params.getExcludeCuisine());
            }
            
            String paramsString = objectMapper.writeValueAsString(hashParams);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(paramsString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to generate search hash", e);
            return String.valueOf(params.hashCode());
        }
    }
}
