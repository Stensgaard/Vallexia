package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.GoalTypeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GoalTypeDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("GoalTypeDto Unit Tests")
class GoalTypeDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build GoalTypeDto with all fields")
  void shouldBuildGoalTypeDtoWithAllFields() {
    // Given
    String code = "WEIGHT_LOSS";
    String name = "Weight Loss";

    // When
    GoalTypeDto dto = GoalTypeDto.builder()
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
    GoalTypeDto dto1 = GoalTypeDto.builder()
        .code("WEIGHT_LOSS")
        .name("Weight Loss")
        .build();
    GoalTypeDto dto2 = GoalTypeDto.builder()
        .code("WEIGHT_LOSS")
        .name("Weight Loss")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    GoalTypeDto dto1 = GoalTypeDto.builder()
        .code("WEIGHT_LOSS")
        .name("Weight Loss")
        .build();
    GoalTypeDto dto2 = GoalTypeDto.builder()
        .code("WEIGHT_GAIN")
        .name("Weight Gain")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
