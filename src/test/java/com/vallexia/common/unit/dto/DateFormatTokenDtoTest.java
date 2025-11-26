package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.DateFormatTokenDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DateFormatTokenDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("DateFormatTokenDto Unit Tests")
class DateFormatTokenDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build DateFormatTokenDto with all fields")
  void shouldBuildDateFormatTokenDtoWithAllFields() {
    // Given
    String type = "DAY";
    String value = "/";

    // When
    DateFormatTokenDto dto = DateFormatTokenDto.builder()
        .type(type)
        .value(value)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getType()).isEqualTo(type);
    assertThat(dto.getValue()).isEqualTo(value);
  }

  @Test
  @DisplayName("Should build DateFormatTokenDto with null value for non-literal types")
  void shouldBuildDateFormatTokenDtoWithNullValueForNonLiteralTypes() {
    // Given/When
    DateFormatTokenDto dto = DateFormatTokenDto.builder()
        .type("DAY")
        .value(null)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getType()).isEqualTo("DAY");
    assertThat(dto.getValue()).isNull();
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given/When
    DateFormatTokenDto dto1 = DateFormatTokenDto.builder()
        .type("LITERAL")
        .value("/")
        .build();
    DateFormatTokenDto dto2 = DateFormatTokenDto.builder()
        .type("LITERAL")
        .value("/")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    DateFormatTokenDto dto1 = DateFormatTokenDto.builder()
        .type("DAY")
        .value(null)
        .build();
    DateFormatTokenDto dto2 = DateFormatTokenDto.builder()
        .type("MONTH")
        .value(null)
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
