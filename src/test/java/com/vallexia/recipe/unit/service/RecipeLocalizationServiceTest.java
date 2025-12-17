package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.Recipe;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.repository.IngredientRepository;
import com.vallexia.recipe.service.RecipeLocalizationService;
import com.vallexia.recipe.util.TranslationResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeLocalizationService.
 * Tests recipe DTO localization with translations and batch ingredient loading.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeLocalizationService Unit Tests")
class RecipeLocalizationServiceTest {
  
  @Mock
  private TranslationResolver translationResolver;
  
  @Mock
  private IngredientRepository ingredientRepository;
  
  @InjectMocks
  private RecipeLocalizationService recipeLocalizationService;
  
  // ==================== enrichWithTranslations() Tests ====================
  
  @Test
  @DisplayName("Should enrich recipe DTO with translated content")
  void shouldEnrichRecipeDtoWithTranslatedContent() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    dto.setName("Original Name");
    dto.setDescription("Original Description");
    dto.setInstructions("Original Instructions");
    
    String userLocale = "es";
    String translatedName = "Nombre Traducido";
    String translatedDescription = "Descripción Traducida";
    String translatedInstructions = "Instrucciones Traducidas";
    
    TranslationResolver.RecipeContent translatedContent = new TranslationResolver.RecipeContent(
        translatedName, translatedDescription, translatedInstructions);
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(translatedContent);
    when(ingredientRepository.findAllById(anyList()))
        .thenReturn(List.of());
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(translatedName);
    assertThat(result.getDescription()).isEqualTo(translatedDescription);
    assertThat(result.getInstructions()).isEqualTo(translatedInstructions);
    verify(translationResolver).resolveRecipeContent(recipe, userLocale);
  }
  
  @Test
  @DisplayName("Should enrich recipe DTO with ingredient translations using batch loading")
  void shouldEnrichRecipeDtoWithIngredientTranslationsUsingBatchLoading() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    
    // Create ingredient DTOs
    IngredientDto ingredientDto1 = new IngredientDto();
    ingredientDto1.setIngredientId(1L);
    ingredientDto1.setName("Flour");
    ingredientDto1.setQuantity(BigDecimal.valueOf(2.0));
    ingredientDto1.setUnit("cups");
    
    IngredientDto ingredientDto2 = new IngredientDto();
    ingredientDto2.setIngredientId(2L);
    ingredientDto2.setName("Eggs");
    ingredientDto2.setQuantity(BigDecimal.valueOf(3.0));
    ingredientDto2.setUnit("pieces");
    
    dto.setIngredients(List.of(ingredientDto1, ingredientDto2));
    
    // Create ingredient entities
    Ingredient ingredient1 = RecipeTestFixtures.createIngredient("Flour");
    ingredient1.setId(1L);
    Ingredient ingredient2 = RecipeTestFixtures.createIngredient("Eggs");
    ingredient2.setId(2L);
    
    String userLocale = "es";
    String translatedName1 = "Harina";
    String translatedName2 = "Huevos";
    
    TranslationResolver.RecipeContent recipeContent = new TranslationResolver.RecipeContent(
        recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(recipeContent);
    when(ingredientRepository.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(ingredient1, ingredient2));
    when(translationResolver.resolveIngredientName(ingredient1, userLocale))
        .thenReturn(translatedName1);
    when(translationResolver.resolveIngredientName(ingredient2, userLocale))
        .thenReturn(translatedName2);
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).hasSize(2);
    assertThat(result.getIngredients().get(0).getName()).isEqualTo(translatedName1);
    assertThat(result.getIngredients().get(1).getName()).isEqualTo(translatedName2);
    
    // Verify batch loading was used (only one call to findAllById)
    verify(ingredientRepository, times(1)).findAllById(anyList());
    verify(ingredientRepository, never()).findById(any());
  }
  
  @Test
  @DisplayName("Should handle null ingredients list gracefully")
  void shouldHandleNullIngredientsListGracefully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    dto.setIngredients(null);
    
    String userLocale = "en";
    TranslationResolver.RecipeContent recipeContent = new TranslationResolver.RecipeContent(
        recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(recipeContent);
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).isNull();
    verify(ingredientRepository, never()).findAllById(anyList());
  }
  
  @Test
  @DisplayName("Should handle empty ingredients list gracefully")
  void shouldHandleEmptyIngredientsListGracefully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    dto.setIngredients(new ArrayList<>());
    
    String userLocale = "en";
    TranslationResolver.RecipeContent recipeContent = new TranslationResolver.RecipeContent(
        recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(recipeContent);
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).isEmpty();
    verify(ingredientRepository, never()).findAllById(anyList());
  }
  
  @Test
  @DisplayName("Should handle ingredients with null ingredient IDs")
  void shouldHandleIngredientsWithNullIngredientIds() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    
    IngredientDto ingredientDto = new IngredientDto();
    ingredientDto.setIngredientId(null);
    ingredientDto.setName("Custom Ingredient");
    ingredientDto.setQuantity(BigDecimal.valueOf(1.0));
    
    dto.setIngredients(List.of(ingredientDto));
    
    String userLocale = "en";
    TranslationResolver.RecipeContent recipeContent = new TranslationResolver.RecipeContent(
        recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(recipeContent);
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).hasSize(1);
    assertThat(result.getIngredients().get(0).getName()).isEqualTo("Custom Ingredient");
    verify(ingredientRepository, never()).findAllById(anyList());
  }
  
  @Test
  @DisplayName("Should handle missing ingredients gracefully")
  void shouldHandleMissingIngredientsGracefully() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    
    IngredientDto ingredientDto = new IngredientDto();
    ingredientDto.setIngredientId(999L); // Non-existent ingredient ID
    ingredientDto.setName("Original Name");
    ingredientDto.setQuantity(BigDecimal.valueOf(1.0));
    
    dto.setIngredients(List.of(ingredientDto));
    
    String userLocale = "en";
    TranslationResolver.RecipeContent recipeContent = new TranslationResolver.RecipeContent(
        recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(recipeContent);
    when(ingredientRepository.findAllById(List.of(999L)))
        .thenReturn(List.of()); // Empty list - ingredient not found
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).hasSize(1);
    assertThat(result.getIngredients().get(0).getName()).isEqualTo("Original Name"); // Unchanged
    verify(ingredientRepository).findAllById(List.of(999L));
    verify(translationResolver, never()).resolveIngredientName(any(), any());
  }
  
  @Test
  @DisplayName("Should deduplicate ingredient IDs when batch loading")
  void shouldDeduplicateIngredientIdsWhenBatchLoading() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    
    // Create multiple ingredient DTOs with same ingredient ID
    IngredientDto ingredientDto1 = new IngredientDto();
    ingredientDto1.setIngredientId(1L);
    ingredientDto1.setName("Flour");
    ingredientDto1.setQuantity(BigDecimal.valueOf(2.0));
    
    IngredientDto ingredientDto2 = new IngredientDto();
    ingredientDto2.setIngredientId(1L); // Same ID
    ingredientDto2.setName("Flour");
    ingredientDto2.setQuantity(BigDecimal.valueOf(1.0));
    
    dto.setIngredients(List.of(ingredientDto1, ingredientDto2));
    
    Ingredient ingredient = RecipeTestFixtures.createIngredient("Flour");
    ingredient.setId(1L);
    
    String userLocale = "es";
    String translatedName = "Harina";
    
    TranslationResolver.RecipeContent recipeContent = new TranslationResolver.RecipeContent(
        recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(recipeContent);
    when(ingredientRepository.findAllById(List.of(1L))) // Only one ID in the list
        .thenReturn(List.of(ingredient));
    when(translationResolver.resolveIngredientName(ingredient, userLocale))
        .thenReturn(translatedName);
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).hasSize(2);
    assertThat(result.getIngredients().get(0).getName()).isEqualTo(translatedName);
    assertThat(result.getIngredients().get(1).getName()).isEqualTo(translatedName);
    
    // Verify batch loading was called with deduplicated IDs (only one ID)
    verify(ingredientRepository, times(1)).findAllById(List.of(1L));
  }
  
  @Test
  @DisplayName("Should enrich recipe with multiple ingredients efficiently")
  void shouldEnrichRecipeWithMultipleIngredientsEfficiently() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    RecipeDto dto = new RecipeDto();
    dto.setId(recipe.getId());
    
    // Create 5 ingredient DTOs
    List<IngredientDto> ingredientDtos = new ArrayList<>();
    List<Ingredient> ingredients = new ArrayList<>();
    List<Long> ingredientIds = new ArrayList<>();
    
    for (int i = 1; i <= 5; i++) {
      IngredientDto ingredientDto = new IngredientDto();
      ingredientDto.setIngredientId((long) i);
      ingredientDto.setName("Ingredient " + i);
      ingredientDto.setQuantity(BigDecimal.valueOf(i));
      ingredientDtos.add(ingredientDto);
      
      Ingredient ingredient = RecipeTestFixtures.createIngredient("Ingredient " + i);
      ingredient.setId((long) i);
      ingredients.add(ingredient);
      ingredientIds.add((long) i);
    }
    
    dto.setIngredients(ingredientDtos);
    
    String userLocale = "fr";
    TranslationResolver.RecipeContent recipeContent = new TranslationResolver.RecipeContent(
        recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    
    when(translationResolver.resolveRecipeContent(recipe, userLocale))
        .thenReturn(recipeContent);
    when(ingredientRepository.findAllById(ingredientIds))
        .thenReturn(ingredients);
    
    // Mock translation for each ingredient
    for (Ingredient ingredient : ingredients) {
      when(translationResolver.resolveIngredientName(eq(ingredient), eq(userLocale)))
          .thenReturn("Translated " + ingredient.getName());
    }
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, recipe, userLocale);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).hasSize(5);
    
    // Verify all ingredients were translated
    for (int i = 0; i < 5; i++) {
      assertThat(result.getIngredients().get(i).getName())
          .isEqualTo("Translated Ingredient " + (i + 1));
    }
    
    // Verify batch loading was used (only one call)
    verify(ingredientRepository, times(1)).findAllById(ingredientIds);
    verify(ingredientRepository, never()).findById(any());
  }
}
