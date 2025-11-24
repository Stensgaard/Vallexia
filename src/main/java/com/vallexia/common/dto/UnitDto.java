package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for unit options.
 *
 * <p>The conversion value expresses how many base units (grams or milliliters) the unit equals.
 * For pure count-based units the conversion can be {@code null}.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Generic unit definition with optional conversion metadata")
public class UnitDto {

    @Schema(description = "Unit code identifier", example = "GRAM")
    String code;

    @Schema(description = "Display label", example = "g")
    String display;

    @Schema(
            description = "Conversion factor relative to base unit (grams/milliliters); null for count units",
            example = "28.35",
            nullable = true)
    BigDecimal conversion;
}
