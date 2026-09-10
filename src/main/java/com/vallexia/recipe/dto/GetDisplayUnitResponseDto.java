package com.vallexia.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for getDisplayUnit responses.
 * 
 * @since 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDisplayUnitResponseDto {
    
    private String displayUnit;
}
