package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.DifficultyLevelDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DifficultyLevelDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("DifficultyLevelDto Unit Tests")
class DifficultyLevelDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build DifficultyLevelDto with all fields")
  void shouldBuildDifficultyLevelDtoWithAllFields() {
    // Given
    String code = "EASY";
    String name = "Easy";

    // When
    DifficultyLevelDto dto = DifficultyLevelDto.builder()
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
    DifficultyLevelDto dto1 = DifficultyLevelDto.builder()
        .code("EASY")
        .name("Easy")
        .build();
    DifficultyLevelDto dto2 = DifficultyLevelDto.builder()
        .code("EASY")
        .name("Easy")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    DifficultyLevelDto dto1 = DifficultyLevelDto.builder()
        .code("EASY")
        .name("Easy")
        .build();
    DifficultyLevelDto dto2 = DifficultyLevelDto.builder()
        .code("HARD")
        .name("Hard")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
