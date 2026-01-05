package com.vallexia.recipe.unit.controller;

import com.vallexia.recipe.controller.RecipeController;
import com.vallexia.recipe.dto.*;
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
  
  // ==================== searchRecipes() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should search recipes successfully")
  void shouldSearchRecipesSuccessfully() {
    // Given
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setSpoonacularId(12345);
    Page<RecipeDto> recipePage = new PageImpl<>(List.of(recipeDto), PageRequest.of(0, 20), 1);
    
    when(recipeService.searchRecipes(
            isNull(), isNull(), isNull(), isNull(), 
            isNull(), isNull(), isNull(), eq(0), eq(20), eq(1L)))
        .thenReturn(recipePage);
    
    // When
    ResponseEntity<Page<RecipeDto>> response = recipeController.searchRecipes(
            null, null, null, null, null, null, null, 0, 20, mockAuthentication);
    
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
    Integer spoonacularId = 12345; // Spoonacular IDs are Integer
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setSpoonacularId(spoonacularId);
    
    when(recipeService.getRecipeById(spoonacularId, 1L))
        .thenReturn(recipeDto);
    
    // When
    ResponseEntity<RecipeDto> response = recipeController.getRecipeById(spoonacularId, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSpoonacularId()).isEqualTo(spoonacularId);
  }
  
  // ==================== addFavorite() Tests ====================
  
  @Test
  @DisplayName("Should add recipe to favorites successfully")
  void shouldAddRecipeToFavoritesSuccessfully() {
    // Given
    Integer spoonacularId = 12345; // Spoonacular IDs are Integer
    doNothing().when(favoriteRecipeService).addFavorite(spoonacularId, 1L);
    
    // When
    ResponseEntity<Void> response = recipeController.addFavorite(spoonacularId, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(favoriteRecipeService).addFavorite(spoonacularId, 1L);
  }
  
  // ==================== removeFavorite() Tests ====================
  
  @Test
  @DisplayName("Should remove recipe from favorites successfully")
  void shouldRemoveRecipeFromFavoritesSuccessfully() {
    // Given
    Integer spoonacularId = 12345; // Spoonacular IDs are Integer
    doNothing().when(favoriteRecipeService).removeFavorite(spoonacularId, 1L);
    
    // When
    ResponseEntity<Void> response = recipeController.removeFavorite(spoonacularId, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(favoriteRecipeService).removeFavorite(spoonacularId, 1L);
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
