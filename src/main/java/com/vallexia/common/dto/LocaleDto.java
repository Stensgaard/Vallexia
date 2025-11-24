package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for locale options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported application locale")
public class LocaleDto {

    @Schema(description = "Locale code such as 'en'", example = "en")
    String code;

    @Schema(description = "Locale name", example = "English")
    String name;
}
