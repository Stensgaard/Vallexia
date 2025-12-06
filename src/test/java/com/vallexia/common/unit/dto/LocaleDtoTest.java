package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.LocaleDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LocaleDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("LocaleDto Unit Tests")
class LocaleDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build LocaleDto with all fields")
  void shouldBuildLocaleDtoWithAllFields() {
    // Given
    String code = "en";
    String name = "English";

    // When
    LocaleDto dto = LocaleDto.builder()
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
    LocaleDto dto1 = LocaleDto.builder()
        .code("en")
        .name("English")
        .build();
    LocaleDto dto2 = LocaleDto.builder()
        .code("en")
        .name("English")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    LocaleDto dto1 = LocaleDto.builder()
        .code("en")
        .name("English")
        .build();
    LocaleDto dto2 = LocaleDto.builder()
        .code("da")
        .name("Danish")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
