package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for difficulty level options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported recipe difficulty level")
public class DifficultyLevelDto {

    @Schema(
        description = "Enum code of the difficulty level", 
        example = "EASY", 
        allowableValues = {"EASY", "MEDIUM", "HARD", "EXPERT"})
    String code;

    @Schema(
        description = "Display label for the difficulty", 
        example = "Easy")
    String name;
}
