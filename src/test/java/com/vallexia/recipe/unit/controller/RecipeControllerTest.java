package com.vallexia.recipe.unit.controller;

import com.vallexia.recipe.controller.RecipeController;
import com.vallexia.recipe.dto.*;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.service.*;
import com.vallexia.security.AuthenticationHelper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeController Unit Tests")
class RecipeControllerTest {
  
  @Mock
  private RecipeService recipeService;
  
  @Mock
  private RecipeScalingService recipeScalingService;
  
  @Mock
  private FavoriteRecipeService favoriteRecipeService;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @InjectMocks
  private RecipeController recipeController;
  
  private Authentication mockAuthentication;
  
  @BeforeEach
  void setUp() {
    mockAuthentication = mock(Authentication.class);
    when(authenticationHelper.getCurrentUserId(any(Authentication.class)))
        .thenReturn(1L);
  }
  
  // ==================== getAllRecipes() Tests ====================
  
@SuppressWarnings("null")
@Test
  @DisplayName("Should retrieve all recipes")
  void shouldRetrieveAllRecipes() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    RecipeDto recipeDto = new RecipeDto();
    Page<RecipeDto> recipePage = new PageImpl<>(List.of(recipeDto), pageable, 1);
    
    when(recipeService.getRecipes(pageable, 1L))
        .thenReturn(recipePage);
    
    // When
    ResponseEntity<Page<RecipeDto>> response = recipeController.getAllRecipes(0, 20, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Page<RecipeDto> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getContent()).hasSize(1);
  }
  
  // ==================== getRecipeById() Tests ====================
  
  @Test
  @DisplayName("Should retrieve recipe by ID")
  void shouldRetrieveRecipeById() {
    // Given
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setId(RecipeTestFixtures.TEST_RECIPE_ID);
    
    when(recipeService.getRecipeById(RecipeTestFixtures.TEST_RECIPE_ID, 1L))
        .thenReturn(recipeDto);
    
    // When
    ResponseEntity<RecipeDto> response = recipeController.getRecipeById(RecipeTestFixtures.TEST_RECIPE_ID, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }
  
  // ==================== scaleRecipe() Tests ====================
  
@SuppressWarnings("null")
  @Test
  @DisplayName("Should scale recipe successfully")
  void shouldScaleRecipeSuccessfully() {
    // Given
    RecipeDto scaledRecipe = new RecipeDto();
    scaledRecipe.setServings(8);
    
    when(recipeScalingService.scaleRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 8))
        .thenReturn(scaledRecipe);
    
    // When
    ResponseEntity<RecipeDto> response = recipeController.scaleRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 8, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    RecipeDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getServings()).isEqualTo(8);
  }
  
  // ==================== addFavorite() Tests ====================
  
  @Test
  @DisplayName("Should add recipe to favorites successfully")
  void shouldAddRecipeToFavoritesSuccessfully() {
    // Given
    doNothing().when(favoriteRecipeService).addFavorite(RecipeTestFixtures.TEST_RECIPE_ID, 1L);
    
    // When
    ResponseEntity<Void> response = recipeController.addFavorite(RecipeTestFixtures.TEST_RECIPE_ID, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(favoriteRecipeService).addFavorite(RecipeTestFixtures.TEST_RECIPE_ID, 1L);
  }
  
  // ==================== removeFavorite() Tests ====================
  
  @Test
  @DisplayName("Should remove recipe from favorites successfully")
  void shouldRemoveRecipeFromFavoritesSuccessfully() {
    // Given
    doNothing().when(favoriteRecipeService).removeFavorite(RecipeTestFixtures.TEST_RECIPE_ID, 1L);
    
    // When
    ResponseEntity<Void> response = recipeController.removeFavorite(RecipeTestFixtures.TEST_RECIPE_ID, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(favoriteRecipeService).removeFavorite(RecipeTestFixtures.TEST_RECIPE_ID, 1L);
  }
  
  // ==================== getFavorites() Tests ====================
  
  @Test
  @DisplayName("Should retrieve user favorites")
  void shouldRetrieveUserFavorites() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    RecipeDto recipeDto = new RecipeDto();
    Page<RecipeDto> favoritesPage = new PageImpl<>(List.of(recipeDto), pageable, 1);
    
    when(favoriteRecipeService.getUserFavorites(1L, pageable))
        .thenReturn(favoritesPage);
    
    // When
    ResponseEntity<Page<RecipeDto>> response = recipeController.getFavorites(0, 20, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }
}
