package com.vallexia.recipe.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.RecipeCache;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.integration.client.SpoonacularApiClient;
import com.vallexia.recipe.integration.dto.SpoonacularIngredientDto;
import com.vallexia.recipe.integration.dto.SpoonacularRecipeDto;
import com.vallexia.recipe.integration.dto.SpoonacularSearchParams;
import com.vallexia.recipe.integration.dto.SpoonacularSearchResponseDto;
import com.vallexia.recipe.integration.exception.SpoonacularApiException;
import com.vallexia.recipe.integration.mapper.SpoonacularMapper;
import com.vallexia.recipe.service.FavoriteRecipeService;
import com.vallexia.recipe.service.RecipeCacheService;
import com.vallexia.recipe.service.RecipeLocalizationService;
import com.vallexia.recipe.service.RecipeService;
import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.service.DietaryPreferencesService;
import com.vallexia.user.service.UserSettingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeService.
 * Tests business logic with Spoonacular API integration and caching.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeService Unit Tests")
class RecipeServiceTest {
  
  private static final Integer TEST_SPOONACULAR_ID = 12345;
  private static final Long TEST_USER_ID = 1L;
  
  @Mock
  private SpoonacularApiClient spoonacularApiClient;
  
  @Mock
  private SpoonacularMapper spoonacularMapper;
  
  @Mock
  private RecipeCacheService cacheService;
  
  @Mock
  private FavoriteRecipeService favoriteRecipeService;
  
  @Mock
  private DietaryPreferencesService dietaryPreferencesService;
  
  @Mock
  private RecipeLocalizationService recipeLocalizationService;
  
  @Mock
  private UserSettingsService userSettingsService;
  
  @Mock
  private ObjectMapper objectMapper;
  
  @InjectMocks
  private RecipeService recipeService;
  
  // ==================== getRecipeById() Tests ====================
  
  @Test
  @DisplayName("Should retrieve recipe by ID from cache successfully")
  void shouldRetrieveRecipeByIdFromCacheSuccessfully() throws Exception {
    // Given
    RecipeCache recipeCache = new RecipeCache();
    recipeCache.setSpoonacularId(TEST_SPOONACULAR_ID);
    recipeCache.setRecipeData("{\"id\":" + TEST_SPOONACULAR_ID + ",\"title\":\"Test Recipe\"}");
    
    SpoonacularRecipeDto cachedRecipeDto = new SpoonacularRecipeDto();
    cachedRecipeDto.setId(TEST_SPOONACULAR_ID);
    cachedRecipeDto.setTitle("Test Recipe");
    // Add ingredients so the service doesn't fall through to API fetch
    SpoonacularIngredientDto ingredient = new SpoonacularIngredientDto();
    ingredient.setName("Test Ingredient");
    cachedRecipeDto.setExtendedIngredients(List.of(ingredient));
    
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setSpoonacularId(TEST_SPOONACULAR_ID);
    recipeDto.setName("Test Recipe");
    
    RecipeDto enrichedDto = new RecipeDto();
    enrichedDto.setSpoonacularId(TEST_SPOONACULAR_ID);
    enrichedDto.setName("Test Recipe");
    enrichedDto.setIsFavorite(true);
    
    when(cacheService.getCachedRecipe(TEST_SPOONACULAR_ID))
        .thenReturn(Optional.of(recipeCache));
    when(objectMapper.readValue(anyString(), eq(SpoonacularRecipeDto.class)))
        .thenReturn(cachedRecipeDto);
    when(spoonacularMapper.toRecipeDto(cachedRecipeDto))
        .thenReturn(recipeDto);
    when(favoriteRecipeService.isFavorite(TEST_SPOONACULAR_ID, TEST_USER_ID))
        .thenReturn(true);
    when(userSettingsService.getUserLocale(TEST_USER_ID))
        .thenReturn("en");
    when(recipeLocalizationService.enrichWithTranslations(any(RecipeDto.class), eq(TEST_SPOONACULAR_ID), eq("en")))
        .thenReturn(enrichedDto);
    
    // When
    RecipeDto result = recipeService.getRecipeById(TEST_SPOONACULAR_ID, TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSpoonacularId()).isEqualTo(TEST_SPOONACULAR_ID);
    verify(cacheService).getCachedRecipe(TEST_SPOONACULAR_ID);
    verify(spoonacularApiClient, never()).getRecipeById(any());
  }
  
  @Test
  @DisplayName("Should retrieve recipe by ID from API when cache miss")
  void shouldRetrieveRecipeByIdFromApiWhenCacheMiss() {
    // Given
    SpoonacularRecipeDto apiRecipeDto = new SpoonacularRecipeDto();
    apiRecipeDto.setId(TEST_SPOONACULAR_ID);
    apiRecipeDto.setTitle("Test Recipe");
    
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setSpoonacularId(TEST_SPOONACULAR_ID);
    recipeDto.setName("Test Recipe");
    
    RecipeDto enrichedDto = new RecipeDto();
    enrichedDto.setSpoonacularId(TEST_SPOONACULAR_ID);
    enrichedDto.setName("Test Recipe");
    
    when(cacheService.getCachedRecipe(TEST_SPOONACULAR_ID))
        .thenReturn(Optional.empty());
    when(spoonacularApiClient.getRecipeById(TEST_SPOONACULAR_ID))
        .thenReturn(apiRecipeDto);
    when(spoonacularMapper.toRecipeDto(apiRecipeDto))
        .thenReturn(recipeDto);
    when(favoriteRecipeService.isFavorite(TEST_SPOONACULAR_ID, TEST_USER_ID))
        .thenReturn(false);
    when(userSettingsService.getUserLocale(TEST_USER_ID))
        .thenReturn("en");
    when(recipeLocalizationService.enrichWithTranslations(any(RecipeDto.class), eq(TEST_SPOONACULAR_ID), eq("en")))
        .thenReturn(enrichedDto);
    
    // When
    RecipeDto result = recipeService.getRecipeById(TEST_SPOONACULAR_ID, TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSpoonacularId()).isEqualTo(TEST_SPOONACULAR_ID);
    verify(spoonacularApiClient).getRecipeById(TEST_SPOONACULAR_ID);
    verify(cacheService).saveRecipe(eq(TEST_SPOONACULAR_ID), eq(apiRecipeDto), isNull());
  }
  
  @Test
  @DisplayName("Should throw RecipeNotFoundException when recipe doesn't exist")
  void shouldThrowRecipeNotFoundExceptionWhenRecipeDoesNotExist() {
    // Given
    when(cacheService.getCachedRecipe(TEST_SPOONACULAR_ID))
        .thenReturn(Optional.empty());
    when(spoonacularApiClient.getRecipeById(TEST_SPOONACULAR_ID))
        .thenThrow(new SpoonacularApiException("Recipe not found"));
    
    // When & Then
    assertThatThrownBy(() -> recipeService.getRecipeById(TEST_SPOONACULAR_ID, TEST_USER_ID))
        .isInstanceOf(RecipeNotFoundException.class)
        .hasMessageContaining("Recipe not found with id: " + TEST_SPOONACULAR_ID);
  }
  
  // ==================== searchRecipes() Tests ====================
  
  @Test
  @DisplayName("Should search recipes successfully")
  void shouldSearchRecipesSuccessfully() {
    // Given
    SpoonacularSearchParams params = SpoonacularSearchParams.builder()
        .query("pasta")
        .number(20)
        .offset(0)
        .addRecipeInformation(true)
        .addRecipeInstructions(true)
        .addRecipeNutrition(true)
        .build();
    
    SpoonacularRecipeDto recipeDto = new SpoonacularRecipeDto();
    recipeDto.setId(TEST_SPOONACULAR_ID);
    recipeDto.setTitle("Pasta Recipe");
    
    SpoonacularSearchResponseDto searchResponse = new SpoonacularSearchResponseDto();
    searchResponse.setResults(List.of(recipeDto));
    searchResponse.setTotalResults(1);
    
    RecipeDto mappedDto = new RecipeDto();
    mappedDto.setSpoonacularId(TEST_SPOONACULAR_ID);
    mappedDto.setName("Pasta Recipe");
    
    RecipeDto enrichedDto = new RecipeDto();
    enrichedDto.setSpoonacularId(TEST_SPOONACULAR_ID);
    enrichedDto.setName("Pasta Recipe");
    
    when(cacheService.getCachedSearchResults(anyString()))
        .thenReturn(List.of());
    when(dietaryPreferencesService.getDietaryPreferences(TEST_USER_ID))
        .thenReturn(null);
    when(spoonacularApiClient.searchRecipes(any(SpoonacularSearchParams.class)))
        .thenReturn(searchResponse);
    when(spoonacularMapper.toRecipeDto(recipeDto))
        .thenReturn(mappedDto);
    when(favoriteRecipeService.isFavorite(TEST_SPOONACULAR_ID, TEST_USER_ID))
        .thenReturn(false);
    when(userSettingsService.getUserLocale(TEST_USER_ID))
        .thenReturn("en");
    when(recipeLocalizationService.enrichWithTranslations(any(RecipeDto.class), eq(TEST_SPOONACULAR_ID), eq("en")))
        .thenReturn(enrichedDto);
    
    // When
    Page<RecipeDto> result = recipeService.searchRecipes(params, TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(spoonacularApiClient).searchRecipes(any(SpoonacularSearchParams.class));
    verify(cacheService).saveRecipe(eq(TEST_SPOONACULAR_ID), eq(recipeDto), anyString());
  }
  
  @Test
  @DisplayName("Should search recipes with user dietary preferences")
  void shouldSearchRecipesWithUserDietaryPreferences() {
    // Given
    DietaryPreferencesDto preferences = new DietaryPreferencesDto();
    preferences.setRestriction(com.vallexia.common.enums.SupportedDietaryRestriction.VEGAN);
    
    SpoonacularSearchResponseDto searchResponse = new SpoonacularSearchResponseDto();
    searchResponse.setResults(List.of());
    searchResponse.setTotalResults(0);
    
    when(cacheService.getCachedSearchResults(anyString()))
        .thenReturn(List.of());
    when(dietaryPreferencesService.getDietaryPreferences(TEST_USER_ID))
        .thenReturn(preferences);
    when(spoonacularApiClient.searchRecipes(any(SpoonacularSearchParams.class)))
        .thenReturn(searchResponse);
    when(userSettingsService.getUserLocale(TEST_USER_ID))
        .thenReturn("en");
    
    // When - use the overloaded method that takes individual parameters
    Page<RecipeDto> result = recipeService.searchRecipes(
            null, // query
            null, // includeIngredients
            null, // excludeIngredients
            null, // diet - null so user preferences will be used
            null, // intolerances - null so user preferences will be used
            null, // cuisine - null so user preferences will be used
            null, // excludeCuisine
            0,    // page
            20,   // size
            TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    verify(dietaryPreferencesService).getDietaryPreferences(TEST_USER_ID);
  }
}
