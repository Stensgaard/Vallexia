package com.vallexia.recipe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nutritional information entity storing macro and micronutrient data for recipes.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Entity
@Table(name = "nutritional_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionalInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false, unique = true)
    private Recipe recipe;
    
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax(value = "50000.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;
    
    @DecimalMin("0.0")
    @DecimalMax(value = "5000.0")
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal protein; // in grams
    
    @DecimalMin("0.0")
    @DecimalMax(value = "10000.0")
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal carbs; // in grams
    
    @DecimalMin("0.0")
    @DecimalMax(value = "2000.0")
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal fats; // in grams
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "500.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal fiber; // in grams
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "50000.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal sodium; // in milligrams
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5000.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal sugar; // in grams
    
    @Column(nullable = false)
    private Boolean perServing = false; // true if values are per serving, false if total
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
