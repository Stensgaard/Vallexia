package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * DTO for date format options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported date format definition")
public class DateFormatDto {

    @Schema(description = "Format code identifier", example = "MM_DD_YYYY")
    String code;

    @Schema(description = "Display string for the format", example = "MM/DD/YYYY")
    String format;

    @Schema(description = "Rendering tokens describing the format order")
    List<DateFormatTokenDto> tokens;
}