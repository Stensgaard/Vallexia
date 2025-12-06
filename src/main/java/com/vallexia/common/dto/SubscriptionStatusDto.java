package com.vallexia.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for subscription status options.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Value
@Builder
@Schema(description = "Supported subscription status")
public class SubscriptionStatusDto {

    @Schema(
        description = "Enum code of the subscription status", 
        example = "PREMIUM", 
        allowableValues = {"FREE", "PREMIUM", "FAMILY", "CANCELLED", "EXPIRED"})
    String code;

    @Schema(
        description = "Display label for the status", 
        example = "Premium")
    String name;
}
