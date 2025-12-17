package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.recipe.service.FavoriteRecipeService;
import com.vallexia.recipe.service.RecipeLocalizationService;
import com.vallexia.recipe.service.RecipeService;
import com.vallexia.user.fixtures.UserTestFixtures;
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
 * Unit tests for RecipeService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@DisplayName("RecipeService Unit Tests")
class RecipeServiceTest {
  
  @Mock
  private RecipeRepository recipeRepository;
  
  @Mock
  private RecipeMapper recipeMapper;
  
  @Mock
  private FavoriteRecipeService favoriteRecipeService;
  
  @Mock
  private UserSettingsService userSettingsService;
  
  @Mock
  private RecipeLocalizationService recipeLocalizationService;
  
  @InjectMocks
  private RecipeService recipeService;
  
  // ==================== Helper Methods ====================
  
  /**
   * Sets up user locale mock with default English language.
   */
  private void setupUserLocaleMock() {
    when(userSettingsService.getUserLocale(any(Long.class)))
        .thenReturn("en");
    when(userSettingsService.getUserLocale(null))
        .thenReturn("en");
  }
  
  /**
   * Sets up recipe localization service mock to return enriched DTO.
   */
  private void setupRecipeLocalizationServiceMock() {
    when(recipeLocalizationService.enrichWithTranslations(any(RecipeDto.class), any(Recipe.class), any(String.class)))
        .thenAnswer(invocation -> {
          RecipeDto dto = invocation.getArgument(0);
          return dto; // Return the DTO as-is for testing
        });
  }
  

  
  // ==================== getRecipeById() Tests ====================
  
  @Test
  @DisplayName("Should retrieve recipe by ID successfully")
  void shouldRetrieveRecipeByIdSuccessfully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto expectedDto = new RecipeDto();
    expectedDto.setId(RecipeTestFixtures.TEST_RECIPE_ID);
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    setupUserLocaleMock();
    setupRecipeLocalizationServiceMock();
    when(favoriteRecipeService.isFavorite(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .thenReturn(true);
    when(recipeMapper.toRecipeDto(recipe, true))
        .thenReturn(expectedDto);
    
    // When
    RecipeDto result = recipeService.getRecipeById(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(RecipeTestFixtures.TEST_RECIPE_ID);
    verify(recipeRepository).findById(RecipeTestFixtures.TEST_RECIPE_ID);
    verify(recipeLocalizationService).enrichWithTranslations(any(RecipeDto.class), eq(recipe), eq("en"));
  }
  
  @Test
  @DisplayName("Should throw RecipeNotFoundException when recipe doesn't exist")
  void shouldThrowRecipeNotFoundExceptionWhenRecipeDoesNotExist() {
    // Given
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> recipeService.getRecipeById(RecipeTestFixtures.TEST_RECIPE_ID, UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(RecipeNotFoundException.class)
        .hasMessageContaining("Recipe not found with id: " + RecipeTestFixtures.TEST_RECIPE_ID);
  }
  
  // ==================== getRecipes() Tests ====================
  
  @Test
  @DisplayName("Should retrieve all recipes with pagination")
  void shouldRetrieveAllRecipesWithPagination() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    List<Recipe> recipes = List.of(recipe);
    Pageable pageable = PageRequest.of(0, 20);
    Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(pageable))
        .thenReturn(recipePage);
    setupUserLocaleMock();
    setupRecipeLocalizationServiceMock();
    when(favoriteRecipeService.isFavorite(any(Long.class), eq(1L)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(recipe, false))
        .thenReturn(recipeDto);
    
    // When
    Page<RecipeDto> result = recipeService.getRecipes(pageable, 1L);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(recipeRepository).findAll(pageable);
    verify(favoriteRecipeService).isFavorite(any(Long.class), eq(1L));
    verify(recipeLocalizationService).enrichWithTranslations(any(RecipeDto.class), eq(recipe), eq("en"));
  }
}
