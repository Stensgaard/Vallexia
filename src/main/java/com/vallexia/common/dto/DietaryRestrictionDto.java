package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for dietary restriction options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported dietary restriction option")
public class DietaryRestrictionDto {

    @Schema(description = "Enum code of the dietary restriction", example = "VEGAN")
    String code;

    @Schema(description = "Display label for the restriction", example = "Vegan")
    String name;
}
