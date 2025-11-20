package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for country options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported country option")
public class CountryDto {

    @Schema(description = "ISO 3166-1 alpha-2 code", example = "US")
    String code;

    @Schema(description = "Display name", example = "United States")
    String name;
}
