package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for goal type options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported goal type option")
public class GoalTypeDto {

    @Schema(description = "Enum code of the goal type", example = "WEIGHT_LOSS")
    String code;

    @Schema(description = "Display label for the goal", example = "Weight Loss")
    String name;
}
