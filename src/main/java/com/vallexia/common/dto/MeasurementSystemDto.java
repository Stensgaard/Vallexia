package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for measurement system options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported measurement system option")
public class MeasurementSystemDto {

    @Schema(description = "Measurement system code", example = "METRIC")
    String code;

    @Schema(description = "Display name", example = "Metric")
    String name;
}
