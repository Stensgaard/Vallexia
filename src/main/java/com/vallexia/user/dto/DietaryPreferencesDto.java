package com.vallexia.user.dto;

import com.vallexia.user.entity.Allergy;
import com.vallexia.user.entity.CuisineType;
import com.vallexia.user.entity.DietaryRestriction;
import com.vallexia.user.entity.ServingSizePreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    
    @Size(max = 50, message = "Maximum 50 disliked ingredients allowed")
    private Set<@NotBlank(message = "Disliked ingredient cannot be blank") @Size(max = 100, message = "Ingredient name must not exceed 100 characters") String> dislikedIngredients;
    
    @NotNull(message = "Serving size preference is required")
    private ServingSizePreference servingSizePreference;
}
