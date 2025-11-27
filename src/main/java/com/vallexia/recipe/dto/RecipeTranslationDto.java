package com.vallexia.recipe.dto;

import com.vallexia.common.validator.ValidLocale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for recipe translations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeTranslationDto {
    
    @NotBlank(message = "Locale is required")
    @ValidLocale
    private String locale;
    
    @NotBlank(message = "Recipe name is required")
    @Size(max = 255, message = "Recipe name must not exceed 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotBlank(message = "Instructions are required")
    private String instructions;
}
