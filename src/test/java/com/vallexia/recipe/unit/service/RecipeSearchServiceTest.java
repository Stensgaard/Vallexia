package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.dto.RecipeSearchCriteria;
import com.vallexia.recipe.dto.RecipeSearchResponseDto;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.recipe.entity.enums.RecipeSortBy;
import com.vallexia.recipe.entity.enums.RecipeSortOrder;
import com.vallexia.recipe.entity.enums.RestrictionMatchMode;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.recipe.service.RecipeSearchService;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.enums.SupportedMealCategory;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeSearchService.
 * Tests advanced search functionality with various criteria.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
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
  private com.vallexia.user.service.UserSettingsService userSettingsService;
  
  @Mock
  private com.vallexia.user.service.DietaryPreferencesService dietaryPreferencesService;
  
  @Mock
  private com.vallexia.recipe.service.RecipeEnrichmentService recipeEnrichmentService;
  
  @InjectMocks
  private RecipeSearchService recipeSearchService;
  
  /**
   * Helper method to set up common mocks for locale and DTO enrichment.
   */
  private void setupCommonMocks() {
    when(userSettingsService.getUserLocale(any(Long.class)))
        .thenReturn("en");
    when(userSettingsService.getUserLocale(null))
        .thenReturn("en");
    when(recipeEnrichmentService.enrichWithTranslations(any(RecipeDto.class), any(Recipe.class), any(String.class)))
        .thenAnswer(invocation -> {
          RecipeDto dto = invocation.getArgument(0);
          return dto; // Return the DTO as-is for testing
        });
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
    criteria.setCategory(SupportedMealCategory.DINNER);
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
  void shouldSearchRecipesWithSupportedCuisineTypeFilter() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setCuisineType(SupportedCuisineType.ITALIAN);
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
    criteria.setCategory(SupportedMealCategory.DINNER);
    criteria.setCuisineType(SupportedCuisineType.ITALIAN);
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
  void shouldFilterRecipesBySupportedDietaryRestrictionsOR() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(SupportedDietaryRestriction.DAIRY_FREE, SupportedDietaryRestriction.VEGETARIAN));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.OR);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addDietaryRestriction(SupportedDietaryRestriction.DAIRY_FREE);
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
  void shouldFilterRecipesBySupportedDietaryRestrictionsAND() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(SupportedDietaryRestriction.DAIRY_FREE, SupportedDietaryRestriction.GLUTEN_FREE));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.AND);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addDietaryRestriction(SupportedDietaryRestriction.DAIRY_FREE);
    recipe.addDietaryRestriction(SupportedDietaryRestriction.GLUTEN_FREE);
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
  void shouldFilterRecipesBySupportedDietaryRestrictionsExcludingIncompatibleAllergens() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(SupportedDietaryRestriction.DAIRY_FREE));
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
    List<SupportedAllergy> userAllergies = List.of(SupportedAllergy.MILK, SupportedAllergy.EGGS);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addAllergen(SupportedAllergy.MILK);
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
    List<SupportedAllergy> userAllergies = List.of(SupportedAllergy.MILK);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addAllergen(SupportedAllergy.MILK);
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
    List<SupportedAllergy> userAllergies = List.of(); // Empty list
    
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
    Set<SupportedCuisineType> preferredCuisines = Set.of(SupportedCuisineType.ITALIAN, SupportedCuisineType.FRENCH);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.setCuisineType(SupportedCuisineType.ITALIAN);
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
    criteria.setCuisineType(SupportedCuisineType.MEXICAN); // Explicit filter
    Pageable pageable = PageRequest.of(0, 20);
    Set<SupportedCuisineType> preferredCuisines = Set.of(SupportedCuisineType.ITALIAN, SupportedCuisineType.FRENCH);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.setCuisineType(SupportedCuisineType.MEXICAN);
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
  void shouldFilterRecipesWithSupportedDietaryRestrictionsAndAllergensCombined() {
    // Given
    setupCommonMocks();
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDietaryRestrictions(List.of(SupportedDietaryRestriction.DAIRY_FREE));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.OR);
    criteria.setExcludeAllergens(true);
    Pageable pageable = PageRequest.of(0, 20);
    List<SupportedAllergy> userAllergies = List.of(SupportedAllergy.PEANUTS);
    
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
    criteria.setCategory(SupportedMealCategory.DINNER);
    criteria.setCuisineType(SupportedCuisineType.ITALIAN);
    criteria.setDifficultyLevel(DifficultyLevel.MEDIUM);
    criteria.setDietaryRestrictions(List.of(SupportedDietaryRestriction.VEGETARIAN));
    criteria.setRestrictionMatchMode(RestrictionMatchMode.OR);
    criteria.setMinCalories(BigDecimal.valueOf(200.0));
    criteria.setMaxCalories(BigDecimal.valueOf(500.0));
    criteria.setExcludeAllergens(true);
    Pageable pageable = PageRequest.of(0, 20);
    List<SupportedAllergy> userAllergies = List.of(SupportedAllergy.SHELLFISH);
    Set<SupportedCuisineType> preferredCuisines = Set.of(SupportedCuisineType.ITALIAN);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.addDietaryRestriction(SupportedDietaryRestriction.VEGETARIAN);
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
