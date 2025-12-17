package com.vallexia.recipe.fixtures;

import com.vallexia.recipe.entity.*;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.common.enums.SupportedMealCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
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
    recipe.setCategory(SupportedMealCategory.DINNER);
    recipe.setCuisineType(SupportedCuisineType.ITALIAN);
    recipe.setImageUrl("https://example.com/recipe.jpg");
    recipe.setTags(new HashSet<>(Set.of("easy", "italian", "dinner")));
    recipe.setCreatedAt(LocalDateTime.now().minusDays(5));
    recipe.setUpdatedAt(LocalDateTime.now().minusDays(1));
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
  
}
