package com.vallexia.recipe.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for nutritional information.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionalInfoDto {
    
    private Long id;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Calories must be greater than 0")
    @DecimalMax(value = "50000.0", message = "Calories must not exceed 50000")
    private BigDecimal calories;
    
    @DecimalMin(value = "0.0", message = "Protein must be 0 or greater")
    @DecimalMax(value = "5000.0", message = "Protein must not exceed 5000 grams")
    private BigDecimal protein; // in grams
    
    @DecimalMin(value = "0.0", message = "Carbs must be 0 or greater")
    @DecimalMax(value = "10000.0", message = "Carbs must not exceed 10000 grams")
    private BigDecimal carbs; // in grams
    
    @DecimalMin(value = "0.0", message = "Fats must be 0 or greater")
    @DecimalMax(value = "2000.0", message = "Fats must not exceed 2000 grams")
    private BigDecimal fats; // in grams
    
    @DecimalMin(value = "0.0", message = "Fiber must be 0 or greater")
    @DecimalMax(value = "500.0", message = "Fiber must not exceed 500 grams")
    private BigDecimal fiber; // in grams
    
    @DecimalMin(value = "0.0", message = "Sodium must be 0 or greater")
    @DecimalMax(value = "50000.0", message = "Sodium must not exceed 50000 mg")
    private BigDecimal sodium; // in milligrams
    
    @DecimalMin(value = "0.0", message = "Sugar must be 0 or greater")
    @DecimalMax(value = "5000.0", message = "Sugar must not exceed 5000 grams")
    private BigDecimal sugar; // in grams
    
    private Boolean perServing = false; // true if values are per serving, false if total
}
