package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for currency options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-20
 */
@Value
@Builder
@Schema(description = "Supported currency option")
public class CurrencyDto {

    @Schema(
        description = "ISO 4217 currency code", 
        example = "USD", 
        allowableValues = {"USD", "DKK"})
    String code;

    @Schema(
        description = "Currency name", 
        example = "US Dollar")
    String name;
}
