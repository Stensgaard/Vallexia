package com.vallexia.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for unit type check responses.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeCheckResponseDto {
    
    private boolean isWeightUnit;
    
    private boolean isVolumeUnit;
    
    private boolean isCountUnit;
}
