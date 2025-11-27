package com.vallexia.recipe.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for recipe ingredients.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {
    
    private Long ingredientId;
    
    @NotBlank(message = "Ingredient name is required")
    @Size(max = 255, message = "Ingredient name must not exceed 255 characters")
    private String name;
    
    @NotNull(message = "Quantity is required")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be 0 or greater")
    private Integer displayOrder;
}
