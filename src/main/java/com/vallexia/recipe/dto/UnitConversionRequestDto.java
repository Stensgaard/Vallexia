package com.vallexia.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for unit conversion requests.
 * Conversion type is automatically detected from the units.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitConversionRequestDto {
    
    @NotNull(message = "Value is required")
    private BigDecimal value;
    
    @NotBlank(message = "From unit is required")
    private String fromUnit;
    
    @NotBlank(message = "To unit is required")
    private String toUnit;
}
