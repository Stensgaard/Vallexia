package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.UnitDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UnitDto.
 * Tests builder pattern, immutability, and nullable conversion field.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("UnitDto Unit Tests")
class UnitDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build UnitDto with all fields including conversion")
  void shouldBuildUnitDtoWithAllFieldsIncludingConversion() {
    // Given
    String code = "GRAM";
    String display = "g";
    BigDecimal conversion = BigDecimal.ONE;

    // When
    UnitDto dto = UnitDto.builder()
        .code(code)
        .display(display)
        .conversion(conversion)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo(code);
    assertThat(dto.getDisplay()).isEqualTo(display);
    assertThat(dto.getConversion()).isEqualTo(conversion);
  }

  @Test
  @DisplayName("Should build UnitDto with null conversion for count units")
  void shouldBuildUnitDtoWithNullConversionForCountUnits() {
    // Given/When
    UnitDto dto = UnitDto.builder()
        .code("PIECE")
        .display("piece")
        .conversion(null)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("PIECE");
    assertThat(dto.getDisplay()).isEqualTo("piece");
    assertThat(dto.getConversion()).isNull();
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given/When
    UnitDto dto1 = UnitDto.builder()
        .code("GRAM")
        .display("g")
        .conversion(BigDecimal.ONE)
        .build();
    UnitDto dto2 = UnitDto.builder()
        .code("GRAM")
        .display("g")
        .conversion(BigDecimal.ONE)
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create equal instances with null conversion")
  void shouldCreateEqualInstancesWithNullConversion() {
    // Given/When
    UnitDto dto1 = UnitDto.builder()
        .code("PIECE")
        .display("piece")
        .conversion(null)
        .build();
    UnitDto dto2 = UnitDto.builder()
        .code("PIECE")
        .display("piece")
        .conversion(null)
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    UnitDto dto1 = UnitDto.builder()
        .code("GRAM")
        .display("g")
        .conversion(BigDecimal.ONE)
        .build();
    UnitDto dto2 = UnitDto.builder()
        .code("KILOGRAM")
        .display("kg")
        .conversion(new BigDecimal("1000"))
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }

  @Test
  @DisplayName("Should create different instances when one has conversion and other is null")
  void shouldCreateDifferentInstancesWhenOneHasConversionAndOtherIsNull() {
    // Given/When
    UnitDto dto1 = UnitDto.builder()
        .code("GRAM")
        .display("g")
        .conversion(BigDecimal.ONE)
        .build();
    UnitDto dto2 = UnitDto.builder()
        .code("GRAM")
        .display("g")
        .conversion(null)
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
