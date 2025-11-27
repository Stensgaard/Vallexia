package com.vallexia.nutrition.service;

import com.vallexia.nutrition.exception.InvalidNutritionalDataException;
import com.vallexia.nutrition.exception.NutritionalCalculationException;
import com.vallexia.nutrition.validator.NutritionalDataValidator;
import com.vallexia.user.entity.NutritionalGoals;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.vallexia.nutrition.util.NutritionalConstants.*;

/**
 * Service for performing macro nutrient calculations.
 * 
 * <p>This service provides methods for calculating:
 * <ul>
 *   <li>Macro nutrient percentages from daily goals</li>
 *   <li>Total calories from macronutrient breakdown</li>
 *   <li>Individual calorie contributions from each macronutrient</li>
 * </ul>
 * 
 * <p><b>Nutritional Constants Used:</b>
 * <ul>
 *   <li>Protein: 4 calories per gram</li>
 *   <li>Carbohydrates: 4 calories per gram</li>
 *   <li>Fats: 9 calories per gram</li>
 * </ul>
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Service
public class MacroCalculator {
  
  // Acceptable percentage sum range for validation warnings
  private static final BigDecimal MIN_PERCENTAGE_SUM = BigDecimal.valueOf(90);
  private static final BigDecimal MAX_PERCENTAGE_SUM = BigDecimal.valueOf(110);
  
  // Percentage multiplier for converting decimal to percentage
  private static final BigDecimal PERCENTAGE_MULTIPLIER = BigDecimal.valueOf(100);
  
  /**
   * Calculate macro percentages for nutritional goals.
   * 
   * <p>This method calculates what percentage of total daily calories comes from each
   * macronutrient (protein, carbohydrates, and fats) and updates the goals entity directly.
   * 
   * <p><b>Formula:</b> Percentage = (grams × calories_per_gram / total_calories) × 100
   * 
   * <p><b>Example:</b>
   * <pre>
   * Daily calories: 2000 kcal
   * Protein: 150g → 150g × 4 cal/g = 600 cal → (600/2000) × 100 = 30%
   * Carbs: 200g → 200g × 4 cal/g = 800 cal → (800/2000) × 100 = 40%
   * Fats: 67g → 67g × 9 cal/g = 603 cal → (603/2000) × 100 = 30.15%
   * </pre>
   * 
   * <p><b>Notes:</b>
   * <ul>
   *   <li>This method mutates the input entity by setting the percentage fields.</li>
   *   <li>All values are rounded to 2 decimal places using HALF_UP rounding.</li>
   *   <li>Macros (protein, carbs, fats) are optional and can be null. Only their percentage
   *       is calculated when provided.</li>
   *   <li>If all macros are null, a warning is logged as the calculation would be meaningless.</li>
   *   <li>A warning is logged if calculated percentages sum outside the 90-110% range, as this
   *       may indicate data inconsistency (but this is allowed for deficit/surplus planning).</li>
   * </ul>
   * 
   * @param goals NutritionalGoals entity to update with calculated percentages
   * @throws InvalidNutritionalDataException if goals is null or nutritional values are invalid or out of range
   * @throws NutritionalCalculationException if calculation fails due to arithmetic error
   */
  public void calculateMacroPercentages(NutritionalGoals goals) {
    
    try {
      // Validate input data
      NutritionalDataValidator.validateNutritionalGoals(goals);
      
      BigDecimal dailyCalories = goals.getDailyCalories();
      
      log.debug("Calculating macro percentages for daily calories: {}", dailyCalories);
      
      // Check if all macros are null and log warning
      if (goals.getDailyProtein() == null 
          && goals.getDailyCarbs() == null 
          && goals.getDailyFats() == null) {
        log.warn("All macros are null for nutritional goals calculation. " +
            "Percentage calculation will result in all percentages being null.");
      }
      
      // Calculate macro percentages
      calculateMacroPercentage(
        goals.getDailyProtein(),
        PROTEIN_CALORIES_PER_GRAM,
        dailyCalories,
        goals::setProteinPercentage,
        "Protein"
      );
      
      calculateMacroPercentage(
        goals.getDailyCarbs(),
        CARB_CALORIES_PER_GRAM,
        dailyCalories,
        goals::setCarbsPercentage,
        "Carbs"
      );
      
      calculateMacroPercentage(
        goals.getDailyFats(),
        FAT_CALORIES_PER_GRAM,
        dailyCalories,
        goals::setFatsPercentage,
        "Fats"
      );
      
      // Validate percentage sum and log warning if outside acceptable range
      BigDecimal percentageSum = calculatePercentageSum(goals);
      
      if (percentageSum.compareTo(BigDecimal.ZERO) > 0 
          && (percentageSum.compareTo(MIN_PERCENTAGE_SUM) < 0 
              || percentageSum.compareTo(MAX_PERCENTAGE_SUM) > 0)) {
        log.warn("Calculated macro percentages sum to {}%, which is outside the expected " +
            "range of 90-110%. This may indicate data inconsistency, but is allowed for " +
            "deficit/surplus planning scenarios.", percentageSum);
      }
      
      log.debug("Successfully calculated macro percentages");
    } catch (ArithmeticException e) {
      throw new NutritionalCalculationException(
        "Failed to calculate macro percentages due to arithmetic error", e
      );
    }
  }
  
  /**
   * Helper method to calculate macro percentage for a single macronutrient.
   * 
   * @param macroGrams the macro value in grams (can be null)
   * @param caloriesPerGram the calories per gram for this macro
   * @param dailyCalories the total daily calories
   * @param setter the setter method to update the percentage on the goals entity
   * @param macroName the name of the macro for logging purposes
   */
  private void calculateMacroPercentage(
      BigDecimal macroGrams,
      BigDecimal caloriesPerGram,
      BigDecimal dailyCalories,
      java.util.function.Consumer<BigDecimal> setter,
      String macroName) {
    if (macroGrams != null) {
      BigDecimal percentage = macroGrams
        .multiply(caloriesPerGram)
        .multiply(PERCENTAGE_MULTIPLIER)
        .divide(dailyCalories, DECIMAL_SCALE, ROUNDING_MODE);
      setter.accept(percentage);
      log.debug("{} percentage: {}%", macroName, percentage);
    }
  }
  
  /**
   * Helper method to calculate the sum of all macro percentages.
   * 
   * @param goals the nutritional goals containing percentage values
   * @return the sum of all non-null percentages
   */
  private BigDecimal calculatePercentageSum(NutritionalGoals goals) {
    BigDecimal sum = BigDecimal.ZERO;
    if (goals.getProteinPercentage() != null) {
      sum = sum.add(goals.getProteinPercentage());
    }
    if (goals.getCarbsPercentage() != null) {
      sum = sum.add(goals.getCarbsPercentage());
    }
    if (goals.getFatsPercentage() != null) {
      sum = sum.add(goals.getFatsPercentage());
    }
    return sum;
  }
    
  /**
   * Calculate total calories from macronutrient breakdown.
   * 
   * <p>This method calculates the total caloric value from protein, carbohydrates, and fats
   * using the Atwater system: Protein (4 cal/g) + Carbs (4 cal/g) + Fats (9 cal/g).
   * 
   * <p><b>Formula:</b> Total Calories = (protein × 4) + (carbs × 4) + (fats × 9)
   * 
   * <p><b>Example:</b>
   * <pre>
   * Protein: 150g × 4 cal/g = 600 cal
   * Carbs: 200g × 4 cal/g = 800 cal
   * Fats: 67g × 9 cal/g = 603 cal
   * Total: 600 + 800 + 603 = 2003 cal
   * </pre>
   * 
   * @param protein protein in grams (can be null, treated as 0)
   * @param carbs carbohydrates in grams (can be null, treated as 0)
   * @param fats fats in grams (can be null, treated as 0)
   * @return total calories calculated from macros
   * @throws InvalidNutritionalDataException if any value is negative or exceeds maximum
   */
  public BigDecimal calculateCaloriesFromMacros(BigDecimal protein, BigDecimal carbs, BigDecimal fats) {
    // Validate inputs
    NutritionalDataValidator.validateProtein(protein);
    NutritionalDataValidator.validateCarbs(carbs);
    NutritionalDataValidator.validateFats(fats);
    
    log.debug("Calculating calories from macros - Protein: {}g, Carbs: {}g, Fats: {}g", 
      protein, carbs, fats);
    
    BigDecimal proteinCalories = protein != null ? protein.multiply(PROTEIN_CALORIES_PER_GRAM) : BigDecimal.ZERO;
    BigDecimal carbCalories = carbs != null ? carbs.multiply(CARB_CALORIES_PER_GRAM) : BigDecimal.ZERO;
    BigDecimal fatCalories = fats != null ? fats.multiply(FAT_CALORIES_PER_GRAM) : BigDecimal.ZERO;
    
    BigDecimal totalCalories = proteinCalories.add(carbCalories).add(fatCalories);
    
    log.debug("Total calories calculated: {}", totalCalories);
    
    return totalCalories.setScale(DECIMAL_SCALE, ROUNDING_MODE);
  }
    
  /**
   * Calculate protein calories from grams.
   * 
   * <p><b>Formula:</b> Protein Calories = protein_grams × 4
   * 
   * @param proteinGrams protein in grams (can be null, treated as 0)
   * @return protein calories
   * @throws InvalidNutritionalDataException if protein is negative or exceeds maximum
   */
  public BigDecimal calculateProteinCalories(BigDecimal proteinGrams) {
    NutritionalDataValidator.validateProtein(proteinGrams);
    return proteinGrams != null ? proteinGrams.multiply(PROTEIN_CALORIES_PER_GRAM) : BigDecimal.ZERO;
  }
    
  /**
   * Calculate carbohydrate calories from grams.
   * 
   * <p><b>Formula:</b> Carbohydrate Calories = carb_grams × 4
   * 
   * @param carbGrams carbohydrates in grams (can be null, treated as 0)
   * @return carbohydrate calories
   * @throws InvalidNutritionalDataException if carbs is negative or exceeds maximum
   */
  public BigDecimal calculateCarbCalories(BigDecimal carbGrams) {
    NutritionalDataValidator.validateCarbs(carbGrams);
    return carbGrams != null ? carbGrams.multiply(CARB_CALORIES_PER_GRAM) : BigDecimal.ZERO;
  }
    
  /**
   * Calculate fat calories from grams.
   * 
   * <p><b>Formula:</b> Fat Calories = fat_grams × 9
   * 
   * @param fatGrams fats in grams (can be null, treated as 0)
   * @return fat calories
   * @throws InvalidNutritionalDataException if fats is negative or exceeds maximum
   */
  public BigDecimal calculateFatCalories(BigDecimal fatGrams) {
    NutritionalDataValidator.validateFats(fatGrams);
    return fatGrams != null ? fatGrams.multiply(FAT_CALORIES_PER_GRAM) : BigDecimal.ZERO;
  }
}
