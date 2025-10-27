package com.vallexia.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
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
 * Nutritional goals entity storing user's daily nutritional targets.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "nutritional_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionalGoals {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax(value = "10000.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyCalories;
    
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax(value = "1000.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyProtein; // in grams
    
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax(value = "1000.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyCarbs; // in grams
    
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax(value = "1000.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyFats; // in grams
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal proteinPercentage;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal carbsPercentage;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal fatsPercentage;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(precision = 5, scale = 2)
    private BigDecimal dailyFiber; // in grams
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10000.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal dailySodium; // in milligrams
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10000.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal dailySugar; // in grams
    
    @Enumerated(EnumType.STRING)
    private GoalType goalType = GoalType.MAINTENANCE;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
