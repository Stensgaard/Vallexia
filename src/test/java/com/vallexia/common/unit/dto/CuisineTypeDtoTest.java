package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.CuisineTypeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CuisineTypeDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("CuisineTypeDto Unit Tests")
class CuisineTypeDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build CuisineTypeDto with all fields")
  void shouldBuildCuisineTypeDtoWithAllFields() {
    // Given
    String code = "ITALIAN";
    String name = "Italian";

    // When
    CuisineTypeDto dto = CuisineTypeDto.builder()
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
    CuisineTypeDto dto1 = CuisineTypeDto.builder()
        .code("ITALIAN")
        .name("Italian")
        .build();
    CuisineTypeDto dto2 = CuisineTypeDto.builder()
        .code("ITALIAN")
        .name("Italian")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    CuisineTypeDto dto1 = CuisineTypeDto.builder()
        .code("ITALIAN")
        .name("Italian")
        .build();
    CuisineTypeDto dto2 = CuisineTypeDto.builder()
        .code("MEXICAN")
        .name("Mexican")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
