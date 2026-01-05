package com.vallexia.recipe.fixtures;

import com.vallexia.recipe.entity.Ingredient;

import java.time.LocalDateTime;

/**
 * Test fixtures for recipe testing.
 * Provides reusable test data and builder methods.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-12-09
 */
public class RecipeTestFixtures {
  
  // Test Constants
  public static final Integer TEST_SPOONACULAR_ID = 12345;
  public static final Long TEST_INGREDIENT_ID = 1L;
  public static final String TEST_RECIPE_NAME = "Test Recipe";
  public static final String TEST_RECIPE_DESCRIPTION = "A delicious test recipe";
  public static final String TEST_RECIPE_INSTRUCTIONS = "Step 1: Do this. Step 2: Do that.";
  public static final String TEST_INGREDIENT_NAME = "Flour";
  public static final Integer TEST_PREP_TIME = 15;
  public static final Integer TEST_COOK_TIME = 30;
  public static final Integer TEST_TOTAL_TIME = 45;
  public static final Integer TEST_SERVINGS = 4;
  
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
}
