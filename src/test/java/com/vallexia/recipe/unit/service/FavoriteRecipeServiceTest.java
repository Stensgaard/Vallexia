package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.FavoriteRecipe;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.exception.RecipeAlreadyFavoritedException;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.FavoriteRecipeRepository;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.recipe.service.FavoriteRecipeService;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserRepository;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for FavoriteRecipeService.
 * Tests favorite recipe management operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FavoriteRecipeService Unit Tests")
class FavoriteRecipeServiceTest {
  
  @Mock
  private FavoriteRecipeRepository favoriteRecipeRepository;
  
  @Mock
  private RecipeRepository recipeRepository;
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private RecipeMapper recipeMapper;
  
  @Mock
  private com.vallexia.user.service.UserSettingsService userSettingsService;
  
  @Mock
  private com.vallexia.recipe.service.RecipeEnrichmentService recipeEnrichmentService;
  
  @InjectMocks
  private FavoriteRecipeService favoriteRecipeService;
  
  // ==================== addFavorite() Tests ====================
  
  @Test
  @DisplayName("Should add recipe to favorites successfully")
  void shouldAddRecipeToFavoritesSuccessfully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    User user = UserTestFixtures.createUser();
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(favoriteRecipeRepository.existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(false);
    when(favoriteRecipeRepository.save(any(FavoriteRecipe.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // When
    favoriteRecipeService.addFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(favoriteRecipeRepository).save(any(FavoriteRecipe.class));
  }
  
  @Test
  @DisplayName("Should throw RecipeAlreadyFavoritedException when adding duplicate favorite")
  void shouldNotAddDuplicateFavorite() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    User user = UserTestFixtures.createUser();
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(favoriteRecipeRepository.existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(true); // Already favorited
    
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.addFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(RecipeAlreadyFavoritedException.class)
        .hasMessageContaining("Recipe with ID " + RecipeTestFixtures.TEST_RECIPE_ID + " is already in your favorites");
    
    verify(favoriteRecipeRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should throw RecipeNotFoundException when recipe doesn't exist")
  void shouldThrowRecipeNotFoundExceptionWhenRecipeDoesNotExist() {
    // Given
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.addFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(RecipeNotFoundException.class);
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> favoriteRecipeService.addFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class);
  }
  
  // ==================== removeFavorite() Tests ====================
  
  @Test
  @DisplayName("Should remove recipe from favorites successfully")
  void shouldRemoveRecipeFromFavoritesSuccessfully() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(true);
    doNothing().when(favoriteRecipeRepository).deleteByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID);
    
    // When
    favoriteRecipeService.removeFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(favoriteRecipeRepository).existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID);
    verify(favoriteRecipeRepository).deleteByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID);
  }
  
  @Test
  @DisplayName("Should handle removing non-existent favorite gracefully")
  void shouldHandleRemovingNonExistentFavoriteGracefully() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(false);
    
    // When
    favoriteRecipeService.removeFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(favoriteRecipeRepository).existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID);
    verify(favoriteRecipeRepository, never()).deleteByUserIdAndRecipeId(any(), any());
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
    assertThatThrownBy(() -> favoriteRecipeService.removeFavorite(RecipeTestFixtures.TEST_RECIPE_ID, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Recipe ID and User ID cannot be null");
  }
  
  // ==================== getUserFavorites() Tests ====================
  
  @Test
  @DisplayName("Should retrieve user favorites with pagination")
  void shouldRetrieveUserFavoritesWithPagination() {
    // Given
    FavoriteRecipe favorite = new FavoriteRecipe();
    Recipe recipe = RecipeTestFixtures.createRecipe();
    favorite.setRecipe(recipe);
    List<FavoriteRecipe> favorites = List.of(favorite);
    Pageable pageable = PageRequest.of(0, 20);
    Page<FavoriteRecipe> favoritesPage = new PageImpl<>(favorites, pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    RecipeDto enrichedDto = new RecipeDto();
    
    when(favoriteRecipeRepository.findByUserId(UserTestFixtures.TEST_USER_ID, pageable))
        .thenReturn(favoritesPage);
    when(userSettingsService.getUserLocale(UserTestFixtures.TEST_USER_ID))
        .thenReturn("en");
    when(recipeMapper.toRecipeDto(recipe, true))
        .thenReturn(recipeDto);
    when(recipeEnrichmentService.enrichWithTranslations(recipeDto, recipe, "en"))
        .thenReturn(enrichedDto);
    
    // When
    Page<RecipeDto> result = favoriteRecipeService.getUserFavorites(UserTestFixtures.TEST_USER_ID, pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(favoriteRecipeRepository).findByUserId(UserTestFixtures.TEST_USER_ID, pageable);
    verify(userSettingsService).getUserLocale(UserTestFixtures.TEST_USER_ID);
    verify(recipeEnrichmentService).enrichWithTranslations(recipeDto, recipe, "en");
  }
  
  // ==================== isFavorite() Tests ====================
  
  @Test
  @DisplayName("Should return true when recipe is favorited")
  void shouldReturnTrueWhenRecipeIsFavorited() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(true);
    
    // When
    boolean result = favoriteRecipeService.isFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isTrue();
  }
  
  @Test
  @DisplayName("Should return false when recipe is not favorited")
  void shouldReturnFalseWhenRecipeIsNotFavorited() {
    // Given
    when(favoriteRecipeRepository.existsByUserIdAndRecipeId(UserTestFixtures.TEST_USER_ID, RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(false);
    
    // When
    boolean result = favoriteRecipeService.isFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isFalse();
  }
}
