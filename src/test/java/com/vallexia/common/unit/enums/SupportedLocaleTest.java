package com.vallexia.common.unit.enums;

import com.vallexia.common.enums.SupportedLocale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportedLocale enum.
 * Tests locale lookup and validation methods.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("SupportedLocale Unit Tests")
class SupportedLocaleTest {

  // ==================== getAll() Tests ====================

  @Test
  @DisplayName("Should return all locales")
  void shouldReturnAllLocales() {
    // When/Then
    assertThat(SupportedLocale.getAll())
        .hasSize(SupportedLocale.values().length)
        .containsExactlyInAnyOrder(SupportedLocale.values());
  }

  // ==================== fromCode() Tests ====================

  @Test
  @DisplayName("Should resolve locales case-insensitively")
  void shouldResolveLocalesCaseInsensitively() {
    // When/Then
    assertThat(SupportedLocale.fromCode("en"))
        .contains(SupportedLocale.EN);
    assertThat(SupportedLocale.fromCode("EN"))
        .contains(SupportedLocale.EN);
    assertThat(SupportedLocale.fromCode("En"))
        .contains(SupportedLocale.EN);
    assertThat(SupportedLocale.fromCode("da"))
        .contains(SupportedLocale.DA);
    assertThat(SupportedLocale.fromCode("DA"))
        .contains(SupportedLocale.DA);
  }

  @Test
  @DisplayName("Should resolve locales with whitespace trimming")
  void shouldResolveLocalesWithWhitespaceTrimming() {
    // When/Then
    assertThat(SupportedLocale.fromCode(" en "))
        .contains(SupportedLocale.EN);
    assertThat(SupportedLocale.fromCode("  da  "))
        .contains(SupportedLocale.DA);
  }

  @Test
  @DisplayName("Should return empty for unknown or blank codes")
  void shouldReturnEmptyForInvalidCodes() {
    // When/Then
    assertThat(SupportedLocale.fromCode("zzz")).isEmpty();
    assertThat(SupportedLocale.fromCode("fr")).isEmpty();
    assertThat(SupportedLocale.fromCode("")).isEmpty();
    assertThat(SupportedLocale.fromCode("   ")).isEmpty();
    assertThat(SupportedLocale.fromCode(null)).isEmpty();
  }

  // ==================== Code Value Tests ====================

  @Test
  @DisplayName("Should have non-blank code values for all locales")
  void shouldHaveNonBlankCodeValuesForAllLocales() {
    // Given/When/Then
    for (SupportedLocale locale : SupportedLocale.values()) {
      assertThat(locale.getCode())
          .as(locale.name() + " code value")
          .isNotBlank();
    }
  }
}
