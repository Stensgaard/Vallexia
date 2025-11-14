package com.vallexia.recipe.unit.mapper;

import com.vallexia.recipe.dto.*;
import com.vallexia.recipe.entity.*;
import com.vallexia.recipe.fixtures.RecipeTestFixtures;
import com.vallexia.recipe.mapper.RecipeMapper;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RecipeMapper.
 * Tests entity-to-DTO mapping with real MapStruct implementation.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootTest(classes = {
    com.vallexia.recipe.mapper.RecipeMapperImpl.class
})
@ActiveProfiles("test")
@DisplayName("RecipeMapper Unit Tests")
class RecipeMapperTest {
  
  @Autowired
  private RecipeMapper recipeMapper;
  
  // ==================== toRecipeDto() Tests ====================
  
  @Test
  @DisplayName("Should map Recipe entity to RecipeDto with favorite flag")
  void shouldMapRecipeEntityToRecipeDtoWithFavoriteFlag() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    Boolean isFavorite = true;
    
    // When
    RecipeDto dto = recipeMapper.toRecipeDto(recipe, isFavorite);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(recipe.getId());
    assertThat(dto.getName()).isEqualTo(recipe.getName());
    assertThat(dto.getIsFavorite()).isTrue();
  }
  
  @Test
  @DisplayName("Should map Recipe entity to RecipeDto without favorite flag")
  void shouldMapRecipeEntityToRecipeDtoWithoutFavoriteFlag() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    
    // When
    RecipeDto dto = recipeMapper.toRecipeDto(recipe);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(recipe.getId());
    assertThat(dto.getIsFavorite()).isFalse(); // Default value
  }
  
  @Test
  @DisplayName("Should map creator information correctly")
  void shouldMapCreatorInformationCorrectly() {
    // Given
    Recipe recipe = RecipeTestFixtures.createRecipe();
    User creator = UserTestFixtures.createUser();
    recipe.setCreator(creator);
    
    // When
    RecipeDto dto = recipeMapper.toRecipeDto(recipe);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCreatorId()).isEqualTo(creator.getId());
    assertThat(dto.getCreatorUsername()).isEqualTo(creator.getUsername());
  }
  
  // ==================== toRecipe() Tests ====================
  
  @Test
  @DisplayName("Should map CreateRecipeDto to Recipe entity")
  void shouldMapCreateRecipeDtoToRecipeEntity() {
    // Given
    CreateRecipeDto dto = RecipeTestFixtures.createCreateRecipeDto();
    
    // When
    Recipe recipe = recipeMapper.toRecipe(dto);
    
    // Then
    assertThat(recipe).isNotNull();
    assertThat(recipe.getName()).isEqualTo(dto.getName());
    assertThat(recipe.getDescription()).isEqualTo(dto.getDescription());
    assertThat(recipe.getId()).isNull(); // Should be ignored
    assertThat(recipe.getCreator()).isNull(); // Should be ignored
  }
  
  // ==================== toIngredientDto() Tests ====================
  
  @Test
  @DisplayName("Should map RecipeIngredient to IngredientDto")
  void shouldMapRecipeIngredientToIngredientDto() {
    // Given
    RecipeIngredient recipeIngredient = RecipeTestFixtures.createRecipeIngredient();
    
    // When
    IngredientDto dto = recipeMapper.toIngredientDto(recipeIngredient);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getName()).isEqualTo(recipeIngredient.getIngredient().getName());
    assertThat(dto.getQuantity()).isEqualTo(recipeIngredient.getQuantity());
    assertThat(dto.getUnit()).isEqualTo(recipeIngredient.getUnit());
  }
  
  // ==================== toNutritionalInfoDto() Tests ====================
  
  @Test
  @DisplayName("Should map NutritionalInfo to NutritionalInfoDto")
  void shouldMapNutritionalInfoToNutritionalInfoDto() {
    // Given
    NutritionalInfo nutritionalInfo = RecipeTestFixtures.createNutritionalInfo();
    
    // When
    NutritionalInfoDto dto = recipeMapper.toNutritionalInfoDto(nutritionalInfo);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCalories()).isEqualTo(nutritionalInfo.getCalories());
    assertThat(dto.getProtein()).isEqualTo(nutritionalInfo.getProtein());
    assertThat(dto.getCarbs()).isEqualTo(nutritionalInfo.getCarbs());
    assertThat(dto.getFats()).isEqualTo(nutritionalInfo.getFats());
  }
  
  @Test
  @DisplayName("Should handle null values correctly")
  void shouldHandleNullValuesCorrectly() {
    // When
    RecipeDto dto = recipeMapper.toRecipeDto((Recipe) null);
    
    // Then
    assertThat(dto).isNull();
  }
}
