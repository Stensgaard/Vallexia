package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for meal category options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported meal category for recipes and meal plans")
public class MealCategoryDto {

    @Schema(description = "Category code", example = "BREAKFAST")
    String code;

    @Schema(description = "Display name", example = "Breakfast")
    String name;
}
