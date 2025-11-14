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
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeController Unit Tests")
class RecipeControllerTest {
  
  @Mock
  private RecipeService recipeService;
  
  @Mock
  private RecipeSearchService recipeSearchService;
  
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
  @DisplayName("Should retrieve all public recipes")
  void shouldRetrieveAllPublicRecipes() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    RecipeDto recipeDto = new RecipeDto();
    Page<RecipeDto> recipePage = new PageImpl<>(List.of(recipeDto), pageable, 1);
    
    when(recipeService.getPublicRecipes(pageable, 1L))
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
  
  // ==================== createRecipe() Tests ====================
  // Note: @PreAuthorize("hasRole('ADMIN')") is tested via integration tests.
  // This unit test verifies the controller logic assuming authorization passed.
  
  @Test
  @DisplayName("Should create recipe successfully (admin only)")
  void shouldCreateRecipeSuccessfully() {
    // Given
    CreateRecipeDto createDto = RecipeTestFixtures.createCreateRecipeDto();
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setId(RecipeTestFixtures.TEST_RECIPE_ID);
    
    when(recipeService.createRecipe(createDto, 1L))
        .thenReturn(recipeDto);
    
    // When
    ResponseEntity<RecipeDto> response = recipeController.createRecipe(createDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    verify(recipeService).createRecipe(createDto, 1L);
  }
  
  // ==================== updateRecipe() Tests ====================
  // Note: @PreAuthorize("hasRole('ADMIN')") is tested via integration tests.
  // This unit test verifies the controller logic assuming authorization passed.
  
  @Test
  @DisplayName("Should update recipe successfully (admin only)")
  void shouldUpdateRecipeSuccessfully() {
    // Given
    UpdateRecipeDto updateDto = RecipeTestFixtures.createUpdateRecipeDto();
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeService.updateRecipe(RecipeTestFixtures.TEST_RECIPE_ID, updateDto, 1L))
        .thenReturn(recipeDto);
    
    // When
    ResponseEntity<RecipeDto> response = recipeController.updateRecipe(RecipeTestFixtures.TEST_RECIPE_ID, updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(recipeService).updateRecipe(RecipeTestFixtures.TEST_RECIPE_ID, updateDto, 1L);
  }
  
  // ==================== deleteRecipe() Tests ====================
  // Note: @PreAuthorize("hasRole('ADMIN')") is tested via integration tests.
  // This unit test verifies the controller logic assuming authorization passed.
  
  @Test
  @DisplayName("Should delete recipe successfully (admin only)")
  void shouldDeleteRecipeSuccessfully() {
    // Given
    doNothing().when(recipeService).deleteRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 1L);
    
    // When
    ResponseEntity<Void> response = recipeController.deleteRecipe(RecipeTestFixtures.TEST_RECIPE_ID, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(recipeService).deleteRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 1L);
  }
  
  // ==================== searchRecipes() Tests ====================
  
  @Test
  @DisplayName("Should search recipes with criteria")
  void shouldSearchRecipesWithCriteria() {
    // Given
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setQuery("pasta");
    Pageable pageable = PageRequest.of(0, 20);
    RecipeSearchResponseDto responseDto = new RecipeSearchResponseDto();
    
    // Create UserSearchPreferences object with null allergies and preferred cuisines
    RecipeSearchService.UserSearchPreferences userPreferences = 
        new RecipeSearchService.UserSearchPreferences(null, null, criteria);
    
    when(recipeSearchService.prepareSearchCriteriaWithUserPreferences(criteria, 1L))
        .thenReturn(userPreferences);
    when(recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null))
        .thenReturn(responseDto);
    
    // When
    ResponseEntity<RecipeSearchResponseDto> response = recipeController.searchRecipes(criteria, 0, 20, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(recipeSearchService).prepareSearchCriteriaWithUserPreferences(criteria, 1L);
    verify(recipeSearchService).searchRecipes(criteria, pageable, 1L, null, null);
  }
  
  // ==================== scaleRecipe() Tests ====================
  
@SuppressWarnings("null")
@Test
  @DisplayName("Should scale recipe successfully")
  void shouldScaleRecipeSuccessfully() {
    // Given
    RecipeDto scaledRecipe = new RecipeDto();
    scaledRecipe.setServings(8);
    
    when(recipeScalingService.scaleRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 8, 1L))
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
