package com.vallexia.recipe.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.dto.RecipeSearchCriteria;
import com.vallexia.recipe.dto.RecipeSearchResponseDto;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.recipe.service.specification.RecipeSortHelper;
import com.vallexia.recipe.service.specification.RecipeSpecificationBuilder;
import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.user.service.DietaryPreferencesService;
import com.vallexia.user.service.UserSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for advanced recipe search with multiple filters and criteria.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RecipeSearchService {
    
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final FavoriteRecipeService favoriteRecipeService;
    private final DietaryPreferencesService dietaryPreferencesService;
    private final UserSettingsService userSettingsService;
    private final RecipeEnrichmentService recipeEnrichmentService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param recipeRepository the recipe repository
     * @param recipeMapper the recipe mapper
     * @param favoriteRecipeService the favorite recipe service
     * @param dietaryPreferencesService the dietary preferences service
     * @param userSettingsService the user settings service (for locale resolution)
     * @param recipeEnrichmentService the recipe enrichment service
     */
    public RecipeSearchService(
            RecipeRepository recipeRepository,
            RecipeMapper recipeMapper,
            FavoriteRecipeService favoriteRecipeService,
            DietaryPreferencesService dietaryPreferencesService,
            UserSettingsService userSettingsService,
            RecipeEnrichmentService recipeEnrichmentService) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.favoriteRecipeService = favoriteRecipeService;
        this.dietaryPreferencesService = dietaryPreferencesService;
        this.userSettingsService = userSettingsService;
        this.recipeEnrichmentService = recipeEnrichmentService;
    }
    
    /**
     * Prepare search criteria with user's dietary preferences.
     * Fetches user preferences and applies dietary restrictions to criteria if not explicitly provided.
     * 
     * @param criteria search criteria (may be modified)
     * @param userId current user ID
     * @return UserSearchPreferences containing allergies, preferred cuisines, and updated criteria
     */
    public UserSearchPreferences prepareSearchCriteriaWithUserPreferences(RecipeSearchCriteria criteria, Long userId) {
        log.debug("Preparing search criteria with user preferences for user ID {}", userId);
        
        // Fetch user allergies, preferred cuisines, and dietary restrictions for filtering
        List<SupportedAllergy> userAllergies = null;
        Set<SupportedCuisineType> preferredCuisines = null;
        List<SupportedDietaryRestriction> userDietaryRestrictions = null;
        
        try {
            DietaryPreferencesDto dietaryPreferences = dietaryPreferencesService.getDietaryPreferences(userId);
            if (dietaryPreferences.getAllergies() != null && !dietaryPreferences.getAllergies().isEmpty()) {
                userAllergies = new ArrayList<>(dietaryPreferences.getAllergies());
            }
            if (dietaryPreferences.getPreferredCuisines() != null && !dietaryPreferences.getPreferredCuisines().isEmpty()) {
                preferredCuisines = dietaryPreferences.getPreferredCuisines();
            }
            // Fetch dietary restrictions from user's profile
            if (dietaryPreferences.getRestrictions() != null && !dietaryPreferences.getRestrictions().isEmpty()) {
                userDietaryRestrictions = new ArrayList<>(dietaryPreferences.getRestrictions());
            }
        } catch (Exception e) {
            log.debug("Could not fetch user dietary preferences for user ID {}: {}", userId, e.getMessage());
            // Continue without preference filtering if preferences not found
        }
        
        // Apply user's dietary restrictions to search criteria if not explicitly provided
        // Explicit filters in search criteria take precedence over profile preferences
        if ((criteria.getDietaryRestrictions() == null || criteria.getDietaryRestrictions().isEmpty()) 
                && userDietaryRestrictions != null && !userDietaryRestrictions.isEmpty()) {
            criteria.setDietaryRestrictions(userDietaryRestrictions);
            log.debug("Applied dietary restrictions from user profile: {}", userDietaryRestrictions);
        }
        
        return new UserSearchPreferences(userAllergies, preferredCuisines, criteria);
    }
    
    /**
     * Search recipes with advanced filtering criteria.
     * 
     * @param criteria search criteria
     * @param pageable pagination information
     * @param userId current user ID (for favorite check, can be null)
     * @param userAllergies user's allergies (for auto-hiding recipes, can be null or empty)
     * @param preferredCuisines user's preferred cuisines (for automatic filtering, can be null or empty)
     * @return search response with paginated results
     */
    public RecipeSearchResponseDto searchRecipes(RecipeSearchCriteria criteria, Pageable pageable, Long userId, List<SupportedAllergy> userAllergies, Set<SupportedCuisineType> preferredCuisines) {
        log.debug("Searching recipes with criteria: {} for user ID {}", criteria, userId);
        
        // Get user's locale for translation resolution
        String userLocale = userSettingsService.getUserLocale(userId);
        
        // Build specification from criteria (no locale filtering)
        Specification<Recipe> spec = RecipeSpecificationBuilder.buildSpecification(criteria, userAllergies, preferredCuisines);
        
        // Apply sorting
        Pageable sortedPageable = RecipeSortHelper.applySorting(pageable, criteria);
        
        // Execute search
        Page<Recipe> recipePage = recipeRepository.findAll(spec, sortedPageable);
        
        // Convert to DTOs with favorite status and resolve translations
        Page<RecipeDto> recipeDtoPage = recipePage.map(recipe -> {
            boolean isFavorite = userId != null && favoriteRecipeService.isFavorite(recipe.getId(), userId);
            RecipeDto dto = recipeMapper.toRecipeDto(recipe, isFavorite);
            
            // Enrich with translations and ingredient names
            return recipeEnrichmentService.enrichWithTranslations(dto, recipe, userLocale);
        });
        
        // Build response
        RecipeSearchResponseDto response = new RecipeSearchResponseDto();
        response.setRecipes(recipeDtoPage.getContent());
        
        RecipeSearchResponseDto.PaginationInfo pagination = new RecipeSearchResponseDto.PaginationInfo();
        pagination.setPage(recipeDtoPage.getNumber());
        pagination.setSize(recipeDtoPage.getSize());
        pagination.setTotalElements(recipeDtoPage.getTotalElements());
        pagination.setTotalPages(recipeDtoPage.getTotalPages());
        pagination.setHasNext(recipeDtoPage.hasNext());
        pagination.setHasPrevious(recipeDtoPage.hasPrevious());
        response.setPagination(pagination);
        
        log.debug("Found {} recipes matching criteria", recipeDtoPage.getTotalElements());
        return response;
    }
    
    /**
     * Record to hold user search preferences data.
     * 
     * @param userAllergies user's allergies (can be null)
     * @param preferredCuisines user's preferred cuisines (can be null)
     * @param criteria search criteria (may have been modified with user preferences)
     */
    public record UserSearchPreferences(
            List<SupportedAllergy> userAllergies,
            Set<SupportedCuisineType> preferredCuisines,
            RecipeSearchCriteria criteria
    ) {}
}
