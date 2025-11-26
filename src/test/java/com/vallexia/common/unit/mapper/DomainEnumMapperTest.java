package com.vallexia.common.unit.mapper;

import com.vallexia.common.dto.*;
import com.vallexia.common.mapper.DomainEnumMapper;
import com.vallexia.recipe.entity.enums.DifficultyLevel;
import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
import com.vallexia.user.entity.enums.GoalType;
import com.vallexia.user.entity.enums.MealType;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DomainEnumMapper.
 * Tests enum-to-DTO mapping with null safety validation for domain-specific enums.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("DomainEnumMapper Unit Tests")
class DomainEnumMapperTest {

  // ==================== toDietaryRestrictionDto() Tests ====================

  @Test
  @DisplayName("Should map DietaryRestriction to DietaryRestrictionDto")
  void shouldMapDietaryRestrictionToDietaryRestrictionDto() {
    // Given
    DietaryRestriction restriction = DietaryRestriction.VEGETARIAN;

    // When
    DietaryRestrictionDto dto = DomainEnumMapper.toDietaryRestrictionDto(restriction);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("VEGETARIAN");
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when dietary restriction is null")
  void shouldThrowIllegalArgumentExceptionWhenDietaryRestrictionIsNull() {
    // When/Then
    assertThatThrownBy(() -> DomainEnumMapper.toDietaryRestrictionDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("restriction must not be null");
  }

  // ==================== toAllergyDto() Tests ====================

  @Test
  @DisplayName("Should map Allergy to AllergyDto")
  void shouldMapAllergyToAllergyDto() {
    // Given
    Allergy allergy = Allergy.PEANUTS;

    // When
    AllergyDto dto = DomainEnumMapper.toAllergyDto(allergy);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("PEANUTS");
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when allergy is null")
  void shouldThrowIllegalArgumentExceptionWhenAllergyIsNull() {
    // When/Then
    assertThatThrownBy(() -> DomainEnumMapper.toAllergyDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("allergy must not be null");
  }

  // ==================== toCuisineTypeDto() Tests ====================

  @Test
  @DisplayName("Should map CuisineType to CuisineTypeDto")
  void shouldMapCuisineTypeToCuisineTypeDto() {
    // Given
    CuisineType cuisineType = CuisineType.ITALIAN;

    // When
    CuisineTypeDto dto = DomainEnumMapper.toCuisineTypeDto(cuisineType);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("ITALIAN");
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when cuisine type is null")
  void shouldThrowIllegalArgumentExceptionWhenCuisineTypeIsNull() {
    // When/Then
    assertThatThrownBy(() -> DomainEnumMapper.toCuisineTypeDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cuisineType must not be null");
  }

  // ==================== toDifficultyLevelDto() Tests ====================

  @Test
  @DisplayName("Should map DifficultyLevel to DifficultyLevelDto")
  void shouldMapDifficultyLevelToDifficultyLevelDto() {
    // Given
    DifficultyLevel difficultyLevel = DifficultyLevel.EASY;

    // When
    DifficultyLevelDto dto = DomainEnumMapper.toDifficultyLevelDto(difficultyLevel);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("EASY");
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when difficulty level is null")
  void shouldThrowIllegalArgumentExceptionWhenDifficultyLevelIsNull() {
    // When/Then
    assertThatThrownBy(() -> DomainEnumMapper.toDifficultyLevelDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("difficultyLevel must not be null");
  }

  // ==================== toGoalTypeDto() Tests ====================

  @Test
  @DisplayName("Should map GoalType to GoalTypeDto")
  void shouldMapGoalTypeToGoalTypeDto() {
    // Given
    GoalType goalType = GoalType.WEIGHT_LOSS;

    // When
    GoalTypeDto dto = DomainEnumMapper.toGoalTypeDto(goalType);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("WEIGHT_LOSS");
    assertThat(dto.getName()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when goal type is null")
  void shouldThrowIllegalArgumentExceptionWhenGoalTypeIsNull() {
    // When/Then
    assertThatThrownBy(() -> DomainEnumMapper.toGoalTypeDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("goalType must not be null");
  }

  // ==================== toSubscriptionStatusDto() Tests ====================

  @Test
  @DisplayName("Should map SubscriptionStatus to SubscriptionStatusDto with same name for code and name")
  void shouldMapSubscriptionStatusToSubscriptionStatusDto() {
    // Given
    SubscriptionStatus status = SubscriptionStatus.PREMIUM;

    // When
    SubscriptionStatusDto dto = DomainEnumMapper.toSubscriptionStatusDto(status);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("PREMIUM");
    assertThat(dto.getName()).isEqualTo("PREMIUM");
    // Verify that both code and name use the enum name (as SubscriptionStatus doesn't have getDisplayName())
    assertThat(dto.getCode()).isEqualTo(dto.getName());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when subscription status is null")
  void shouldThrowIllegalArgumentExceptionWhenSubscriptionStatusIsNull() {
    // When/Then
    assertThatThrownBy(() -> DomainEnumMapper.toSubscriptionStatusDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("status must not be null");
  }

  // ==================== toMealTypeDto() Tests ====================

  @Test
  @DisplayName("Should map MealType to MealTypeDto")
  void shouldMapMealTypeToMealTypeDto() {
    // Given
    MealType mealType = MealType.BREAKFAST;

    // When
    MealTypeDto dto = DomainEnumMapper.toMealTypeDto(mealType);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("BREAKFAST");
    assertThat(dto.getName()).isEqualTo("Breakfast");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when meal type is null")
  void shouldThrowIllegalArgumentExceptionWhenMealTypeIsNull() {
    // When/Then
    assertThatThrownBy(() -> DomainEnumMapper.toMealTypeDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("mealType must not be null");
  }
}
