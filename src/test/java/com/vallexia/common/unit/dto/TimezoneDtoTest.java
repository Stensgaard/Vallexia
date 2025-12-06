package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.TimezoneDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TimezoneDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("TimezoneDto Unit Tests")
class TimezoneDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build TimezoneDto with all fields")
  void shouldBuildTimezoneDtoWithAllFields() {
    // Given
    String value = "America/New_York";
    String label = "Eastern Time (US & Canada)";

    // When
    TimezoneDto dto = TimezoneDto.builder()
        .value(value)
        .label(label)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getValue()).isEqualTo(value);
    assertThat(dto.getLabel()).isEqualTo(label);
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given/When
    TimezoneDto dto1 = TimezoneDto.builder()
        .value("America/New_York")
        .label("Eastern Time (US & Canada)")
        .build();
    TimezoneDto dto2 = TimezoneDto.builder()
        .value("America/New_York")
        .label("Eastern Time (US & Canada)")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    TimezoneDto dto1 = TimezoneDto.builder()
        .value("America/New_York")
        .label("Eastern Time (US & Canada)")
        .build();
    TimezoneDto dto2 = TimezoneDto.builder()
        .value("Europe/Copenhagen")
        .label("Central European Time")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
