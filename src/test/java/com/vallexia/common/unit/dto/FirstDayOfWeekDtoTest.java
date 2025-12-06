package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.FirstDayOfWeekDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FirstDayOfWeekDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("FirstDayOfWeekDto Unit Tests")
class FirstDayOfWeekDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build FirstDayOfWeekDto with all fields")
  void shouldBuildFirstDayOfWeekDtoWithAllFields() {
    // Given
    String code = "MONDAY";
    String name = "Monday";

    // When
    FirstDayOfWeekDto dto = FirstDayOfWeekDto.builder()
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
    FirstDayOfWeekDto dto1 = FirstDayOfWeekDto.builder()
        .code("MONDAY")
        .name("Monday")
        .build();
    FirstDayOfWeekDto dto2 = FirstDayOfWeekDto.builder()
        .code("MONDAY")
        .name("Monday")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    FirstDayOfWeekDto dto1 = FirstDayOfWeekDto.builder()
        .code("SUNDAY")
        .name("Sunday")
        .build();
    FirstDayOfWeekDto dto2 = FirstDayOfWeekDto.builder()
        .code("MONDAY")
        .name("Monday")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
