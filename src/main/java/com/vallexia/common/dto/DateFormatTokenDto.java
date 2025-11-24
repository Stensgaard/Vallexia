package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Token describing a segment of a date format")
public class DateFormatTokenDto {

    @Schema(description = "Token type (DAY|MONTH|YEAR|LITERAL)", example = "DAY")
    String type;

    @Schema(description = "Literal value when type is LITERAL", example = "/")
    String value;
}
