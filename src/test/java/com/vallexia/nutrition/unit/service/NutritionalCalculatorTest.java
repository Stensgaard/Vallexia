package com.vallexia.nutrition.unit.service;

import com.vallexia.nutrition.exception.InvalidNutritionalDataException;
import com.vallexia.nutrition.service.MacroCalculator;
import com.vallexia.user.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.vallexia.nutrition.util.NutritionalConstants.DECIMAL_SCALE;
import static com.vallexia.nutrition.util.NutritionalConstants.ROUNDING_MODE;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MacroCalculator service.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MacroCalculator Unit Tests")
class MacroCalculatorTest {
  
  private MacroCalculator macroCalculator;
  
  @BeforeEach
  void setUp() {
    macroCalculator = new MacroCalculator();
  }
  
  // ==================== calculateMacroPercentages() Tests ====================
  
  @Test
  @DisplayName("Should calculate macro percentages correctly for valid goals")
  void shouldCalculateMacroPercentagesCorrectly() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(150)); // 600 cal, 30%
    goals.setDailyCarbs(BigDecimal.valueOf(200));   // 800 cal, 40%
    goals.setDailyFats(BigDecimal.valueOf(67));     // 603 cal, 30.15%
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    // Using compareTo for BigDecimal comparison to handle scale differences
    assertThat(goals.getProteinPercentage().compareTo(new BigDecimal("30.00"))).isEqualTo(0);
    assertThat(goals.getCarbsPercentage().compareTo(new BigDecimal("40.00"))).isEqualTo(0);
    assertThat(goals.getFatsPercentage().compareTo(new BigDecimal("30.15"))).isEqualTo(0);
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when goals is null")
  void shouldThrowExceptionWhenGoalsIsNull() {
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(null))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessage("Nutritional goals cannot be null");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when calories is null")
  void shouldThrowExceptionWhenCaloriesIsNull() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(null);
    goals.setDailyProtein(BigDecimal.valueOf(150));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Daily calories cannot be null");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when calories is zero")
  void shouldThrowExceptionWhenCaloriesIsZero() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.ZERO);
    goals.setDailyProtein(BigDecimal.valueOf(150));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Daily calories must be positive");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when calories is negative")
  void shouldThrowExceptionWhenCaloriesIsNegative() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(-100));
    goals.setDailyProtein(BigDecimal.valueOf(150));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Daily calories must be positive");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when calories is too low")
  void shouldThrowExceptionWhenCaloriesIsTooLow() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(400)); // Below minimum of 500
    goals.setDailyProtein(BigDecimal.valueOf(50));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("too low");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when calories is too high")
  void shouldThrowExceptionWhenCaloriesIsTooHigh() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(15000)); // Above maximum of 10000
    goals.setDailyProtein(BigDecimal.valueOf(150));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("exceeds maximum");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when protein is negative")
  void shouldThrowExceptionWhenProteinIsNegative() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(-10));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Protein cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when protein is too high")
  void shouldThrowExceptionWhenProteinIsTooHigh() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(1500)); // Above maximum of 1000
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Protein")
      .hasMessageContaining("exceeds maximum");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when carbs is negative")
  void shouldThrowExceptionWhenCarbsIsNegative() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyCarbs(BigDecimal.valueOf(-10));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Carbohydrates cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when carbs is too high")
  void shouldThrowExceptionWhenCarbsIsTooHigh() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyCarbs(BigDecimal.valueOf(2000)); // Above maximum of 1500
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Carbohydrates")
      .hasMessageContaining("exceeds maximum");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when fats is negative")
  void shouldThrowExceptionWhenFatsIsNegative() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyFats(BigDecimal.valueOf(-10));
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Fats cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw InvalidNutritionalDataException when fats is too high")
  void shouldThrowExceptionWhenFatsIsTooHigh() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyFats(BigDecimal.valueOf(600)); // Above maximum of 500
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateMacroPercentages(goals))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Fats")
      .hasMessageContaining("exceeds maximum");
  }
  
  @Test
  @DisplayName("Should handle null protein value gracefully")
  void shouldHandleNullProteinGracefully() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(null);
    goals.setDailyCarbs(BigDecimal.valueOf(200));
    goals.setDailyFats(BigDecimal.valueOf(67));
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNull();
    assertThat(goals.getCarbsPercentage()).isNotNull();
    assertThat(goals.getFatsPercentage()).isNotNull();
  }
  
  @Test
  @DisplayName("Should handle null carbs value gracefully")
  void shouldHandleNullCarbsGracefully() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(150));
    goals.setDailyCarbs(null);
    goals.setDailyFats(BigDecimal.valueOf(67));
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNotNull();
    assertThat(goals.getCarbsPercentage()).isNull();
    assertThat(goals.getFatsPercentage()).isNotNull();
  }
  
  @Test
  @DisplayName("Should handle null fats value gracefully")
  void shouldHandleNullFatsGracefully() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(150));
    goals.setDailyCarbs(BigDecimal.valueOf(200));
    goals.setDailyFats(null);
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNotNull();
    assertThat(goals.getCarbsPercentage()).isNotNull();
    assertThat(goals.getFatsPercentage()).isNull();
  }
  
  @Test
  @DisplayName("Should calculate percentages correctly for edge case values")
  void shouldCalculatePercentagesForEdgeCaseValues() {
    // Given - minimum valid values
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(500)); // Minimum
    goals.setDailyProtein(BigDecimal.valueOf(10));
    goals.setDailyCarbs(BigDecimal.valueOf(50));
    goals.setDailyFats(BigDecimal.valueOf(10));
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNotNull();
    assertThat(goals.getCarbsPercentage()).isNotNull();
    assertThat(goals.getFatsPercentage()).isNotNull();
    
    // Verify percentages are within reasonable range
    assertThat(goals.getProteinPercentage()).isBetween(BigDecimal.ZERO, BigDecimal.valueOf(100));
    assertThat(goals.getCarbsPercentage()).isBetween(BigDecimal.ZERO, BigDecimal.valueOf(100));
    assertThat(goals.getFatsPercentage()).isBetween(BigDecimal.ZERO, BigDecimal.valueOf(100));
  }
  
  @Test
  @DisplayName("Should handle all macros null scenario with warning")
  void shouldHandleAllMacrosNullScenario() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(null);
    goals.setDailyCarbs(null);
    goals.setDailyFats(null);
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNull();
    assertThat(goals.getCarbsPercentage()).isNull();
    assertThat(goals.getFatsPercentage()).isNull();
  }
  
  @Test
  @DisplayName("Should calculate percentages when macros exceed daily calories")
  void shouldCalculatePercentagesWhenMacrosExceedDailyCalories() {
    // Given - macros provide more calories than daily goal
    // Daily: 2000 cal
    // Protein: 200g = 800 cal, Carbs: 300g = 1200 cal, Fats: 100g = 900 cal
    // Total from macros: 2900 cal (> 2000 cal goal)
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(200)); // 800 cal = 40%
    goals.setDailyCarbs(BigDecimal.valueOf(300));    // 1200 cal = 60%
    goals.setDailyFats(BigDecimal.valueOf(100));      // 900 cal = 45%
    // Total: 40 + 60 + 45 = 145% (exceeds 100%)
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNotNull();
    assertThat(goals.getCarbsPercentage()).isNotNull();
    assertThat(goals.getFatsPercentage()).isNotNull();
    
    // Verify percentages exceed 100% when summed
    BigDecimal totalPercentage = goals.getProteinPercentage()
        .add(goals.getCarbsPercentage())
        .add(goals.getFatsPercentage());
    assertThat(totalPercentage).isGreaterThan(BigDecimal.valueOf(100));
  }
  
  @Test
  @DisplayName("Should calculate percentages that sum to exactly 100 percent")
  void shouldCalculatePercentagesThatSumToExactly100Percent() {
    // Given - macros exactly match daily calories
    // Daily: 2000 cal
    // Protein: 150g = 600 cal = 30%, Carbs: 200g = 800 cal = 40%, Fats: 67g ≈ 603 cal ≈ 30%
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(150)); // 600 cal = 30%
    goals.setDailyCarbs(BigDecimal.valueOf(200));   // 800 cal = 40%
    goals.setDailyFats(BigDecimal.valueOf(67));     // 603 cal ≈ 30.15%
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNotNull();
    assertThat(goals.getCarbsPercentage()).isNotNull();
    assertThat(goals.getFatsPercentage()).isNotNull();
    
    // Verify percentages sum to approximately 100%
    BigDecimal totalPercentage = goals.getProteinPercentage()
        .add(goals.getCarbsPercentage())
        .add(goals.getFatsPercentage());
    // Allow for rounding differences, should be between 99.5% and 100.5%
    assertThat(totalPercentage).isBetween(
        BigDecimal.valueOf(99.5), 
        BigDecimal.valueOf(100.5)
    );
  }
  
  @Test
  @DisplayName("Should calculate percentages that sum to less than 100 percent")
  void shouldCalculatePercentagesThatSumToLessThan100Percent() {
    // Given - macros provide fewer calories than daily goal
    // Daily: 2000 cal
    // Protein: 100g = 400 cal = 20%, Carbs: 150g = 600 cal = 30%, Fats: 30g = 270 cal = 13.5%
    // Total: 20 + 30 + 13.5 = 63.5% (< 100%)
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(100)); // 400 cal = 20%
    goals.setDailyCarbs(BigDecimal.valueOf(150));  // 600 cal = 30%
    goals.setDailyFats(BigDecimal.valueOf(30));     // 270 cal = 13.5%
    
    // When
    macroCalculator.calculateMacroPercentages(goals);
    
    // Then
    assertThat(goals.getProteinPercentage()).isNotNull();
    assertThat(goals.getCarbsPercentage()).isNotNull();
    assertThat(goals.getFatsPercentage()).isNotNull();
    
    // Verify percentages sum to less than 100%
    BigDecimal totalPercentage = goals.getProteinPercentage()
        .add(goals.getCarbsPercentage())
        .add(goals.getFatsPercentage());
    assertThat(totalPercentage).isLessThan(BigDecimal.valueOf(100));
  }
  
  // ==================== calculateCaloriesFromMacros() Tests ====================
  
  @Test
  @DisplayName("Should calculate total calories from macros correctly")
  void shouldCalculateTotalCaloriesFromMacros() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(150); // 600 cal
    BigDecimal carbs = BigDecimal.valueOf(200);   // 800 cal
    BigDecimal fats = BigDecimal.valueOf(67);     // 603 cal
    
    // When
    BigDecimal result = macroCalculator.calculateCaloriesFromMacros(protein, carbs, fats);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.valueOf(2003).setScale(DECIMAL_SCALE, ROUNDING_MODE));
  }
  
  @Test
  @DisplayName("Should handle null protein in calculateCaloriesFromMacros")
  void shouldHandleNullProteinInCalculateCalories() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(200);
    BigDecimal fats = BigDecimal.valueOf(67);
    
    // When
    BigDecimal result = macroCalculator.calculateCaloriesFromMacros(null, carbs, fats);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.valueOf(1403).setScale(DECIMAL_SCALE, ROUNDING_MODE)); // 800 + 603
  }
  
  @Test
  @DisplayName("Should handle null carbs in calculateCaloriesFromMacros")
  void shouldHandleNullCarbsInCalculateCalories() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(150);
    BigDecimal fats = BigDecimal.valueOf(67);
    
    // When
    BigDecimal result = macroCalculator.calculateCaloriesFromMacros(protein, null, fats);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.valueOf(1203).setScale(DECIMAL_SCALE, ROUNDING_MODE)); // 600 + 603
  }
  
  @Test
  @DisplayName("Should handle null fats in calculateCaloriesFromMacros")
  void shouldHandleNullFatsInCalculateCalories() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(150);
    BigDecimal carbs = BigDecimal.valueOf(200);
    
    // When
    BigDecimal result = macroCalculator.calculateCaloriesFromMacros(protein, carbs, null);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.valueOf(1400).setScale(DECIMAL_SCALE, ROUNDING_MODE)); // 600 + 800
  }
  
  @Test
  @DisplayName("Should return zero when all macros are null")
  void shouldReturnZeroWhenAllMacrosAreNull() {
    // When
    BigDecimal result = macroCalculator.calculateCaloriesFromMacros(null, null, null);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE));
  }
  
  @Test
  @DisplayName("Should throw exception when protein is negative in calculateCaloriesFromMacros")
  void shouldThrowExceptionWhenProteinIsNegativeInCalculateCalories() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateCaloriesFromMacros(protein, null, null))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Protein cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw exception when carbs is negative in calculateCaloriesFromMacros")
  void shouldThrowExceptionWhenCarbsIsNegativeInCalculateCalories() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateCaloriesFromMacros(null, carbs, null))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Carbohydrates cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw exception when fats is negative in calculateCaloriesFromMacros")
  void shouldThrowExceptionWhenFatsIsNegativeInCalculateCalories() {
    // Given
    BigDecimal fats = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateCaloriesFromMacros(null, null, fats))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Fats cannot be negative");
  }
  
  // ==================== calculateProteinCalories() Tests ====================
  
  @Test
  @DisplayName("Should calculate protein calories correctly")
  void shouldCalculateProteinCalories() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(150);
    
    // When
    BigDecimal result = macroCalculator.calculateProteinCalories(protein);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.valueOf(600)); // 150 * 4
  }
  
  @Test
  @DisplayName("Should return zero for null protein")
  void shouldReturnZeroForNullProtein() {
    // When
    BigDecimal result = macroCalculator.calculateProteinCalories(null);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.ZERO);
  }
  
  @Test
  @DisplayName("Should throw exception for negative protein in calculateProteinCalories")
  void shouldThrowExceptionForNegativeProteinCalories() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateProteinCalories(protein))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Protein cannot be negative");
  }
  
  // ==================== calculateCarbCalories() Tests ====================
  
  @Test
  @DisplayName("Should calculate carb calories correctly")
  void shouldCalculateCarbCalories() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(200);
    
    // When
    BigDecimal result = macroCalculator.calculateCarbCalories(carbs);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.valueOf(800)); // 200 * 4
  }
  
  @Test
  @DisplayName("Should return zero for null carbs")
  void shouldReturnZeroForNullCarbs() {
    // When
    BigDecimal result = macroCalculator.calculateCarbCalories(null);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.ZERO);
  }
  
  @Test
  @DisplayName("Should throw exception for negative carbs in calculateCarbCalories")
  void shouldThrowExceptionForNegativeCarbCalories() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateCarbCalories(carbs))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Carbohydrates cannot be negative");
  }
  
  // ==================== calculateFatCalories() Tests ====================
  
  @Test
  @DisplayName("Should calculate fat calories correctly")
  void shouldCalculateFatCalories() {
    // Given
    BigDecimal fats = BigDecimal.valueOf(67);
    
    // When
    BigDecimal result = macroCalculator.calculateFatCalories(fats);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.valueOf(603)); // 67 * 9
  }
  
  @Test
  @DisplayName("Should return zero for null fats")
  void shouldReturnZeroForNullFats() {
    // When
    BigDecimal result = macroCalculator.calculateFatCalories(null);
    
    // Then
    assertThat(result).isEqualTo(BigDecimal.ZERO);
  }
  
  @Test
  @DisplayName("Should throw exception for negative fats in calculateFatCalories")
  void shouldThrowExceptionForNegativeFatCalories() {
    // Given
    BigDecimal fats = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> macroCalculator.calculateFatCalories(fats))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Fats cannot be negative");
  }
}
