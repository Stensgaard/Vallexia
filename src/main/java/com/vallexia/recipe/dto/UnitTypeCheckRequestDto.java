package com.vallexia.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for unit type check requests.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeCheckRequestDto {
    
    @NotBlank(message = "Unit is required")
    private String unit;
}
