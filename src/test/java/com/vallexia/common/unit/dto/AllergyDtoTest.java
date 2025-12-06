package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.AllergyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AllergyDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("AllergyDto Unit Tests")
class AllergyDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build AllergyDto with all fields")
  void shouldBuildAllergyDtoWithAllFields() {
    // Given
    String code = "PEANUTS";
    String name = "Peanuts";

    // When
    AllergyDto dto = AllergyDto.builder()
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
    AllergyDto dto1 = AllergyDto.builder()
        .code("PEANUTS")
        .name("Peanuts")
        .build();
    AllergyDto dto2 = AllergyDto.builder()
        .code("PEANUTS")
        .name("Peanuts")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    AllergyDto dto1 = AllergyDto.builder()
        .code("PEANUTS")
        .name("Peanuts")
        .build();
    AllergyDto dto2 = AllergyDto.builder()
        .code("TREE_NUTS")
        .name("Tree Nuts")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
