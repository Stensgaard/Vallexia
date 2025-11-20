package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for allergy options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported allergy option")
public class AllergyDto {

    @Schema(description = "Enum code of the allergy", example = "PEANUTS")
    String code;

    @Schema(description = "Display label for the allergy", example = "Peanuts")
    String name;
}
