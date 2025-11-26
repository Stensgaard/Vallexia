package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for first day of week options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported first-day-of-week option")
public class FirstDayOfWeekDto {

    @Schema(
        description = "Code (SUNDAY or MONDAY)", 
        example = "MONDAY", 
        allowableValues = {"SUNDAY", "MONDAY"})
    String code;

    @Schema(
        description = "Display name", 
        example = "Monday")
    String name;
}
