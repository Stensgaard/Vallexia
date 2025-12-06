package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.MealCategoryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MealCategoryDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("MealCategoryDto Unit Tests")
class MealCategoryDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build MealCategoryDto with all fields")
  void shouldBuildMealCategoryDtoWithAllFields() {
    // Given
    String code = "BREAKFAST";
    String name = "Breakfast";

    // When
    MealCategoryDto dto = MealCategoryDto.builder()
        .code(code)
        .name(name)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo(code);
    assertThat(dto.getName()).isEqualTo(name);
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given/When
    MealCategoryDto dto1 = MealCategoryDto.builder()
        .code("BREAKFAST")
        .name("Breakfast")
        .build();
    MealCategoryDto dto2 = MealCategoryDto.builder()
        .code("BREAKFAST")
        .name("Breakfast")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    MealCategoryDto dto1 = MealCategoryDto.builder()
        .code("BREAKFAST")
        .name("Breakfast")
        .build();
    MealCategoryDto dto2 = MealCategoryDto.builder()
        .code("LUNCH")
        .name("Lunch")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
