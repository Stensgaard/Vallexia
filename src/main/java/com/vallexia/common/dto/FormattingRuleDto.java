package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for formatting rules.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Number/currency formatting metadata derived from country settings")
public class FormattingRuleDto {

    @Schema(description = "Country code", example = "US")
    String countryCode;

    @Schema(description = "Country name", example = "United States")
    String countryName;

    @Schema(description = "Decimal separator", example = ".")
    String decimalSeparator;

    @Schema(description = "Thousands separator", example = ",")
    String thousandsSeparator;

    @Schema(description = "Currency code", example = "USD")
    String currencyCode;
}
