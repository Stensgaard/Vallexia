package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.NutritionalInfoDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.NutritionalInfo;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeIngredient;
import com.vallexia.recipe.exception.InvalidRecipeServingsException;
import com.vallexia.recipe.exception.RecipeNotFoundException;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.recipe.service.RecipeScalingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeScalingService.
 * Tests portion scaling calculations for ingredients and nutrition.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeScalingService Unit Tests")
class RecipeScalingServiceTest {
  
  @Mock
  private RecipeRepository recipeRepository;
  
  @Mock
  private RecipeMapper recipeMapper;
  
  @InjectMocks
  private RecipeScalingService recipeScalingService;
  
  // ==================== scaleRecipe() Tests ====================
  
  @Test
  @DisplayName("Should scale recipe successfully")
  void shouldScaleRecipeSuccessfully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    recipe.setServings(4);
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setId(RecipeTestFixtures.TEST_RECIPE_ID);
    recipeDto.setServings(4);
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    when(recipeMapper.toRecipeDto(recipe, false))
        .thenReturn(recipeDto);
    
    // When
    RecipeDto result = recipeScalingService.scaleRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 8, 1L);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getServings()).isEqualTo(8);
    verify(recipeRepository).findById(RecipeTestFixtures.TEST_RECIPE_ID);
  }
  
  @Test
  @DisplayName("Should throw RecipeNotFoundException when recipe doesn't exist")
  void shouldThrowRecipeNotFoundExceptionWhenRecipeDoesNotExist() {
    // Given
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> recipeScalingService.scaleRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 8, 1L))
        .isInstanceOf(RecipeNotFoundException.class);
  }
  
  @Test
  @DisplayName("Should throw InvalidRecipeServingsException when target servings is invalid")
  void shouldThrowIllegalArgumentExceptionWhenTargetServingsIsInvalid() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    
    when(recipeRepository.findById(RecipeTestFixtures.TEST_RECIPE_ID))
        .thenReturn(Optional.of(recipe));
    
    // When & Then
    assertThatThrownBy(() -> recipeScalingService.scaleRecipe(RecipeTestFixtures.TEST_RECIPE_ID, 0, 1L))
        .isInstanceOf(InvalidRecipeServingsException.class)
        .hasMessageContaining("Target servings must be greater than 0");
  }
  
  // ==================== scaleIngredientQuantities() Tests ====================
  
  @Test
  @DisplayName("Should scale ingredient quantities proportionally")
  void shouldScaleIngredientQuantitiesProportionally() {
    // Given
    RecipeIngredient ingredient = RecipeTestFixtures.createRecipeIngredient();
    ingredient.setQuantity(BigDecimal.valueOf(2.0));
    List<RecipeIngredient> ingredients = List.of(ingredient);
    
    IngredientDto ingredientDto = new IngredientDto();
    ingredientDto.setName(ingredient.getIngredient().getName());
    ingredientDto.setQuantity(ingredient.getQuantity());
    ingredientDto.setUnit(ingredient.getUnit());
    
    when(recipeMapper.toIngredientDto(ingredient))
        .thenReturn(ingredientDto);
    
    // When
    var result = recipeScalingService.scaleIngredientQuantities(ingredients, 4, 8);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(4.0)); // 2 * (8/4)
  }
  
  // ==================== scaleNutritionalInfo() Tests ====================
  
  @Test
  @DisplayName("Should scale nutritional info proportionally")
  void shouldScaleNutritionalInfoProportionally() {
    // Given
    NutritionalInfo nutritionalInfo = RecipeTestFixtures.createNutritionalInfo();
    nutritionalInfo.setCalories(BigDecimal.valueOf(500.0));
    nutritionalInfo.setProtein(BigDecimal.valueOf(20.0));
    
    NutritionalInfoDto nutritionalInfoDto = new NutritionalInfoDto();
    nutritionalInfoDto.setCalories(nutritionalInfo.getCalories());
    nutritionalInfoDto.setProtein(nutritionalInfo.getProtein());
    
    when(recipeMapper.toNutritionalInfoDto(nutritionalInfo))
        .thenReturn(nutritionalInfoDto);
    
    // When
    var result = recipeScalingService.scaleNutritionalInfo(nutritionalInfo, 4, 8);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(1000.0)); // 500 * (8/4)
    assertThat(result.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(40.0)); // 20 * (8/4)
  }
  
  @Test
  @DisplayName("Should scale nutritional info with null values")
  void shouldScaleNutritionalInfoWithNullValues() {
    // Given
    NutritionalInfo nutritionalInfo = new NutritionalInfo();
    nutritionalInfo.setCalories(BigDecimal.valueOf(500.0));
    nutritionalInfo.setProtein(null); // Null value
    
    NutritionalInfoDto nutritionalInfoDto = new NutritionalInfoDto();
    nutritionalInfoDto.setCalories(nutritionalInfo.getCalories());
    nutritionalInfoDto.setProtein(null);
    
    when(recipeMapper.toNutritionalInfoDto(nutritionalInfo))
        .thenReturn(nutritionalInfoDto);
    
    // When
    var result = recipeScalingService.scaleNutritionalInfo(nutritionalInfo, 4, 8);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(1000.0));
    assertThat(result.getProtein()).isNull();
  }
}
