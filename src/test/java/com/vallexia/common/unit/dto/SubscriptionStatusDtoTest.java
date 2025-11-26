package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.SubscriptionStatusDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SubscriptionStatusDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SubscriptionStatusDto Unit Tests")
class SubscriptionStatusDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build SubscriptionStatusDto with all fields")
  void shouldBuildSubscriptionStatusDtoWithAllFields() {
    // Given
    String code = "PREMIUM";
    String name = "Premium";

    // When
    SubscriptionStatusDto dto = SubscriptionStatusDto.builder()
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
    SubscriptionStatusDto dto1 = SubscriptionStatusDto.builder()
        .code("PREMIUM")
        .name("Premium")
        .build();
    SubscriptionStatusDto dto2 = SubscriptionStatusDto.builder()
        .code("PREMIUM")
        .name("Premium")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    SubscriptionStatusDto dto1 = SubscriptionStatusDto.builder()
        .code("FREE")
        .name("Free")
        .build();
    SubscriptionStatusDto dto2 = SubscriptionStatusDto.builder()
        .code("PREMIUM")
        .name("Premium")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
