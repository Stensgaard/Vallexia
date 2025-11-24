package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for timezone options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported timezone option")
public class TimezoneDto {

    @Schema(description = "IANA timezone identifier", example = "America/New_York")
    String value;

    @Schema(description = "Display label", example = "Eastern Time (US & Canada)")
    String label;
}
