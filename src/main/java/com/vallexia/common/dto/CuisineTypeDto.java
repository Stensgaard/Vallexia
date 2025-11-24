package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for cuisine type options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-20
 */
@Value
@Builder
@Schema(description = "Supported cuisine type option")
public class CuisineTypeDto {

    @Schema(description = "Enum code of the cuisine type", example = "ITALIAN")
    String code;

    @Schema(description = "Display label for the cuisine", example = "Italian")
    String name;
}
