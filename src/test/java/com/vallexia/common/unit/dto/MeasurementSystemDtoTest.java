package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.MeasurementSystemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MeasurementSystemDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("MeasurementSystemDto Unit Tests")
class MeasurementSystemDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build MeasurementSystemDto with all fields")
  void shouldBuildMeasurementSystemDtoWithAllFields() {
    // Given
    String code = "METRIC";
    String name = "Metric";

    // When
    MeasurementSystemDto dto = MeasurementSystemDto.builder()
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
    MeasurementSystemDto dto1 = MeasurementSystemDto.builder()
        .code("METRIC")
        .name("Metric")
        .build();
    MeasurementSystemDto dto2 = MeasurementSystemDto.builder()
        .code("METRIC")
        .name("Metric")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    MeasurementSystemDto dto1 = MeasurementSystemDto.builder()
        .code("METRIC")
        .name("Metric")
        .build();
    MeasurementSystemDto dto2 = MeasurementSystemDto.builder()
        .code("IMPERIAL")
        .name("Imperial")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
