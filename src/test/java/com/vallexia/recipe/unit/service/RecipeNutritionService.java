package com.vallexia.recipe.unit.service;

import com.vallexia.nutrition.service.MacroCalculator;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.IngredientNutrition;
import com.vallexia.recipe.entity.NutritionalInfo;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeIngredient;
import com.vallexia.recipe.exception.InvalidRecipeServingsException;
import com.vallexia.recipe.exception.RecipeValidationException;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.repository.IngredientNutritionRepository;
import com.vallexia.recipe.repository.NutritionalInfoRepository;
import com.vallexia.recipe.service.RecipeNutritionService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeNutritionService.
 * Tests nutritional calculation logic.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeNutritionService Unit Tests")
class RecipeNutritionServiceTest {
  
  @Mock
  private MacroCalculator macroCalculator;
  
  @Mock
  private NutritionalInfoRepository nutritionalInfoRepository;
  
  @Mock
  private IngredientNutritionRepository ingredientNutritionRepository;
  
  @InjectMocks
  private RecipeNutritionService recipeNutritionService;
  
  // ==================== calculateRecipeNutrition() Tests ====================
  
  @Test
  @DisplayName("Should calculate recipe nutrition from ingredient nutrition data")
  void shouldCalculateRecipeNutrition() {
    // Given
    RecipeIngredient recipeIngredient = RecipeTestFixtures.createRecipeIngredient();
    Ingredient ingredient = RecipeTestFixtures.createIngredient();
    ingredient.setId(1L);
    recipeIngredient.setIngredient(ingredient);
    recipeIngredient.setQuantity(BigDecimal.valueOf(200.0)); // 200g
    recipeIngredient.setUnit("g");
    
    IngredientNutrition ingredientNutrition = new IngredientNutrition();
    ingredientNutrition.setIngredient(ingredient);
    ingredientNutrition.setCaloriesPer100g(BigDecimal.valueOf(250.0)); // 250 cal per 100g
    ingredientNutrition.setProteinPer100g(BigDecimal.valueOf(10.0)); // 10g protein per 100g
    ingredientNutrition.setCarbsPer100g(BigDecimal.valueOf(30.0)); // 30g carbs per 100g
    ingredientNutrition.setFatsPer100g(BigDecimal.valueOf(5.0)); // 5g fats per 100g
    
    when(ingredientNutritionRepository.findByIngredientId(1L))
        .thenReturn(Optional.of(ingredientNutrition));
    
    // When
    NutritionalInfo result = recipeNutritionService.calculateRecipeNutrition(
        List.of(recipeIngredient)
    );
    
    // Then
    assertThat(result).isNotNull();
    // 200g * (250 cal / 100g) = 500 calories
    assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(500.0));
    // 200g * (10g / 100g) = 20g protein
    assertThat(result.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
    assertThat(result.getPerServing()).isFalse();
  }
  
  @Test
  @DisplayName("Should throw exception when no ingredient nutrition data available")
  void shouldThrowExceptionWhenNoIngredientNutritionDataAvailable() {
    // Given
    RecipeIngredient recipeIngredient = RecipeTestFixtures.createRecipeIngredient();
    Ingredient ingredient = RecipeTestFixtures.createIngredient();
    ingredient.setId(1L);
    recipeIngredient.setIngredient(ingredient);
    
    when(ingredientNutritionRepository.findByIngredientId(1L))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> recipeNutritionService.calculateRecipeNutrition(
        List.of(recipeIngredient)
    ))
        .isInstanceOf(RecipeValidationException.class)
        .hasMessageContaining("no nutrition data available for recipe ingredients");
  }
  
  // ==================== calculatePerServingNutrition() Tests ====================
  
  @Test
  @DisplayName("Should calculate per-serving nutrition correctly")
  void shouldCalculatePerServingNutritionCorrectly() {
    // Given
    NutritionalInfo totalInfo = RecipeTestFixtures.createNutritionalInfo();
    totalInfo.setCalories(BigDecimal.valueOf(800.0));
    totalInfo.setProtein(BigDecimal.valueOf(40.0));
    Integer servings = 4;
    
    // When
    NutritionalInfo result = recipeNutritionService.calculatePerServingNutrition(totalInfo, servings);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPerServing()).isTrue();
    assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(200.0)); // 800 / 4
    assertThat(result.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(10.0)); // 40 / 4
  }
  
  @Test
  @DisplayName("Should throw InvalidRecipeServingsException when servings is invalid")
  void shouldThrowIllegalArgumentExceptionWhenServingsIsInvalid() {
    // Given
    NutritionalInfo totalInfo = RecipeTestFixtures.createNutritionalInfo();
    
    // When & Then
    assertThatThrownBy(() -> recipeNutritionService.calculatePerServingNutrition(totalInfo, 0))
        .isInstanceOf(InvalidRecipeServingsException.class)
        .hasMessageContaining("Servings must be greater than 0");
    
    assertThatThrownBy(() -> recipeNutritionService.calculatePerServingNutrition(totalInfo, -1))
        .isInstanceOf(InvalidRecipeServingsException.class);
  }
  
  @Test
  @DisplayName("Should handle null nutritional values when calculating per-serving")
  void shouldHandleNullNutritionalValuesWhenCalculatingPerServing() {
    // Given
    NutritionalInfo totalInfo = new NutritionalInfo();
    totalInfo.setCalories(BigDecimal.valueOf(800.0));
    totalInfo.setProtein(null); // Null value
    Integer servings = 4;
    
    // When
    NutritionalInfo result = recipeNutritionService.calculatePerServingNutrition(totalInfo, servings);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(200.0));
    assertThat(result.getProtein()).isNull();
  }
  
  // ==================== updateRecipeNutrition() Tests ====================
  
  @Test
  @DisplayName("Should update recipe nutrition successfully")
  void shouldUpdateRecipeNutritionSuccessfully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeIngredient recipeIngredient = RecipeTestFixtures.createRecipeIngredient();
    Ingredient ingredient = recipeIngredient.getIngredient();
    ingredient.setId(RecipeTestFixtures.TEST_INGREDIENT_ID);
    recipeIngredient.setIngredient(ingredient);
    recipeIngredient.setQuantity(BigDecimal.valueOf(200.0)); // 200g
    recipeIngredient.setUnit("g");
    recipe.setIngredients(List.of(recipeIngredient));
    
    IngredientNutrition ingredientNutrition = new IngredientNutrition();
    ingredientNutrition.setIngredient(ingredient);
    ingredientNutrition.setCaloriesPer100g(BigDecimal.valueOf(250.0)); // 250 cal per 100g
    ingredientNutrition.setProteinPer100g(BigDecimal.valueOf(10.0)); // 10g protein per 100g
    ingredientNutrition.setCarbsPer100g(BigDecimal.valueOf(30.0)); // 30g carbs per 100g
    ingredientNutrition.setFatsPer100g(BigDecimal.valueOf(5.0)); // 5g fats per 100g
    
    when(ingredientNutritionRepository.findByIngredientId(RecipeTestFixtures.TEST_INGREDIENT_ID))
        .thenReturn(Optional.of(ingredientNutrition));
    when(nutritionalInfoRepository.save(any(NutritionalInfo.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // When
    recipeNutritionService.updateRecipeNutrition(recipe);
    
    // Then
    verify(nutritionalInfoRepository).save(any(NutritionalInfo.class));
  }
  
  @Test
  @DisplayName("Should update existing nutritional info when present")
  void shouldUpdateExistingNutritionalInfoWhenPresent() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipeWithNutrition();
    RecipeIngredient recipeIngredient = RecipeTestFixtures.createRecipeIngredient();
    Ingredient ingredient = recipeIngredient.getIngredient();
    ingredient.setId(RecipeTestFixtures.TEST_INGREDIENT_ID);
    recipeIngredient.setIngredient(ingredient);
    recipeIngredient.setQuantity(BigDecimal.valueOf(200.0)); // 200g
    recipeIngredient.setUnit("g");
    recipe.setIngredients(List.of(recipeIngredient));
    
    IngredientNutrition ingredientNutrition = new IngredientNutrition();
    ingredientNutrition.setIngredient(ingredient);
    ingredientNutrition.setCaloriesPer100g(BigDecimal.valueOf(250.0)); // 250 cal per 100g
    ingredientNutrition.setProteinPer100g(BigDecimal.valueOf(10.0)); // 10g protein per 100g
    ingredientNutrition.setCarbsPer100g(BigDecimal.valueOf(30.0)); // 30g carbs per 100g
    ingredientNutrition.setFatsPer100g(BigDecimal.valueOf(5.0)); // 5g fats per 100g
    
    when(ingredientNutritionRepository.findByIngredientId(RecipeTestFixtures.TEST_INGREDIENT_ID))
        .thenReturn(Optional.of(ingredientNutrition));
    when(nutritionalInfoRepository.save(any(NutritionalInfo.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // When
    recipeNutritionService.updateRecipeNutrition(recipe);
    
    // Then
    verify(nutritionalInfoRepository).save(any(NutritionalInfo.class));
  }
}
