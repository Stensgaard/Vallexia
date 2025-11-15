package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.dto.RecipeSearchCriteria;
import com.vallexia.recipe.dto.RecipeSearchResponseDto;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeCategory;
import com.vallexia.recipe.entity.enums.RecipeSortBy;
import com.vallexia.recipe.entity.enums.RecipeSortOrder;
import com.vallexia.recipe.entity.enums.RestrictionMatchMode;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.recipe.service.RecipeSearchService;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeSearchService.
 * Tests advanced search functionality with various criteria.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeSearchService Unit Tests")
@SuppressWarnings("unchecked")
class RecipeSearchServiceTest {
  
  @Mock
  private RecipeRepository recipeRepository;
  
  @Mock
  private RecipeMapper recipeMapper;
  
  @Mock
  private com.vallexia.recipe.service.FavoriteRecipeService favoriteRecipeService;
  
  @Mock
  private com.vallexia.recipe.service.TranslationResolver translationResolver;
  
  @Mock
  private com.vallexia.user.service.UserSettingsService userSettingsService;
  
  @Mock
  private com.vallexia.user.service.DietaryPreferencesService dietaryPreferencesService;
  
  @Mock
  private com.vallexia.recipe.repository.IngredientRepository ingredientRepository;
  
  @InjectMocks
  private RecipeSearchService recipeSearchService;
  
  /**
   * Helper method to set up common mocks for translation and user settings.
   */
  private void setupCommonMocks() {
    com.vallexia.user.dto.UserSettingsDto userSettings = new com.vallexia.user.dto.UserSettingsDto();
    userSettings.setLanguage("en");
    
    when(userSettingsService.getUserSettings(any(Long.class)))
        .thenReturn(userSettings);
    when(translationResolver.resolveRecipeContent(any(Recipe.class), any(String.class)))
        .thenAnswer(invocation -> {
          Recipe r = invocation.getArgument(0);
          return new com.vallexia.recipe.service.TranslationResolver.RecipeContent(
              r.getName(), r.getDescription(), r.getInstructions());
        });
    when(translationResolver.resolveIngredientName(any(com.vallexia.recipe.entity.Ingredient.class), any(String.class)))
        .thenAnswer(invocation -> {
          com.vallexia.recipe.entity.Ingredient ing = invocation.getArgument(0);
          return ing != null ? ing.getName() : null;
        });
    when(ingredientRepository.findById(any(Long.class)))
        .thenReturn(Optional.empty());
  }
  
  // ==================== searchRecipes() Tests ====================
  
  @Test
  @DisplayName("Should search recipes with text query")
  void shouldSearchRecipesWithTextQuery() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setQuery("pasta");
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    List<Recipe> recipes = List.of(recipe);
    Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with category filter")
  void shouldSearchRecipesWithCategoryFilter() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setCategory(RecipeCategory.DINNER);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with cuisine type filter")
  void shouldSearchRecipesWithCuisineTypeFilter() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setCuisineType(CuisineType.ITALIAN);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with difficulty level filter")
  void shouldSearchRecipesWithDifficultyLevelFilter() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDifficultyLevel(DifficultyLevel.EASY);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with time range filters")
  void shouldSearchRecipesWithTimeRangeFilters() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setMinPrepTime(10);
    criteria.setMaxPrepTime(30);
    criteria.setMinCookTime(20);
    criteria.setMaxCookTime(60);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with calories range filter")
  void shouldSearchRecipesWithCaloriesRangeFilter() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setMinCalories(BigDecimal.valueOf(200.0));
    criteria.setMaxCalories(BigDecimal.valueOf(500.0));
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with combined filters")
  void shouldSearchRecipesWithCombinedFilters() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setQuery("pasta");
    criteria.setCategory(RecipeCategory.DINNER);
    criteria.setCuisineType(CuisineType.ITALIAN);
    criteria.setDifficultyLevel(DifficultyLevel.MEDIUM);
    criteria.setMinCalories(BigDecimal.valueOf(300.0));
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPagination()).isNotNull();
    assertThat(result.getPagination().getPage()).isEqualTo(0);
    assertThat(result.getPagination().getSize()).isEqualTo(20);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should return empty results when no recipes match criteria")
  void shouldReturnEmptyResultsWhenNoRecipesMatchCriteria() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setQuery("nonexistent");
    Pageable pageable = PageRequest.of(0, 20);
    
    Page<Recipe> emptyPage = new PageImpl<>(List.of(), pageable, 0);
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(emptyPage);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).isEmpty();
    assertThat(result.getPagination().getTotalElements()).isEqualTo(0);
  }
  
  // ==================== Dietary Restrictions Filter Tests ====================
  
  @Test
  @DisplayName("Should filter recipes by dietary restrictions (OR mode)")
  void shouldFilterRecipesByDietaryRestrictionsOR() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(DietaryRestriction.DAIRY_FREE, DietaryRestriction.VEGETARIAN));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.OR);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addDietaryRestriction(DietaryRestriction.DAIRY_FREE);
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should filter recipes by dietary restrictions (AND mode)")
  void shouldFilterRecipesByDietaryRestrictionsAND() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(DietaryRestriction.DAIRY_FREE, DietaryRestriction.GLUTEN_FREE));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.AND);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addDietaryRestriction(DietaryRestriction.DAIRY_FREE);
    recipe.addDietaryRestriction(DietaryRestriction.GLUTEN_FREE);
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should filter recipes by dietary restrictions excluding incompatible allergens")
  void shouldFilterRecipesByDietaryRestrictionsExcludingIncompatibleAllergens() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(DietaryRestriction.DAIRY_FREE));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.OR);
    Pageable pageable = PageRequest.of(0, 20);
    
    // Recipe without DAIRY_FREE tag but also without MILK allergen (should match)
    Recipe recipe = RecipeTestFixtures.createRecipe();
    // Don't add DAIRY_FREE tag, but ensure no MILK allergen
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  // ==================== Allergen Filter Tests ====================
  
  @Test
  @DisplayName("Should exclude recipes with user allergies when excludeAllergens is true")
  void shouldExcludeRecipesWithUserAllergies() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setExcludeAllergens(true);
    Pageable pageable = PageRequest.of(0, 20);
    List<Allergy> userAllergies = List.of(Allergy.MILK, Allergy.EGGS);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addAllergen(Allergy.MILK);
    // Recipe with MILK allergen should be excluded
    Page<Recipe> emptyPage = new PageImpl<>(List.of(), pageable, 0);
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(emptyPage);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, userAllergies, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).isEmpty();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should not exclude recipes when excludeAllergens is false")
  void shouldNotExcludeRecipesWhenExcludeAllergensIsFalse() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setExcludeAllergens(false);
    Pageable pageable = PageRequest.of(0, 20);
    List<Allergy> userAllergies = List.of(Allergy.MILK);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addAllergen(Allergy.MILK);
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, userAllergies, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should not exclude recipes when user has no allergies")
  void shouldNotExcludeRecipesWhenUserHasNoAllergies() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setExcludeAllergens(true);
    Pageable pageable = PageRequest.of(0, 20);
    List<Allergy> userAllergies = List.of(); // Empty list
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, userAllergies, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  // ==================== Preferred Cuisines Filter Tests ====================
  
  @Test
  @DisplayName("Should filter recipes by preferred cuisines when no explicit cuisine filter")
  void shouldFilterRecipesByPreferredCuisines() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    // No explicit cuisineType set
    Pageable pageable = PageRequest.of(0, 20);
    Set<CuisineType> preferredCuisines = Set.of(CuisineType.ITALIAN, CuisineType.FRENCH);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.setCuisineType(CuisineType.ITALIAN);
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, preferredCuisines);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should use explicit cuisine filter over preferred cuisines")
  void shouldUseExplicitCuisineFilterOverPreferredCuisines() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setCuisineType(CuisineType.MEXICAN); // Explicit filter
    Pageable pageable = PageRequest.of(0, 20);
    Set<CuisineType> preferredCuisines = Set.of(CuisineType.ITALIAN, CuisineType.FRENCH);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.setCuisineType(CuisineType.MEXICAN);
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, preferredCuisines);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  // ==================== Sorting Tests ====================
  
  @Test
  @DisplayName("Should sort recipes by name ascending")
  void shouldSortRecipesByNameAscending() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setSortBy(RecipeSortBy.NAME);
    criteria.setSortOrder(RecipeSortOrder.ASC);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should sort recipes by calories descending")
  void shouldSortRecipesByCaloriesDescending() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setSortBy(RecipeSortBy.CALORIES);
    criteria.setSortOrder(RecipeSortOrder.DESC);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, null, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  // ==================== Combined Filter Tests ====================
  
  @Test
  @DisplayName("Should filter recipes with dietary restrictions and allergens combined")
  void shouldFilterRecipesWithDietaryRestrictionsAndAllergensCombined() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(DietaryRestriction.DAIRY_FREE));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.OR);
    criteria.setExcludeAllergens(true);
    Pageable pageable = PageRequest.of(0, 20);
    List<Allergy> userAllergies = List.of(Allergy.PEANUTS);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    // Recipe should match DAIRY_FREE (no MILK allergen) and not have PEANUTS
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, userAllergies, null);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should filter recipes with all filters combined")
  void shouldFilterRecipesWithAllFiltersCombined() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setQuery("pasta");
    criteria.setCategory(RecipeCategory.DINNER);
    criteria.setCuisineType(CuisineType.ITALIAN);
    criteria.setDifficultyLevel(DifficultyLevel.MEDIUM);
    criteria.setDietaryRestrictions(List.of(DietaryRestriction.VEGETARIAN));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.OR);
    criteria.setMinCalories(BigDecimal.valueOf(200.0));
    criteria.setMaxCalories(BigDecimal.valueOf(500.0));
    criteria.setExcludeAllergens(true);
    Pageable pageable = PageRequest.of(0, 20);
    List<Allergy> userAllergies = List.of(Allergy.SHELLFISH);
    Set<CuisineType> preferredCuisines = Set.of(CuisineType.ITALIAN);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addDietaryRestriction(DietaryRestriction.VEGETARIAN);
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(favoriteRecipeService.isFavorite(any(Long.class), any(Long.class)))
        .thenReturn(false);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable, 1L, userAllergies, preferredCuisines);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
}
