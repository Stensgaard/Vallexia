package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for first day of week options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported first-day-of-week option")
public class FirstDayOfWeekDto {

    @Schema(description = "Code (SUNDAY or MONDAY)", example = "MONDAY")
    String code;

    @Schema(description = "Display name", example = "Monday")
    String name;

    @Schema(description = "Numeric value used by backend (0 or 1)", example = "1")
    int value;
}
