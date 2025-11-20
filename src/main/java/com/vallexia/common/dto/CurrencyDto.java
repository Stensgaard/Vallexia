package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for currency options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported currency option")
public class CurrencyDto {

    @Schema(description = "ISO 4217 currency code", example = "USD")
    String code;

    @Schema(description = "Currency name", example = "US Dollar")
    String name;
}
