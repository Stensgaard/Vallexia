package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.DateFormatDto;
import com.vallexia.common.dto.DateFormatTokenDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DateFormatDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("DateFormatDto Unit Tests")
class DateFormatDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build DateFormatDto with all fields")
  void shouldBuildDateFormatDtoWithAllFields() {
    // Given
    String code = "MM_DD_YYYY";
    String format = "MM/DD/YYYY";
    List<DateFormatTokenDto> tokens = List.of(
        DateFormatTokenDto.builder().type("MONTH").value(null).build(),
        DateFormatTokenDto.builder().type("LITERAL").value("/").build(),
        DateFormatTokenDto.builder().type("DAY").value(null).build(),
        DateFormatTokenDto.builder().type("LITERAL").value("/").build(),
        DateFormatTokenDto.builder().type("YEAR").value(null).build()
    );

    // When
    DateFormatDto dto = DateFormatDto.builder()
        .code(code)
        .format(format)
        .tokens(tokens)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo(code);
    assertThat(dto.getFormat()).isEqualTo(format);
    assertThat(dto.getTokens()).isEqualTo(tokens);
    assertThat(dto.getTokens()).hasSize(5);
  }

  @Test
  @DisplayName("Should build DateFormatDto with empty tokens list")
  void shouldBuildDateFormatDtoWithEmptyTokensList() {
    // Given/When
    DateFormatDto dto = DateFormatDto.builder()
        .code("MM_DD_YYYY")
        .format("MM/DD/YYYY")
        .tokens(List.of())
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getTokens()).isEmpty();
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given
    List<DateFormatTokenDto> tokens = List.of(
        DateFormatTokenDto.builder().type("DAY").value(null).build()
    );

    // When
    DateFormatDto dto1 = DateFormatDto.builder()
        .code("MM_DD_YYYY")
        .format("MM/DD/YYYY")
        .tokens(tokens)
        .build();
    DateFormatDto dto2 = DateFormatDto.builder()
        .code("MM_DD_YYYY")
        .format("MM/DD/YYYY")
        .tokens(tokens)
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    DateFormatDto dto1 = DateFormatDto.builder()
        .code("MM_DD_YYYY")
        .format("MM/DD/YYYY")
        .tokens(List.of())
        .build();
    DateFormatDto dto2 = DateFormatDto.builder()
        .code("DD_MM_YYYY")
        .format("DD/MM/YYYY")
        .tokens(List.of())
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
