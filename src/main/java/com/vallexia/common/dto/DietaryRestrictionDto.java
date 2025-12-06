package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for dietary restriction options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported dietary restriction option")
public class DietaryRestrictionDto {

    @Schema(
        description = "Enum code of the dietary restriction", 
        example = "VEGAN", 
        allowableValues = {"VEGETARIAN", "VEGAN", "GLUTEN_FREE", "DAIRY_FREE", 
            "NUT_FREE", "SOY_FREE", "EGG_FREE", "LOW_CARB", "KETO", "PALEO", 
            "MEDITERRANEAN", "LOW_SODIUM", "LOW_FAT", "HIGH_PROTEIN", "HALAL", "KOSHER"})
    String code;

    @Schema(
        description = "Display label for the restriction", 
        example = "Vegan")
    String name;
}
