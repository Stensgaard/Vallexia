package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for allergy options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-20
 */
@Value
@Builder
@Schema(description = "Supported allergy option")
public class AllergyDto {

    @Schema(
        description = "Enum code of the allergy", 
        example = "PEANUTS", 
        allowableValues = {"PEANUTS", "TREE_NUTS", "MILK", "EGGS", "FISH", 
            "SHELLFISH", "SOY", "WHEAT", "SESAME", "MUSTARD", "CELERY", "LUPIN", "SULFITES"})
    String code;

    @Schema(
        description = "Display label for the allergy", 
        example = "Peanuts"
    )
    String name;
}
