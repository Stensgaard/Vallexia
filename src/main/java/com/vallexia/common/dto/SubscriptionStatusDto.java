package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for subscription status options.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@Schema(description = "Supported subscription status")
public class SubscriptionStatusDto {

    @Schema(description = "Enum code of the subscription status", example = "PREMIUM")
    String code;

    @Schema(description = "Display label for the status", example = "Premium")
    String name;
}
