package com.vallexia.common.unit.dto;

import com.vallexia.common.dto.FormattingRuleDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FormattingRuleDto.
 * Tests builder pattern and immutability.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("FormattingRuleDto Unit Tests")
class FormattingRuleDtoTest {

  // ==================== Builder Tests ====================

  @Test
  @DisplayName("Should build FormattingRuleDto with all fields")
  void shouldBuildFormattingRuleDtoWithAllFields() {
    // Given
    String countryCode = "US";
    String countryName = "United States";
    String decimalSeparator = ".";
    String thousandsSeparator = ",";
    String currencyCode = "USD";

    // When
    FormattingRuleDto dto = FormattingRuleDto.builder()
        .countryCode(countryCode)
        .countryName(countryName)
        .decimalSeparator(decimalSeparator)
        .thousandsSeparator(thousandsSeparator)
        .currencyCode(currencyCode)
        .build();

    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCountryCode()).isEqualTo(countryCode);
    assertThat(dto.getCountryName()).isEqualTo(countryName);
    assertThat(dto.getDecimalSeparator()).isEqualTo(decimalSeparator);
    assertThat(dto.getThousandsSeparator()).isEqualTo(thousandsSeparator);
    assertThat(dto.getCurrencyCode()).isEqualTo(currencyCode);
  }

  @Test
  @DisplayName("Should create equal instances with same values")
  void shouldCreateEqualInstancesWithSameValues() {
    // Given/When
    FormattingRuleDto dto1 = FormattingRuleDto.builder()
        .countryCode("US")
        .countryName("United States")
        .decimalSeparator(".")
        .thousandsSeparator(",")
        .currencyCode("USD")
        .build();
    FormattingRuleDto dto2 = FormattingRuleDto.builder()
        .countryCode("US")
        .countryName("United States")
        .decimalSeparator(".")
        .thousandsSeparator(",")
        .currencyCode("USD")
        .build();

    // Then
    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("Should create different instances with different values")
  void shouldCreateDifferentInstancesWithDifferentValues() {
    // Given/When
    FormattingRuleDto dto1 = FormattingRuleDto.builder()
        .countryCode("US")
        .countryName("United States")
        .decimalSeparator(".")
        .thousandsSeparator(",")
        .currencyCode("USD")
        .build();
    FormattingRuleDto dto2 = FormattingRuleDto.builder()
        .countryCode("DK")
        .countryName("Denmark")
        .decimalSeparator(",")
        .thousandsSeparator(".")
        .currencyCode("DKK")
        .build();

    // Then
    assertThat(dto1).isNotEqualTo(dto2);
  }
}
