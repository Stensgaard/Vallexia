package com.vallexia.recipe.fixtures;

import com.vallexia.recipe.dto.CreateRecipeDto;
import com.vallexia.recipe.dto.IngredientDto;
import com.vallexia.recipe.dto.NutritionalInfoDto;
import com.vallexia.recipe.dto.UpdateRecipeDto;
import com.vallexia.recipe.entity.*;
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.common.enums.SupportedMealCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test fixtures for recipe testing.
 * Provides reusable test data and builder methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
public class RecipeTestFixtures {
  
  // Test Constants
  public static final Long TEST_RECIPE_ID = 1L;
  public static final Long TEST_INGREDIENT_ID = 1L;
  public static final Long TEST_RECIPE_INGREDIENT_ID = 1L;
  public static final Long TEST_NUTRITIONAL_INFO_ID = 1L;
  public static final String TEST_RECIPE_NAME = "Test Recipe";
  public static final String TEST_RECIPE_DESCRIPTION = "A delicious test recipe";
  public static final String TEST_RECIPE_INSTRUCTIONS = "Step 1: Do this. Step 2: Do that.";
  public static final String TEST_INGREDIENT_NAME = "Flour";
  public static final Integer TEST_PREP_TIME = 15;
  public static final Integer TEST_COOK_TIME = 30;
  public static final Integer TEST_TOTAL_TIME = 45;
  public static final Integer TEST_SERVINGS = 4;
  public static final BigDecimal TEST_CALORIES = BigDecimal.valueOf(250.0);
  public static final BigDecimal TEST_PROTEIN = BigDecimal.valueOf(10.0);
  public static final BigDecimal TEST_CARBS = BigDecimal.valueOf(30.0);
  public static final BigDecimal TEST_FATS = BigDecimal.valueOf(8.0);
  
  /**
   * Creates a standard test recipe.
   */
  public static Recipe createRecipe() {
    return createRecipe(TEST_RECIPE_ID);
  }
  
  /**
   * Creates a recipe with specified ID.
   */
  public static Recipe createRecipe(Long id) {
    Recipe recipe = new Recipe();
    recipe.setId(id);
    recipe.setCreator(UserTestFixtures.createUser());
    recipe.setName(TEST_RECIPE_NAME);
    recipe.setDescription(TEST_RECIPE_DESCRIPTION);
    recipe.setInstructions(TEST_RECIPE_INSTRUCTIONS);
    recipe.setPrepTimeMinutes(TEST_PREP_TIME);
    recipe.setCookTimeMinutes(TEST_COOK_TIME);
    recipe.setTotalTimeMinutes(TEST_TOTAL_TIME);
    recipe.setServings(TEST_SERVINGS);
    recipe.setDifficultyLevel(DifficultyLevel.MEDIUM);
    recipe.setCategory(SupportedMealCategory.DINNER);
    recipe.setCuisineType(SupportedCuisineType.ITALIAN);
    recipe.setImageUrl("https://example.com/recipe.jpg");
    recipe.setIsPublic(true);
    recipe.setTags(new HashSet<>(Set.of("easy", "italian", "dinner")));
    recipe.setCreatedAt(LocalDateTime.now().minusDays(5));
    recipe.setUpdatedAt(LocalDateTime.now().minusDays(1));
    return recipe;
  }
  
  /**
   * Creates a private recipe.
   */
  public static Recipe createPrivateRecipe() {
    Recipe recipe = createRecipe();
    recipe.setIsPublic(false);
    return recipe;
  }
  
  /**
   * Creates a recipe with nutritional info.
   */
  public static Recipe createRecipeWithNutrition() {
    Recipe recipe = createRecipe();
    NutritionalInfo nutritionalInfo = createNutritionalInfo(recipe);
    recipe.setNutritionalInfo(nutritionalInfo);
    return recipe;
  }
  
  /**
   * Creates a test ingredient.
   */
  public static Ingredient createIngredient() {
    return createIngredient(TEST_INGREDIENT_NAME);
  }
  
  /**
   * Creates an ingredient with specified name.
   */
  public static Ingredient createIngredient(String name) {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(TEST_INGREDIENT_ID);
    ingredient.setName(name);
    ingredient.setCreatedAt(LocalDateTime.now().minusDays(10));
    ingredient.setUpdatedAt(LocalDateTime.now().minusDays(1));
    return ingredient;
  }
  
  /**
   * Creates a recipe ingredient.
   */
  public static RecipeIngredient createRecipeIngredient() {
    Recipe recipe = createRecipe();
    Ingredient ingredient = createIngredient();
    return createRecipeIngredient(recipe, ingredient, BigDecimal.valueOf(1.0), "cup");
  }
  
  /**
   * Creates a recipe ingredient with specified values.
   */
  public static RecipeIngredient createRecipeIngredient(Recipe recipe, Ingredient ingredient, 
                                                         BigDecimal quantity, String unit) {
    RecipeIngredient recipeIngredient = new RecipeIngredient();
    recipeIngredient.setId(TEST_RECIPE_INGREDIENT_ID);
    recipeIngredient.setRecipe(recipe);
    recipeIngredient.setIngredient(ingredient);
    recipeIngredient.setQuantity(quantity);
    recipeIngredient.setUnit(unit);
    recipeIngredient.setNotes("Optional notes");
    recipeIngredient.setDisplayOrder(0);
    return recipeIngredient;
  }
  
  /**
   * Creates nutritional info.
   */
  public static NutritionalInfo createNutritionalInfo() {
    Recipe recipe = createRecipe();
    return createNutritionalInfo(recipe);
  }
  
  /**
   * Creates nutritional info for a recipe.
   */
  public static NutritionalInfo createNutritionalInfo(Recipe recipe) {
    NutritionalInfo nutritionalInfo = new NutritionalInfo();
    nutritionalInfo.setId(TEST_NUTRITIONAL_INFO_ID);
    nutritionalInfo.setRecipe(recipe);
    nutritionalInfo.setCalories(TEST_CALORIES);
    nutritionalInfo.setProtein(TEST_PROTEIN);
    nutritionalInfo.setCarbs(TEST_CARBS);
    nutritionalInfo.setFats(TEST_FATS);
    nutritionalInfo.setFiber(BigDecimal.valueOf(5.0));
    nutritionalInfo.setSodium(BigDecimal.valueOf(500.0));
    nutritionalInfo.setSugar(BigDecimal.valueOf(10.0));
    nutritionalInfo.setPerServing(false);
    nutritionalInfo.setCreatedAt(LocalDateTime.now().minusDays(5));
    nutritionalInfo.setUpdatedAt(LocalDateTime.now().minusDays(1));
    return nutritionalInfo;
  }
  
  /**
   * Creates a CreateRecipeDto.
   */
  public static CreateRecipeDto createCreateRecipeDto() {
    CreateRecipeDto dto = new CreateRecipeDto();
    dto.setName(TEST_RECIPE_NAME);
    dto.setDescription(TEST_RECIPE_DESCRIPTION);
    dto.setInstructions(TEST_RECIPE_INSTRUCTIONS);
    dto.setPrepTimeMinutes(TEST_PREP_TIME);
    dto.setCookTimeMinutes(TEST_COOK_TIME);
    dto.setServings(TEST_SERVINGS);
    dto.setDifficultyLevel(DifficultyLevel.MEDIUM);
    dto.setCategory(SupportedMealCategory.DINNER);
    dto.setCuisineType(SupportedCuisineType.ITALIAN);
    dto.setImageUrl("https://example.com/recipe.jpg");
    dto.setIsPublic(true);
    
    // Add ingredients
    List<IngredientDto> ingredients = new ArrayList<>();
    IngredientDto ingredient1 = new IngredientDto();
    ingredient1.setName("Flour");
    ingredient1.setQuantity(BigDecimal.valueOf(2.0));
    ingredient1.setUnit("cups");
    ingredient1.setDisplayOrder(0);
    ingredients.add(ingredient1);
    
    IngredientDto ingredient2 = new IngredientDto();
    ingredient2.setName("Eggs");
    ingredient2.setQuantity(BigDecimal.valueOf(3.0));
    ingredient2.setUnit("pieces");
    ingredient2.setDisplayOrder(1);
    ingredients.add(ingredient2);
    
    dto.setIngredients(ingredients);
    
    // Add nutritional info
    NutritionalInfoDto nutritionalInfo = new NutritionalInfoDto();
    nutritionalInfo.setCalories(TEST_CALORIES);
    nutritionalInfo.setProtein(TEST_PROTEIN);
    nutritionalInfo.setCarbs(TEST_CARBS);
    nutritionalInfo.setFats(TEST_FATS);
    dto.setNutritionalInfo(nutritionalInfo);
    
    // Add tags
    dto.setTags(new HashSet<>(Set.of("easy", "italian")));
    
    return dto;
  }
  
  /**
   * Creates an UpdateRecipeDto.
   */
  public static UpdateRecipeDto createUpdateRecipeDto() {
    UpdateRecipeDto dto = new UpdateRecipeDto();
    dto.setName("Updated Recipe Name");
    dto.setDescription("Updated description");
    dto.setServings(6);
    dto.setDifficultyLevel(DifficultyLevel.EASY);
    return dto;
  }
}
