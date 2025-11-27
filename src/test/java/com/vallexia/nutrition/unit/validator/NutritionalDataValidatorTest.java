package com.vallexia.nutrition.unit.validator;

import com.vallexia.nutrition.exception.InvalidNutritionalDataException;
import com.vallexia.nutrition.validator.NutritionalDataValidator;
import com.vallexia.user.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for NutritionalDataValidator.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("NutritionalDataValidator Unit Tests")
class NutritionalDataValidatorTest {
  
  // ==================== validateNutritionalGoals() Tests ====================
  
  @Test
  @DisplayName("Should validate valid nutritional goals successfully")
  void shouldValidateValidNutritionalGoals() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(BigDecimal.valueOf(150));
    goals.setDailyCarbs(BigDecimal.valueOf(200));
    goals.setDailyFats(BigDecimal.valueOf(67));
    
    // When & Then - should not throw exception
    assertThatCode(() -> NutritionalDataValidator.validateNutritionalGoals(goals))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should throw exception when goals is null")
  void shouldThrowExceptionWhenGoalsIsNull() {
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateNutritionalGoals(null))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessage("Nutritional goals cannot be null");
  }
  
  @Test
  @DisplayName("Should validate goals with null optional macros")
  void shouldValidateGoalsWithNullOptionalMacros() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setUser(new User());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(null); // Optional
    goals.setDailyCarbs(null);   // Optional
    goals.setDailyFats(null);    // Optional
    
    // When & Then - should not throw exception
    assertThatCode(() -> NutritionalDataValidator.validateNutritionalGoals(goals))
      .doesNotThrowAnyException();
  }
  
  // ==================== validateCalories() Tests ====================
  
  @Test
  @DisplayName("Should validate valid calories")
  void shouldValidateValidCalories() {
    // Given
    BigDecimal calories = BigDecimal.valueOf(2000);
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateCalories(calories))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate minimum valid calories")
  void shouldValidateMinimumCalories() {
    // Given
    BigDecimal calories = BigDecimal.valueOf(500); // Minimum valid
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateCalories(calories))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate maximum valid calories")
  void shouldValidateMaximumCalories() {
    // Given
    BigDecimal calories = BigDecimal.valueOf(10000); // Maximum valid
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateCalories(calories))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should throw exception when calories is null")
  void shouldThrowExceptionWhenCaloriesIsNull() {
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateCalories(null))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessage("Daily calories cannot be null");
  }
  
  @Test
  @DisplayName("Should throw exception when calories is zero")
  void shouldThrowExceptionWhenCaloriesIsZero() {
    // Given
    BigDecimal calories = BigDecimal.ZERO;
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateCalories(calories))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("must be positive");
  }
  
  @Test
  @DisplayName("Should throw exception when calories is negative")
  void shouldThrowExceptionWhenCaloriesIsNegative() {
    // Given
    BigDecimal calories = BigDecimal.valueOf(-100);
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateCalories(calories))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("must be positive");
  }
  
  @Test
  @DisplayName("Should throw exception when calories is below minimum")
  void shouldThrowExceptionWhenCaloriesIsBelowMinimum() {
    // Given
    BigDecimal calories = BigDecimal.valueOf(499); // Below 500 minimum
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateCalories(calories))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("too low")
      .hasMessageContaining("500");
  }
  
  @Test
  @DisplayName("Should throw exception when calories exceeds maximum")
  void shouldThrowExceptionWhenCaloriesExceedsMaximum() {
    // Given
    BigDecimal calories = BigDecimal.valueOf(10001); // Above 10000 maximum
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateCalories(calories))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("exceeds maximum")
      .hasMessageContaining("10000");
  }
  
  // ==================== validateProtein() Tests ====================
  
  @Test
  @DisplayName("Should validate valid protein")
  void shouldValidateValidProtein() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(150);
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateProtein(protein))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should accept null protein")
  void shouldAcceptNullProtein() {
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateProtein(null))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate zero protein")
  void shouldValidateZeroProtein() {
    // Given
    BigDecimal protein = BigDecimal.ZERO;
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateProtein(protein))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate maximum protein")
  void shouldValidateMaximumProtein() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(1000); // Maximum
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateProtein(protein))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should throw exception when protein is negative")
  void shouldThrowExceptionWhenProteinIsNegative() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateProtein(protein))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Protein cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw exception when protein exceeds maximum")
  void shouldThrowExceptionWhenProteinExceedsMaximum() {
    // Given
    BigDecimal protein = BigDecimal.valueOf(1001); // Above 1000 maximum
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateProtein(protein))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Protein")
      .hasMessageContaining("exceeds maximum")
      .hasMessageContaining("1000");
  }
  
  // ==================== validateCarbs() Tests ====================
  
  @Test
  @DisplayName("Should validate valid carbs")
  void shouldValidateValidCarbs() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(200);
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateCarbs(carbs))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should accept null carbs")
  void shouldAcceptNullCarbs() {
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateCarbs(null))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate zero carbs")
  void shouldValidateZeroCarbs() {
    // Given
    BigDecimal carbs = BigDecimal.ZERO;
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateCarbs(carbs))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate maximum carbs")
  void shouldValidateMaximumCarbs() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(1500); // Maximum
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateCarbs(carbs))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should throw exception when carbs is negative")
  void shouldThrowExceptionWhenCarbsIsNegative() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateCarbs(carbs))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Carbohydrates cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw exception when carbs exceeds maximum")
  void shouldThrowExceptionWhenCarbsExceedsMaximum() {
    // Given
    BigDecimal carbs = BigDecimal.valueOf(1501); // Above 1500 maximum
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateCarbs(carbs))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Carbohydrates")
      .hasMessageContaining("exceeds maximum")
      .hasMessageContaining("1500");
  }
  
  // ==================== validateFats() Tests ====================
  
  @Test
  @DisplayName("Should validate valid fats")
  void shouldValidateValidFats() {
    // Given
    BigDecimal fats = BigDecimal.valueOf(67);
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateFats(fats))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should accept null fats")
  void shouldAcceptNullFats() {
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateFats(null))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate zero fats")
  void shouldValidateZeroFats() {
    // Given
    BigDecimal fats = BigDecimal.ZERO;
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateFats(fats))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should validate maximum fats")
  void shouldValidateMaximumFats() {
    // Given
    BigDecimal fats = BigDecimal.valueOf(500); // Maximum
    
    // When & Then
    assertThatCode(() -> NutritionalDataValidator.validateFats(fats))
      .doesNotThrowAnyException();
  }
  
  @Test
  @DisplayName("Should throw exception when fats is negative")
  void shouldThrowExceptionWhenFatsIsNegative() {
    // Given
    BigDecimal fats = BigDecimal.valueOf(-10);
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateFats(fats))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Fats cannot be negative");
  }
  
  @Test
  @DisplayName("Should throw exception when fats exceeds maximum")
  void shouldThrowExceptionWhenFatsExceedsMaximum() {
    // Given
    BigDecimal fats = BigDecimal.valueOf(501); // Above 500 maximum
    
    // When & Then
    assertThatThrownBy(() -> NutritionalDataValidator.validateFats(fats))
      .isInstanceOf(InvalidNutritionalDataException.class)
      .hasMessageContaining("Fats")
      .hasMessageContaining("exceeds maximum")
      .hasMessageContaining("500");
  }
}
