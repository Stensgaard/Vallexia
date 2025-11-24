package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for measurement system options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
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
