package com.vallexia.nutrition.unit.mapper;

import com.vallexia.nutrition.dto.NutritionalGoalsDto;
import com.vallexia.nutrition.entity.NutritionalGoals;
import com.vallexia.nutrition.enums.GoalType;
import com.vallexia.nutrition.mapper.NutritionalGoalsMapper;
import com.vallexia.user.fixtures.UserTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NutritionalGoalsMapper.
 * Tests entity-to-DTO mapping with real MapStruct implementation.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@SpringBootTest(classes = {
    com.vallexia.nutrition.mapper.NutritionalGoalsMapperImpl.class
})
@ActiveProfiles("test")
@DisplayName("NutritionalGoalsMapper Unit Tests")
class NutritionalGoalsMapperTest {
  
  @Autowired
  private NutritionalGoalsMapper nutritionalGoalsMapper;
  
  // ==================== toNutritionalGoalsDto() Tests ====================
  
  @Test
  @DisplayName("Should map all fields from entity to DTO")
  void shouldMapAllFieldsFromEntityToDto() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(goals.getId());
    assertThat(dto.getUserId()).isEqualTo(goals.getUser().getId());
    assertThat(dto.getDailyCalories()).isEqualTo(goals.getDailyCalories());
    assertThat(dto.getDailyProtein()).isEqualTo(goals.getDailyProtein());
    assertThat(dto.getDailyCarbs()).isEqualTo(goals.getDailyCarbs());
    assertThat(dto.getDailyFats()).isEqualTo(goals.getDailyFats());
  }
  
  @Test
  @DisplayName("Should map all nutritional values (calories, protein, carbs, fats, fiber, sodium, sugar)")
  void shouldMapAllNutritionalValues() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    goals.setDailyCalories(BigDecimal.valueOf(2500));
    goals.setDailyProtein(BigDecimal.valueOf(200));
    goals.setDailyCarbs(BigDecimal.valueOf(300));
    goals.setDailyFats(BigDecimal.valueOf(83));
    goals.setDailyFiber(BigDecimal.valueOf(30));
    goals.setDailySodium(BigDecimal.valueOf(2000));
    goals.setDailySugar(BigDecimal.valueOf(40));
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto.getDailyCalories()).isEqualByComparingTo(BigDecimal.valueOf(2500));
    assertThat(dto.getDailyProtein()).isEqualByComparingTo(BigDecimal.valueOf(200));
    assertThat(dto.getDailyCarbs()).isEqualByComparingTo(BigDecimal.valueOf(300));
    assertThat(dto.getDailyFats()).isEqualByComparingTo(BigDecimal.valueOf(83));
    assertThat(dto.getDailyFiber()).isEqualByComparingTo(BigDecimal.valueOf(30));
    assertThat(dto.getDailySodium()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    assertThat(dto.getDailySugar()).isEqualByComparingTo(BigDecimal.valueOf(40));
  }
  
  @Test
  @DisplayName("Should map goalType enum to string correctly")
  void shouldMapGoalTypeEnumToStringCorrectly() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    goals.setGoalType(GoalType.MUSCLE_GAIN);
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto.getGoalType()).isEqualTo("MUSCLE_GAIN");
  }
  
  @Test
  @DisplayName("Should map null goalType to null")
  void shouldMapNullGoalTypeToNull() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    goals.setGoalType(null);
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto.getGoalType()).isNull();
  }
  
  @Test
  @DisplayName("Should map macro percentages correctly")
  void shouldMapMacroPercentagesCorrectly() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    goals.setProteinPercentage(BigDecimal.valueOf(30.5));
    goals.setCarbsPercentage(BigDecimal.valueOf(50.25));
    goals.setFatsPercentage(BigDecimal.valueOf(19.75));
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto.getProteinPercentage()).isEqualByComparingTo(BigDecimal.valueOf(30.5));
    assertThat(dto.getCarbsPercentage()).isEqualByComparingTo(BigDecimal.valueOf(50.25));
    assertThat(dto.getFatsPercentage()).isEqualByComparingTo(BigDecimal.valueOf(19.75));
  }
  
  @Test
  @DisplayName("Should return null when entity is null")
  void shouldReturnNullWhenEntityIsNull() {
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(null);
    
    // Then
    assertThat(dto).isNull();
  }
  
  @Test
  @DisplayName("Should map partial entity with only required fields")
  void shouldMapPartialEntityWithOnlyRequiredFields() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setId(1L);
    goals.setUser(UserTestFixtures.createUser());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    // Leave other fields null
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getUserId()).isEqualTo(goals.getUser().getId());
    assertThat(dto.getDailyCalories()).isEqualByComparingTo(BigDecimal.valueOf(2000));
  }
  
  @Test
  @DisplayName("Should handle null values gracefully")
  void shouldHandleNullValuesGracefully() {
    // Given
    NutritionalGoals goals = new NutritionalGoals();
    goals.setId(1L);
    goals.setUser(UserTestFixtures.createUser());
    goals.setDailyCalories(BigDecimal.valueOf(2000));
    goals.setDailyProtein(null);
    goals.setDailyCarbs(null);
    goals.setDailyFats(null);
    goals.setGoalType(null);
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getDailyProtein()).isNull();
    assertThat(dto.getDailyCarbs()).isNull();
    assertThat(dto.getDailyFats()).isNull();
    assertThat(dto.getGoalType()).isNull();
  }
  
  @Test
  @DisplayName("Should map BigDecimal values correctly")
  void shouldMapBigDecimalValuesCorrectly() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    goals.setDailyCalories(new BigDecimal("2000.50"));
    goals.setDailyProtein(new BigDecimal("150.75"));
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto.getDailyCalories()).isEqualByComparingTo(new BigDecimal("2000.50"));
    assertThat(dto.getDailyProtein()).isEqualByComparingTo(new BigDecimal("150.75"));
  }
  
  @Test
  @DisplayName("Should handle default values")
  void shouldHandleDefaultValues() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    // Uses default values from fixture
    
    // When
    NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    
    // Then
    assertThat(dto.getDailyCalories()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    assertThat(dto.getDailyProtein()).isEqualByComparingTo(BigDecimal.valueOf(150));
    assertThat(dto.getDailyCarbs()).isEqualByComparingTo(BigDecimal.valueOf(250));
    assertThat(dto.getDailyFats()).isEqualByComparingTo(BigDecimal.valueOf(67));
  }
  
  @Test
  @DisplayName("Should map all goalType enum values correctly")
  void shouldMapAllGoalTypeEnumValuesCorrectly() {
    // Given
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals();
    
    for (GoalType goalType : GoalType.values()) {
      goals.setGoalType(goalType);
      
      // When
      NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
      
      // Then
      assertThat(dto.getGoalType()).isEqualTo(goalType.name());
    }
  }
}
