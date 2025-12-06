package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedCurrency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedCurrency enum.
 * Tests currency lookup and validation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedCurrency Unit Tests")
class SupportedCurrencyTest {

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve currencies case-insensitively")
  void shouldResolveCurrenciesCaseInsensitively() {
    // When/Then
    assertThat(SupportedCurrency.fromCode("usd"))
        .contains(SupportedCurrency.USD);
    assertThat(SupportedCurrency.fromCode(" dkk "))
        .contains(SupportedCurrency.DKK);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank codes")
  void shouldReturnEmptyForInvalidCodes() {
    // When/Then
    assertThat(SupportedCurrency.fromCode("zzz")).isEmpty();
    assertThat(SupportedCurrency.fromCode("")).isEmpty();
    assertThat(SupportedCurrency.fromCode(null)).isEmpty();
  }
}
