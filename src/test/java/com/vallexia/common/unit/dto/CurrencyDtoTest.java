package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.CurrencyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CurrencyDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("CurrencyDto Unit Tests")
class CurrencyDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build CurrencyDto with all fields")
  void shouldBuildCurrencyDtoWithAllFields() {
    // Given
    String code = "USD";
    String name = "US Dollar";

    // When
    CurrencyDto dto = CurrencyDto.builder()
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
    CurrencyDto dto1 = CurrencyDto.builder()
        .code("USD")
        .name("US Dollar")
        .build();
    CurrencyDto dto2 = CurrencyDto.builder()
        .code("USD")
        .name("US Dollar")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    CurrencyDto dto1 = CurrencyDto.builder()
        .code("USD")
        .name("US Dollar")
        .build();
    CurrencyDto dto2 = CurrencyDto.builder()
        .code("DKK")
        .name("Danish Krone")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
