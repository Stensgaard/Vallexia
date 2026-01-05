package com.vallexia.recipe.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.FavoriteRecipe;
import com.vallexia.recipe.entity.RecipeCache;
import com.vallexia.recipe.exception.RecipeAlreadyFavoritedException;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.integration.client.SpoonacularApiClient;
import com.vallexia.recipe.integration.dto.SpoonacularRecipeDto;
import com.vallexia.recipe.integration.exception.SpoonacularApiException;
import com.vallexia.recipe.integration.mapper.SpoonacularMapper;
import com.vallexia.recipe.repository.FavoriteRecipeRepository;
import com.vallexia.recipe.service.FavoriteRecipeService;
import com.vallexia.recipe.service.RecipeCacheService;
import com.vallexia.recipe.service.RecipeLocalizationService;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FavoriteRecipeService.
 * Tests favorite recipe management operations with Spoonacular API integration.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FavoriteRecipeService Unit Tests")
class FavoriteRecipeServiceTest {
  
  private static final Integer TEST_SPOONACULAR_ID = 12345;
  
  @Mock
  private FavoriteRecipeRepository favoriteRecipeRepository;
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private SpoonacularApiClient spoonacularApiClient;
  
  @Mock
  private SpoonacularMapper spoonacularMapper;
  
  @Mock
  private RecipeCacheService cacheService;
  
  @Mock
  private RecipeLocalizationService recipeLocalizationService;
  
  @Mock
  private UserSettingsService userSettingsService;
  
  @Mock
  private ObjectMapper objectMapper;
  
  @InjectMocks
  private FavoriteRecipeService favoriteRecipeService;
  
  // ==================== addFavorite() Tests ====================
  
  @Test
  @DisplayName("Should add recipe to favorites successfully")
  void shouldAddRecipeToFavoritesSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    SpoonacularRecipeDto recipeDto = new SpoonacularRecipeDto();
    recipeDto.setId(TEST_SPOONACULAR_ID);
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(favoriteRecipeRepository.existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID))
        .thenReturn(false);
    when(spoonacularApiClient.getRecipeById(TEST_SPOONACULAR_ID))
        .thenReturn(recipeDto);
    when(favoriteRecipeRepository.save(any(FavoriteRecipe.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // When
    favoriteRecipeService.addFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(spoonacularApiClient).getRecipeById(TEST_SPOONACULAR_ID);
    verify(favoriteRecipeRepository).save(any(FavoriteRecipe.class));
  }
  
  @Test
  @DisplayName("Should throw RecipeAlreadyFavoritedException when adding duplicate favorite")
  void shouldNotAddDuplicateFavorite() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(favoriteRecipeRepository.existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID))
        .thenReturn(true); // Already favorited
    
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.addFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(RecipeAlreadyFavoritedException.class)
        .hasMessageContaining("Recipe with ID " + TEST_SPOONACULAR_ID + " is already in your favorites");
    
    verify(favoriteRecipeRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should throw RecipeNotFoundException when recipe doesn't exist in Spoonacular")
  void shouldThrowRecipeNotFoundExceptionWhenRecipeDoesNotExist() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(favoriteRecipeRepository.existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID))
        .thenReturn(false);
    when(spoonacularApiClient.getRecipeById(TEST_SPOONACULAR_ID))
        .thenThrow(new SpoonacularApiException("Recipe not found"));
    
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.addFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(RecipeNotFoundException.class);
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    // Given
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.addFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class);
  }
  
  // ==================== removeFavorite() Tests ====================
  
  @Test
  @DisplayName("Should remove recipe from favorites successfully")
  void shouldRemoveRecipeFromFavoritesSuccessfully() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID))
        .thenReturn(true);
    doNothing().when(favoriteRecipeRepository).deleteByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID);
    
    // When
    favoriteRecipeService.removeFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(favoriteRecipeRepository).existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID);
    verify(favoriteRecipeRepository).deleteByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID);
  }
  
  @Test
  @DisplayName("Should handle removing non-existent favorite gracefully")
  void shouldHandleRemovingNonExistentFavoriteGracefully() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID))
        .thenReturn(false);
    
    // When
    favoriteRecipeService.removeFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(favoriteRecipeRepository).existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID);
    verify(favoriteRecipeRepository, never()).deleteByUserIdAndSpoonacularId(any(), any());
  }
  
  @Test
  @DisplayName("Should throw IllegalArgumentException when recipeId is null")
  void shouldThrowIllegalArgumentExceptionWhenRecipeIdIsNull() {
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.removeFavorite(null, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recipe ID and User ID cannot be null");
  }
  
  @Test
  @DisplayName("Should throw IllegalArgumentException when userId is null")
  void shouldThrowIllegalArgumentExceptionWhenUserIdIsNull() {
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.removeFavorite(TEST_SPOONACULAR_ID, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recipe ID and User ID cannot be null");
  }
  
  // ==================== getUserFavorites() Tests ====================
  
  @Test
  @DisplayName("Should retrieve user favorites with pagination")
  void shouldRetrieveUserFavoritesWithPagination() {
    // Given
    FavoriteRecipe favorite = new FavoriteRecipe();
    favorite.setSpoonacularId(TEST_SPOONACULAR_ID);
    List<FavoriteRecipe> favorites = List.of(favorite);
    Pageable pageable = PageRequest.of(0, 20);
    Page<FavoriteRecipe> favoritesPage = new PageImpl<>(favorites, pageable, 1);
    
    RecipeCache recipeCache = new RecipeCache();
    recipeCache.setSpoonacularId(TEST_SPOONACULAR_ID);
    recipeCache.setRecipeData("{\"id\":" + TEST_SPOONACULAR_ID + ",\"title\":\"Test Recipe\"}");
    
    SpoonacularRecipeDto recipeDto = new SpoonacularRecipeDto();
    recipeDto.setId(TEST_SPOONACULAR_ID);
    recipeDto.setTitle("Test Recipe");
    
    RecipeDto enrichedDto = new RecipeDto();
    enrichedDto.setSpoonacularId(TEST_SPOONACULAR_ID);
    enrichedDto.setName("Test Recipe");
    enrichedDto.setIsFavorite(true);
    
    when(favoriteRecipeRepository.findByUserId(UserTestFixtures.TEST_USER_ID, pageable))
        .thenReturn(favoritesPage);
    when(cacheService.getCachedRecipe(TEST_SPOONACULAR_ID))
        .thenReturn(Optional.of(recipeCache));
    when(userSettingsService.getUserLocale(UserTestFixtures.TEST_USER_ID))
        .thenReturn("en");
    try {
      when(objectMapper.readValue(anyString(), eq(SpoonacularRecipeDto.class)))
          .thenReturn(recipeDto);
    } catch (Exception e) {
      // Mock setup
    }
    when(spoonacularMapper.toRecipeDto(any(SpoonacularRecipeDto.class)))
        .thenReturn(enrichedDto);
    when(recipeLocalizationService.enrichWithTranslations(any(RecipeDto.class), eq(TEST_SPOONACULAR_ID), eq("en")))
        .thenReturn(enrichedDto);
    
    // When
    Page<RecipeDto> result = favoriteRecipeService.getUserFavorites(UserTestFixtures.TEST_USER_ID, pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(favoriteRecipeRepository).findByUserId(UserTestFixtures.TEST_USER_ID, pageable);
    verify(userSettingsService).getUserLocale(UserTestFixtures.TEST_USER_ID);
  }
  
  // ==================== isFavorite() Tests ====================
  
  @Test
  @DisplayName("Should return true when recipe is favorited")
  void shouldReturnTrueWhenRecipeIsFavorited() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID))
        .thenReturn(true);
    
    // When
    boolean result = favoriteRecipeService.isFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isTrue();
  }
  
  @Test
  @DisplayName("Should return false when recipe is not favorited")
  void shouldReturnFalseWhenRecipeIsNotFavorited() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndSpoonacularId(UserTestFixtures.TEST_USER_ID, TEST_SPOONACULAR_ID))
        .thenReturn(false);
    
    // When
    boolean result = favoriteRecipeService.isFavorite(TEST_SPOONACULAR_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isFalse();
  }
}
