package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for difficulty level options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported recipe difficulty level")
public class DifficultyLevelDto {

    @Schema(description = "Enum code of the difficulty level", example = "EASY")
    String code;

    @Schema(description = "Display label for the difficulty", example = "Easy")
    String name;
}
