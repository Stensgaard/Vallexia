package com.vallexia.user.dto;

import com.vallexia.user.entity.enums.Allergy;
import com.vallexia.user.entity.enums.CuisineType;
import com.vallexia.user.entity.enums.DietaryRestriction;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Data Transfer Object for dietary preferences.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaryPreferencesDto {
    
    private Long id;
    
    private Long userId;
    
    @Size(max = 20, message = "Maximum 20 dietary restrictions allowed")
    private Set<DietaryRestriction> restrictions;
    
    @Size(max = 20, message = "Maximum 20 allergies allowed")
    private Set<Allergy> allergies;
    
    @Size(max = 20, message = "Maximum 20 preferred cuisines allowed")
    private Set<CuisineType> preferredCuisines;
}
