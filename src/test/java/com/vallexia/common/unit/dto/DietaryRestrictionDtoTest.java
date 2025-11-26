package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.DietaryRestrictionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DietaryRestrictionDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("DietaryRestrictionDto Unit Tests")
class DietaryRestrictionDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build DietaryRestrictionDto with all fields")
  void shouldBuildDietaryRestrictionDtoWithAllFields() {
    // Given
    String code = "VEGAN";
    String name = "Vegan";

    // When
    DietaryRestrictionDto dto = DietaryRestrictionDto.builder()
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
    DietaryRestrictionDto dto1 = DietaryRestrictionDto.builder()
        .code("VEGAN")
        .name("Vegan")
        .build();
    DietaryRestrictionDto dto2 = DietaryRestrictionDto.builder()
        .code("VEGAN")
        .name("Vegan")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    DietaryRestrictionDto dto1 = DietaryRestrictionDto.builder()
        .code("VEGAN")
        .name("Vegan")
        .build();
    DietaryRestrictionDto dto2 = DietaryRestrictionDto.builder()
        .code("VEGETARIAN")
        .name("Vegetarian")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
