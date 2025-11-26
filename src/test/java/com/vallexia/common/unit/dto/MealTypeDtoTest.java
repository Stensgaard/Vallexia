package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.MealTypeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MealTypeDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("MealTypeDto Unit Tests")
class MealTypeDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build MealTypeDto with all fields")
  void shouldBuildMealTypeDtoWithAllFields() {
    // Given
    String code = "BREAKFAST";
    String name = "Breakfast";

    // When
    MealTypeDto dto = MealTypeDto.builder()
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
    MealTypeDto dto1 = MealTypeDto.builder()
        .code("BREAKFAST")
        .name("Breakfast")
        .build();
    MealTypeDto dto2 = MealTypeDto.builder()
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
    MealTypeDto dto1 = MealTypeDto.builder()
        .code("BREAKFAST")
        .name("Breakfast")
        .build();
    MealTypeDto dto2 = MealTypeDto.builder()
        .code("LUNCH")
        .name("Lunch")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
