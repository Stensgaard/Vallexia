package com.vallexia.recipe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing nutritional information for an ingredient.
 * Nutritional values are stored per 100g or per standard unit.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "ingredient_nutrition")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientNutrition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false, unique = true)
    @NotNull
    private Ingredient ingredient;
    
    /**
     * Nutritional values per 100g (standard unit for food databases).
     */
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "calories_per_100g", precision = 10, scale = 2)
    private BigDecimal caloriesPer100g;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "protein_per_100g", precision = 10, scale = 2)
    private BigDecimal proteinPer100g;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "carbs_per_100g", precision = 10, scale = 2)
    private BigDecimal carbsPer100g;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "fats_per_100g", precision = 10, scale = 2)
    private BigDecimal fatsPer100g;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "fiber_per_100g", precision = 10, scale = 2)
    private BigDecimal fiberPer100g;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "sodium_per_100g", precision = 10, scale = 2)
    private BigDecimal sodiumPer100g;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "sugar_per_100g", precision = 10, scale = 2)
    private BigDecimal sugarPer100g;
    
    /**
     * Standard unit for this ingredient (e.g., "g", "ml", "cup", "piece").
     * Used for converting recipe quantities to grams for calculation.
     */
    @Column(name = "standard_unit", length = 50)
    private String standardUnit = "g";
    
    /**
     * Conversion factor from standard unit to grams.
     * For example: 1 cup flour = 125g, so conversion_factor = 125.0
     */
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "conversion_factor_to_grams", precision = 10, scale = 2)
    private BigDecimal conversionFactorToGrams;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
