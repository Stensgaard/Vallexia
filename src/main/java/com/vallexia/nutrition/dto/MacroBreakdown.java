package com.vallexia.nutrition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for macro nutrient breakdown.
 * 
 * <p>Represents calculated macro values (protein, carbs, fats) in grams
 * based on goal type and daily calories.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MacroBreakdown {
    
    /**
     * Protein in grams.
     */
    private BigDecimal protein;
    
    /**
     * Carbohydrates in grams.
     */
    private BigDecimal carbs;
    
    /**
     * Fats in grams.
     */
    private BigDecimal fats;
}
