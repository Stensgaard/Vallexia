package com.vallexia.user.dto;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.validator.ValidAllergy;
import com.vallexia.common.validator.ValidCuisineType;
import com.vallexia.common.validator.ValidDietaryRestriction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Data Transfer Object for dietary preferences.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaryPreferencesDto {
    
    private Long id;
    
    private Long userId;
    
    @ValidDietaryRestriction
    private Set<SupportedDietaryRestriction> restrictions;
    
    @ValidAllergy
    private Set<SupportedAllergy> allergies;

    @ValidCuisineType
    private Set<SupportedCuisineType> preferredCuisines;
}
