package com.vallexia.recipe.unit.util;

import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.IngredientTranslation;
import com.vallexia.recipe.repository.IngredientTranslationRepository;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for TranslationResolver.
 * Tests translation resolution for ingredients with locale validation and fallback logic.
 * 
 * Note: Recipe translations are now handled by RecipeLocalizationService using Google Cloud Translation API.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TranslationResolver Unit Tests")
class TranslationResolverTest {
  
  @Mock
  private IngredientTranslationRepository ingredientTranslationRepository;
  
  @InjectMocks
  private TranslationResolver translationResolver;
  
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
