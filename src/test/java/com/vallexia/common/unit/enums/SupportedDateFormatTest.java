package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedDateFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedDateFormat enum.
 * Tests date format integrity and lookup functionality.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedDateFormat Unit Tests")
class SupportedDateFormatTest {

  // ==================== Format Integrity Tests ====================

  @ParameterizedTest
  @EnumSource(SupportedDateFormat.class)
  @DisplayName("Should keep format in sync with tokens")
  void shouldKeepFormatInSyncWithTokens(SupportedDateFormat format) {
    // When
    String rebuilt = format.getTokens().stream()
        .map(token -> switch (token.getType()) {
          case DAY -> "DD";
          case MONTH -> "MM";
          case YEAR -> "YYYY";
          case LITERAL -> token.getValue();
        })
        .collect(Collectors.joining());

    // Then
    assertThat(rebuilt).isEqualTo(format.getFormat());
  }

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve codes case-insensitively")
  void shouldResolveCodesCaseInsensitive() {
    // When/Then
    assertThat(SupportedDateFormat.fromCode("mm_dd_yyyy"))
        .contains(SupportedDateFormat.MM_DD_YYYY);
  }
}
