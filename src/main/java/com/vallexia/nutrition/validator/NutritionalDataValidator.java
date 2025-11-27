package com.vallexia.nutrition.validator;

import com.vallexia.nutrition.exception.InvalidNutritionalDataException;
import com.vallexia.user.entity.NutritionalGoals;

import java.math.BigDecimal;

/**
 * Validator for nutritional data to ensure values are within acceptable ranges.
 * 
 * <p>Acceptable ranges:
 * <ul>
 *   <li>Calories: 500 - 10,000 kcal (required)</li>
 *   <li>Protein: 0 - 1,000 g (optional)</li>
 *   <li>Carbohydrates: 0 - 1,500 g (optional)</li>
 *   <li>Fats: 0 - 500 g (optional)</li>
 * </ul>
 * 
 * <p><b>Note:</b> Macros (protein, carbs, fats) are optional and can be null.
 * Only daily calories is required. When macros are provided, they must be within
 * the specified ranges.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
public class NutritionalDataValidator {
  
  // Acceptable ranges for nutritional values
  private static final BigDecimal MIN_CALORIES = BigDecimal.valueOf(500);
  private static final BigDecimal MAX_CALORIES = BigDecimal.valueOf(10000);
  
  private static final BigDecimal MIN_MACROS = BigDecimal.ZERO;
  private static final BigDecimal MAX_PROTEIN = BigDecimal.valueOf(1000);
  private static final BigDecimal MAX_CARBS = BigDecimal.valueOf(1500);
  private static final BigDecimal MAX_FATS = BigDecimal.valueOf(500);
  
  /**
   * Validates a NutritionalGoals object.
   * 
   * @param goals the nutritional goals to validate
   * @throws InvalidNutritionalDataException if validation fails
   */
  public static void validateNutritionalGoals(NutritionalGoals goals) {
    if (goals == null) {
      throw new InvalidNutritionalDataException("Nutritional goals cannot be null");
    }
    
    validateCalories(goals.getDailyCalories());
    
    if (goals.getDailyProtein() != null) {
      validateProtein(goals.getDailyProtein());
    }
    
    if (goals.getDailyCarbs() != null) {
      validateCarbs(goals.getDailyCarbs());
    }
    
    if (goals.getDailyFats() != null) {
      validateFats(goals.getDailyFats());
    }
  }
  
  /**
   * Validates daily calories value.
   * 
   * @param calories the calories value to validate
   * @throws InvalidNutritionalDataException if calories is null, negative, or outside acceptable range
   */
  public static void validateCalories(BigDecimal calories) {
    if (calories == null) {
      throw new InvalidNutritionalDataException("Daily calories cannot be null");
    }
    
    if (calories.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidNutritionalDataException(
        String.format("Daily calories must be positive. Received: %s", calories)
      );
    }
    
    if (calories.compareTo(MIN_CALORIES) < 0) {
      throw new InvalidNutritionalDataException(
        String.format("Daily calories (%s) is too low. Minimum: %s kcal", calories, MIN_CALORIES)
      );
    }
    
    if (calories.compareTo(MAX_CALORIES) > 0) {
      throw new InvalidNutritionalDataException(
        String.format("Daily calories (%s) exceeds maximum allowed value of %s kcal", calories, MAX_CALORIES)
      );
    }
  }
  
  /**
   * Validates protein value.
   * 
   * @param protein the protein value in grams to validate (can be null, treated as optional)
   * @throws InvalidNutritionalDataException if protein is negative or outside acceptable range
   */
  public static void validateProtein(BigDecimal protein) {
    validateMacro(protein, MAX_PROTEIN, "Protein");
  }
  
  /**
   * Validates carbohydrates value.
   * 
   * @param carbs the carbohydrates value in grams to validate (can be null, treated as optional)
   * @throws InvalidNutritionalDataException if carbs is negative or outside acceptable range
   */
  public static void validateCarbs(BigDecimal carbs) {
    validateMacro(carbs, MAX_CARBS, "Carbohydrates");
  }
  
  /**
   * Validates fats value.
   * 
   * @param fats the fats value in grams to validate (can be null, treated as optional)
   * @throws InvalidNutritionalDataException if fats is negative or outside acceptable range
   */
  public static void validateFats(BigDecimal fats) {
    validateMacro(fats, MAX_FATS, "Fats");
  }
  
  /**
   * Common validation logic for macronutrients (protein, carbohydrates, fats).
   * 
   * <p>Validates that the value is:
   * <ul>
   *   <li>Not negative (must be >= 0)</li>
   *   <li>Not exceeding the maximum allowed value</li>
   * </ul>
   * 
   * <p>Null values are acceptable and will return immediately without validation.
   * 
   * @param value the macro value in grams to validate (can be null)
   * @param maxValue the maximum allowed value for this macro
   * @param macroName the name of the macro (e.g., "Protein", "Carbohydrates", "Fats") for error messages
   * @throws InvalidNutritionalDataException if value is negative or exceeds maximum
   */
  private static void validateMacro(BigDecimal value, BigDecimal maxValue, String macroName) {
    if (value == null) {
      return; // Null is acceptable for optional values
    }
    
    if (value.compareTo(MIN_MACROS) < 0) {
      throw new InvalidNutritionalDataException(
        String.format("%s cannot be negative. Received: %s g", macroName, value)
      );
    }
    
    if (value.compareTo(maxValue) > 0) {
      throw new InvalidNutritionalDataException(
        String.format("%s (%s g) exceeds maximum allowed value of %s g", macroName, value, maxValue)
      );
    }
  }
}
