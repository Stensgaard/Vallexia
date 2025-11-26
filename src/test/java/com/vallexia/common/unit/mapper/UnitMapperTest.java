package com.vallexia.common.unit.mapper;

import com.vallexia.common.dto.UnitDto;
import com.vallexia.common.enums.SupportedCountUnit;
import com.vallexia.common.enums.SupportedVolumeUnit;
import com.vallexia.common.enums.SupportedWeightUnit;
import com.vallexia.common.mapper.UnitMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for UnitMapper.
 * Tests enum-to-DTO mapping with null safety validation for measurement unit enums.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("UnitMapper Unit Tests")
class UnitMapperTest {

  // ==================== toWeightUnitDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedWeightUnit to UnitDto")
  void shouldMapSupportedWeightUnitToUnitDto() {
    // Given
    SupportedWeightUnit unit = SupportedWeightUnit.GRAM;

    // When
    UnitDto dto = UnitMapper.toWeightUnitDto(unit);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("GRAM");
    assertThat(dto.getDisplay()).isNotNull();
    assertThat(dto.getConversion()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when weight unit is null")
  void shouldThrowIllegalArgumentExceptionWhenWeightUnitIsNull() {
    // When/Then
    assertThatThrownBy(() -> UnitMapper.toWeightUnitDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unit must not be null");
  }

  // ==================== toVolumeUnitDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedVolumeUnit to UnitDto")
  void shouldMapSupportedVolumeUnitToUnitDto() {
    // Given
    SupportedVolumeUnit unit = SupportedVolumeUnit.MILLILITER;

    // When
    UnitDto dto = UnitMapper.toVolumeUnitDto(unit);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("MILLILITER");
    assertThat(dto.getDisplay()).isNotNull();
    assertThat(dto.getConversion()).isNotNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when volume unit is null")
  void shouldThrowIllegalArgumentExceptionWhenVolumeUnitIsNull() {
    // When/Then
    assertThatThrownBy(() -> UnitMapper.toVolumeUnitDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unit must not be null");
  }

  // ==================== toCountUnitDto() Tests ====================

  @Test
  @DisplayName("Should map SupportedCountUnit to UnitDto with null conversion")
  void shouldMapSupportedCountUnitToUnitDtoWithNullConversion() {
    // Given
    SupportedCountUnit unit = SupportedCountUnit.PIECE;

    // When
    UnitDto dto = UnitMapper.toCountUnitDto(unit);

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCode()).isEqualTo("PIECE");
    assertThat(dto.getDisplay()).isNotNull();
    assertThat(dto.getConversion()).isNull();
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when count unit is null")
  void shouldThrowIllegalArgumentExceptionWhenCountUnitIsNull() {
    // When/Then
    assertThatThrownBy(() -> UnitMapper.toCountUnitDto(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unit must not be null");
  }
}
