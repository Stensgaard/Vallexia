package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for meal type options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported meal type option")
public class MealTypeDto {

    @Schema(description = "Enum code for the meal type", example = "BREAKFAST")
    String code;

    @Schema(description = "Display name", example = "Breakfast")
    String name;
}
