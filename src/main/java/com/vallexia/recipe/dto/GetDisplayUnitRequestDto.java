package com.vallexia.recipe.dto;

import com.vallexia.common.validator.ValidMeasurementSystem;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for getDisplayUnit requests.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetDisplayUnitRequestDto {
    
    @NotBlank(message = "Unit is required")
    private String unit;
    
    @NotBlank(message = "Measurement system is required")
    @ValidMeasurementSystem
    private String measurementSystem;
}
