package com.vallexia.recipe.unit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.RecipeDto;
import com.vallexia.recipe.entity.Ingredient;
import com.vallexia.recipe.entity.RecipeTranslationCache;
import com.vallexia.recipe.integration.client.GoogleTranslationClient;
import com.vallexia.recipe.integration.exception.GoogleTranslationException;
import com.vallexia.recipe.repository.IngredientRepository;
import com.vallexia.recipe.repository.RecipeTranslationCacheRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeLocalizationService.
 * Tests recipe DTO localization with Google Cloud Translation API and translation caching.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecipeLocalizationService Unit Tests")
class RecipeLocalizationServiceTest {
  
  private static final Integer TEST_SPOONACULAR_ID = 12345;
  
  @Mock
  private GoogleTranslationClient translationClient;
  
  @Mock
  private RecipeTranslationCacheRepository translationCacheRepository;
  
  @Mock
  private TranslationResolver translationResolver;
  
  @Mock
  private IngredientRepository ingredientRepository;
  
  @Mock
  private ObjectMapper objectMapper;
  
  @Mock
  private com.vallexia.recipe.service.RecipeCacheService recipeCacheService;
  
  @InjectMocks
  private RecipeLocalizationService recipeLocalizationService;
  
  // ==================== enrichWithTranslations() Tests ====================
  
  @Test
  @DisplayName("Should skip translation when locale is English")
  void shouldSkipTranslationWhenLocaleIsEnglish() {
    // Given
    RecipeDto dto = new RecipeDto();
    dto.setSpoonacularId(TEST_SPOONACULAR_ID);
    dto.setName("Test Recipe");
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, TEST_SPOONACULAR_ID, "en");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Recipe");
    verify(translationClient, never()).translateText(anyString(), anyString());
    verify(translationCacheRepository, never()).findBySpoonacularIdAndLocaleAndNotExpired(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should use cached translation when available")
  @SuppressWarnings("unchecked")
  void shouldUseCachedTranslationWhenAvailable() throws Exception {
    // Given
    RecipeDto dto = new RecipeDto();
    dto.setSpoonacularId(TEST_SPOONACULAR_ID);
    dto.setName("Test Recipe");
    dto.setDescription("Test Description");
    dto.setInstructions("Test Instructions");
    
    RecipeTranslationCache cachedTranslation = new RecipeTranslationCache();
    cachedTranslation.setSpoonacularId(TEST_SPOONACULAR_ID);
    cachedTranslation.setLocale("da");
    cachedTranslation.setTranslatedName("Test Opskrift");
    cachedTranslation.setTranslatedDescription("Test Beskrivelse");
    cachedTranslation.setTranslatedInstructions("Test Instruktioner");
    cachedTranslation.setTranslatedIngredients("[\"Ingrediens 1\", \"Ingrediens 2\"]");
    cachedTranslation.setExpiresAt(LocalDateTime.now().plusHours(1));
    
    when(translationCacheRepository.findBySpoonacularIdAndLocaleAndNotExpired(
            eq(TEST_SPOONACULAR_ID), eq("da"), any(LocalDateTime.class)))
        .thenReturn(Optional.of(cachedTranslation));
    // TypeReference is a generic type, unchecked conversion is unavoidable when mocking
    when(objectMapper.readValue(anyString(), any(TypeReference.class)))
        .thenReturn(List.of("Ingrediens 1", "Ingrediens 2"));
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, TEST_SPOONACULAR_ID, "da");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Opskrift");
    assertThat(result.getDescription()).isEqualTo("Test Beskrivelse");
    assertThat(result.getInstructions()).isEqualTo("Test Instruktioner");
    verify(translationClient, never()).translateText(anyString(), anyString());
    verify(translationCacheRepository).findBySpoonacularIdAndLocaleAndNotExpired(
        eq(TEST_SPOONACULAR_ID), eq("da"), any(LocalDateTime.class));
  }
  
  @Test
  @DisplayName("Should translate and cache when cache miss")
  void shouldTranslateAndCacheWhenCacheMiss() {
    // Given
    RecipeDto dto = new RecipeDto();
    dto.setSpoonacularId(TEST_SPOONACULAR_ID);
    dto.setName("Test Recipe");
    dto.setDescription("Test Description");
    dto.setInstructions("Test Instructions");
    
    IngredientDto ingredientDto = new IngredientDto();
    ingredientDto.setName("Flour");
    dto.setIngredients(List.of(ingredientDto));
    
    when(translationCacheRepository.findBySpoonacularIdAndLocaleAndNotExpired(
            eq(TEST_SPOONACULAR_ID), eq("da"), any(LocalDateTime.class)))
        .thenReturn(Optional.empty());
    when(translationClient.translateText("Test Recipe", "da"))
        .thenReturn("Test Opskrift");
    when(translationClient.translateText("Test Description", "da"))
        .thenReturn("Test Beskrivelse");
    when(translationClient.translateText("Test Instructions", "da"))
        .thenReturn("Test Instruktioner");
    when(translationClient.translateTexts(List.of("Flour"), "da"))
        .thenReturn(List.of("Mel"));
    try {
      when(objectMapper.writeValueAsString(anyList()))
          .thenReturn("[\"Mel\"]");
    } catch (Exception e) {
      // Mock setup
    }
    when(translationCacheRepository.save(any(RecipeTranslationCache.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, TEST_SPOONACULAR_ID, "da");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Opskrift");
    verify(translationClient).translateText("Test Recipe", "da");
    verify(translationCacheRepository).save(any(RecipeTranslationCache.class));
  }
  
  @Test
  @DisplayName("Should enrich ingredient names with translations")
  void shouldEnrichIngredientNamesWithTranslations() {
    // Given
    RecipeDto dto = new RecipeDto();
    dto.setSpoonacularId(TEST_SPOONACULAR_ID);
    dto.setName("Test Recipe");
    
    IngredientDto ingredientDto1 = new IngredientDto();
    ingredientDto1.setIngredientId(1L);
    ingredientDto1.setName("Flour");
    
    IngredientDto ingredientDto2 = new IngredientDto();
    ingredientDto2.setIngredientId(2L);
    ingredientDto2.setName("Eggs");
    
    dto.setIngredients(List.of(ingredientDto1, ingredientDto2));
    
    Ingredient ingredient1 = new Ingredient();
    ingredient1.setId(1L);
    ingredient1.setName("Flour");
    
    Ingredient ingredient2 = new Ingredient();
    ingredient2.setId(2L);
    ingredient2.setName("Eggs");
    
    when(translationCacheRepository.findBySpoonacularIdAndLocaleAndNotExpired(
            eq(TEST_SPOONACULAR_ID), eq("da"), any(LocalDateTime.class)))
        .thenReturn(Optional.empty());
    when(translationClient.translateText(anyString(), eq("da")))
        .thenAnswer(invocation -> invocation.getArgument(0)); // Return as-is for simplicity
    when(translationClient.translateTexts(anyList(), eq("da")))
        .thenAnswer(invocation -> invocation.getArgument(0)); // Return as-is
    when(ingredientRepository.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(ingredient1, ingredient2));
    when(translationResolver.resolveIngredientName(ingredient1, "da"))
        .thenReturn("Mel");
    when(translationResolver.resolveIngredientName(ingredient2, "da"))
        .thenReturn("Æg");
    try {
      when(objectMapper.writeValueAsString(anyList()))
          .thenReturn("[]");
    } catch (Exception e) {
      // Mock setup
    }
    when(translationCacheRepository.save(any(RecipeTranslationCache.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, TEST_SPOONACULAR_ID, "da");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getIngredients()).hasSize(2);
    assertThat(result.getIngredients().get(0).getName()).isEqualTo("Mel");
    assertThat(result.getIngredients().get(1).getName()).isEqualTo("Æg");
    verify(ingredientRepository).findAllById(List.of(1L, 2L));
  }
  
  @Test
  @DisplayName("Should fallback to English when translation fails")
  void shouldFallbackToEnglishWhenTranslationFails() {
    // Given
    RecipeDto dto = new RecipeDto();
    dto.setSpoonacularId(TEST_SPOONACULAR_ID);
    dto.setName("Test Recipe");
    
    when(translationCacheRepository.findBySpoonacularIdAndLocaleAndNotExpired(
            eq(TEST_SPOONACULAR_ID), eq("da"), any(LocalDateTime.class)))
        .thenReturn(Optional.empty());
    when(translationClient.translateText(anyString(), eq("da")))
        .thenThrow(new GoogleTranslationException("Translation failed"));
    
    // When
    RecipeDto result = recipeLocalizationService.enrichWithTranslations(dto, TEST_SPOONACULAR_ID, "da");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Recipe"); // Fallback to original
  }
}
