package com.vallexia.recipe.unit.util;

import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.IngredientTranslation;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.entity.RecipeTranslation;
import com.vallexia.recipe.repository.IngredientTranslationRepository;
import com.vallexia.recipe.repository.RecipeTranslationRepository;
import com.vallexia.recipe.util.TranslationResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TranslationResolver.
 * Tests translation resolution for recipes and ingredients with locale validation and fallback logic.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TranslationResolver Unit Tests")
class TranslationResolverTest {
  
  @Mock
  private RecipeTranslationRepository recipeTranslationRepository;
  
  @Mock
  private IngredientTranslationRepository ingredientTranslationRepository;
  
  @InjectMocks
  private TranslationResolver translationResolver;
  
  // ==================== resolveRecipeContent() Tests ====================
  
  @Test
  @DisplayName("Should return null when recipe is null")
  void resolveRecipeContent_shouldReturnNullWhenRecipeIsNull() {
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(null, "en");
    
    // Then
    assertThat(result).isNull();
    verifyNoInteractions(recipeTranslationRepository);
  }
  
  @Test
  @DisplayName("Should return base content when user locale matches base locale")
  void resolveRecipeContent_shouldReturnBaseContentWhenLocaleMatches() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, "en");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Base Name");
    assertThat(result.description()).isEqualTo("Base Description");
    assertThat(result.instructions()).isEqualTo("Base Instructions");
    verify(recipeTranslationRepository, never()).findByRecipeIdAndLocale(any(), any());
  }
  
  @Test
  @DisplayName("Should return translated content when translation exists")
  void resolveRecipeContent_shouldReturnTranslatedContentWhenTranslationExists() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    RecipeTranslation translation = new RecipeTranslation();
    translation.setName("Translated Name");
    translation.setDescription("Translated Description");
    translation.setInstructions("Translated Instructions");
    
    when(recipeTranslationRepository.findByRecipeIdAndLocale(1L, "da"))
        .thenReturn(Optional.of(translation));
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, "da");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Translated Name");
    assertThat(result.description()).isEqualTo("Translated Description");
    assertThat(result.instructions()).isEqualTo("Translated Instructions");
    verify(recipeTranslationRepository).findByRecipeIdAndLocale(1L, "da");
  }
  
  @Test
  @DisplayName("Should fallback to base content when translation not found")
  void resolveRecipeContent_shouldFallbackToBaseContentWhenTranslationNotFound() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    when(recipeTranslationRepository.findByRecipeIdAndLocale(1L, "da"))
        .thenReturn(Optional.empty());
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, "da");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Base Name");
    assertThat(result.description()).isEqualTo("Base Description");
    assertThat(result.instructions()).isEqualTo("Base Instructions");
    verify(recipeTranslationRepository).findByRecipeIdAndLocale(1L, "da");
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is null")
  void resolveRecipeContent_shouldDefaultToEnWhenLocaleIsNull() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, null);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Base Name");
    assertThat(result.description()).isEqualTo("Base Description");
    assertThat(result.instructions()).isEqualTo("Base Instructions");
    // Should match base locale, so no repository call
    verify(recipeTranslationRepository, never()).findByRecipeIdAndLocale(any(), any());
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is empty")
  void resolveRecipeContent_shouldDefaultToEnWhenLocaleIsEmpty() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, "");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Base Name");
    verify(recipeTranslationRepository, never()).findByRecipeIdAndLocale(any(), any());
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is blank")
  void resolveRecipeContent_shouldDefaultToEnWhenLocaleIsBlank() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, "   ");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Base Name");
    verify(recipeTranslationRepository, never()).findByRecipeIdAndLocale(any(), any());
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is invalid")
  void resolveRecipeContent_shouldDefaultToEnWhenLocaleIsInvalid() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, "fr");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Base Name");
    // Should match base locale (en), so no repository call
    verify(recipeTranslationRepository, never()).findByRecipeIdAndLocale(any(), any());
  }
  
  @Test
  @DisplayName("Should handle case-insensitive locale matching")
  void resolveRecipeContent_shouldHandleCaseInsensitiveLocale() {
    // Given
    Recipe recipe = new Recipe();
    recipe.setId(1L);
    recipe.setName("Base Name");
    recipe.setDescription("Base Description");
    recipe.setInstructions("Base Instructions");
    recipe.setBaseLocale("en");
    
    RecipeTranslation translation = new RecipeTranslation();
    translation.setName("Translated Name");
    translation.setDescription("Translated Description");
    translation.setInstructions("Translated Instructions");
    
    // SupportedLocale.fromCode normalizes to lowercase, so "DA" becomes "da"
    when(recipeTranslationRepository.findByRecipeIdAndLocale(1L, "da"))
        .thenReturn(Optional.of(translation));
    
    // When
    TranslationResolver.RecipeContent result = translationResolver.resolveRecipeContent(recipe, "DA");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Translated Name");
    verify(recipeTranslationRepository).findByRecipeIdAndLocale(1L, "da");
  }
  
  // ==================== resolveIngredientName() Tests ====================
  
  @Test
  @DisplayName("Should return null when ingredient is null")
  void resolveIngredientName_shouldReturnNullWhenIngredientIsNull() {
    // When
    String result = translationResolver.resolveIngredientName(null, "en");
    
    // Then
    assertThat(result).isNull();
    verifyNoInteractions(ingredientTranslationRepository);
  }
  
  @Test
  @DisplayName("Should return translated name when translation exists")
  void resolveIngredientName_shouldReturnTranslatedNameWhenTranslationExists() {
    // Given
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Flour");
    
    IngredientTranslation translation = new IngredientTranslation();
    translation.setName("Harina");
    
    when(ingredientTranslationRepository.findByIngredientIdAndLocale(1L, "da"))
        .thenReturn(Optional.of(translation));
    
    // When
    String result = translationResolver.resolveIngredientName(ingredient, "da");
    
    // Then
    assertThat(result).isEqualTo("Harina");
    verify(ingredientTranslationRepository).findByIngredientIdAndLocale(1L, "da");
  }
  
  @Test
  @DisplayName("Should fallback to base name when translation not found")
  void resolveIngredientName_shouldFallbackToBaseNameWhenTranslationNotFound() {
    // Given
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Flour");
    
    when(ingredientTranslationRepository.findByIngredientIdAndLocale(1L, "da"))
        .thenReturn(Optional.empty());
    
    // When
    String result = translationResolver.resolveIngredientName(ingredient, "da");
    
    // Then
    assertThat(result).isEqualTo("Flour");
    verify(ingredientTranslationRepository).findByIngredientIdAndLocale(1L, "da");
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is null for ingredient")
  void resolveIngredientName_shouldDefaultToEnWhenLocaleIsNull() {
    // Given
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Flour");
    
    // When
    String result = translationResolver.resolveIngredientName(ingredient, null);
    
    // Then
    assertThat(result).isEqualTo("Flour");
    // Since default is "en" and no translation exists, should fallback to base name
    verify(ingredientTranslationRepository).findByIngredientIdAndLocale(1L, "en");
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is empty for ingredient")
  void resolveIngredientName_shouldDefaultToEnWhenLocaleIsEmpty() {
    // Given
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Flour");
    
    // When
    String result = translationResolver.resolveIngredientName(ingredient, "");
    
    // Then
    assertThat(result).isEqualTo("Flour");
    verify(ingredientTranslationRepository).findByIngredientIdAndLocale(1L, "en");
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is blank for ingredient")
  void resolveIngredientName_shouldDefaultToEnWhenLocaleIsBlank() {
    // Given
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Flour");
    
    // When
    String result = translationResolver.resolveIngredientName(ingredient, "   ");
    
    // Then
    assertThat(result).isEqualTo("Flour");
    verify(ingredientTranslationRepository).findByIngredientIdAndLocale(1L, "en");
  }
  
  @Test
  @DisplayName("Should default to 'en' when locale is invalid for ingredient")
  void resolveIngredientName_shouldDefaultToEnWhenLocaleIsInvalid() {
    // Given
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Flour");
    
    // When
    String result = translationResolver.resolveIngredientName(ingredient, "fr");
    
    // Then
    assertThat(result).isEqualTo("Flour");
    verify(ingredientTranslationRepository).findByIngredientIdAndLocale(1L, "en");
  }
  
  @Test
  @DisplayName("Should handle case-insensitive locale for ingredient")
  void resolveIngredientName_shouldHandleCaseInsensitiveLocale() {
    // Given
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Flour");
    
    IngredientTranslation translation = new IngredientTranslation();
    translation.setName("Harina");
    
    // SupportedLocale.fromCode normalizes to lowercase, so "DA" becomes "da"
    when(ingredientTranslationRepository.findByIngredientIdAndLocale(1L, "da"))
        .thenReturn(Optional.of(translation));
    
    // When
    String result = translationResolver.resolveIngredientName(ingredient, "DA");
    
    // Then
    assertThat(result).isEqualTo("Harina");
    verify(ingredientTranslationRepository).findByIngredientIdAndLocale(1L, "da");
  }
}
