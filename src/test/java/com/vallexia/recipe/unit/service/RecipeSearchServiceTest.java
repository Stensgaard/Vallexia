package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.dto.RecipeSearchCriteria;
import com.vallexia.recipe.dto.RecipeSearchResponseDto;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeCategory;
import com.vallexia.recipe.entity.DifficultyLevel;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.recipe.service.RecipeSearchService;
import com.vallexia.user.entity.CuisineType;
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
  
  @InjectMocks
  private RecipeSearchService recipeSearchService;
  
  // ==================== searchRecipes() Tests ====================
  
  @Test
  @DisplayName("Should search recipes with text query")
  void shouldSearchRecipesWithTextQuery() {
    // Given
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setQuery("pasta");
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    List<Recipe> recipes = List.of(recipe);
    Page<Recipe> recipePage = new PageImpl<>(recipes, pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).hasSize(1);
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with category filter")
  void shouldSearchRecipesWithCategoryFilter() {
    // Given
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setCategory(RecipeCategory.DINNER);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with cuisine type filter")
  void shouldSearchRecipesWithCuisineTypeFilter() {
    // Given
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setCuisineType(CuisineType.ITALIAN);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with difficulty level filter")
  void shouldSearchRecipesWithDifficultyLevelFilter() {
    // Given
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setDifficultyLevel(DifficultyLevel.EASY);
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with time range filters")
  void shouldSearchRecipesWithTimeRangeFilters() {
    // Given
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
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with calories range filter")
  void shouldSearchRecipesWithCaloriesRangeFilter() {
    // Given
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setMinCalories(BigDecimal.valueOf(200.0));
    criteria.setMaxCalories(BigDecimal.valueOf(500.0));
    Pageable pageable = PageRequest.of(0, 20);
    
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Page<Recipe> recipePage = new PageImpl<>(List.of(recipe), pageable, 1);
    RecipeDto recipeDto = new RecipeDto();
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(recipePage);
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
    // Then
    assertThat(result).isNotNull();
    verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
  }
  
  @Test
  @DisplayName("Should search recipes with combined filters")
  void shouldSearchRecipesWithCombinedFilters() {
    // Given
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
    when(recipeMapper.toRecipeDto(any(Recipe.class), eq(false)))
        .thenReturn(recipeDto);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
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
    RecipeSearchCriteria criteria = new RecipeSearchCriteria();
    criteria.setQuery("nonexistent");
    Pageable pageable = PageRequest.of(0, 20);
    
    Page<Recipe> emptyPage = new PageImpl<>(List.of(), pageable, 0);
    
    when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(emptyPage);
    
    // When
    RecipeSearchResponseDto result = recipeSearchService.searchRecipes(criteria, pageable);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRecipes()).isEmpty();
    assertThat(result.getPagination().getTotalElements()).isEqualTo(0);
  }
}
