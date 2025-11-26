package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for country options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-20
 */
@Value
@Builder
@Schema(description = "Supported country option")
public class CountryDto {

    @Schema(
        description = "ISO 3166-1 alpha-2 code", 
        example = "US", 
        allowableValues = {"US", "DK"})
    String code;

    @Schema(
        description = "Display name", 
        example = "United States")
    String name;

    @Schema(
        description = "Default language code", 
        example = "en")
    String languageCode;

    @Schema(
        description = "Default date format pattern", 
        example = "MM/dd/yyyy")
    String defaultDateFormat;

    @Schema(
        description = "Default timezone ID", 
        example = "America/New_York")
    String defaultTimezone;

    @Schema(
        description = "Default first day of week", 
        example = "SUNDAY")
    String firstDayOfWeek;

    @Schema(
        description = "Default measurement system", 
        example = "IMPERIAL")
    String measurementSystem;
}
