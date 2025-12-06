package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for locale options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-20
 */
@Value
@Builder
@Schema(description = "Supported application locale")
public class LocaleDto {

    @Schema(
        description = "Locale code such as 'en'", 
        example = "en", 
        allowableValues = {"en", "da"})
    String code;

    @Schema(
        description = "Locale name", 
        example = "English")
    String name;
}
