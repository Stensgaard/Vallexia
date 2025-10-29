package com.vallexia.user.dto;

import com.vallexia.user.entity.GoalType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for nutritional goals.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionalGoalsDto {
    
    private Long id;
    private Long userId;
    
    @NotNull(message = "Daily calories is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily calories must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Daily calories must not exceed 10000")
    private BigDecimal dailyCalories;
    
    @DecimalMin(value = "0.0", message = "Daily protein must be 0 or greater")
    @DecimalMax(value = "1000.0", message = "Daily protein must not exceed 1000 grams")
    private BigDecimal dailyProtein;
    
    @DecimalMin(value = "0.0", message = "Daily carbs must be 0 or greater")
    @DecimalMax(value = "1500.0", message = "Daily carbs must not exceed 1500 grams")
    private BigDecimal dailyCarbs;
    
    @DecimalMin(value = "0.0", message = "Daily fats must be 0 or greater")
    @DecimalMax(value = "500.0", message = "Daily fats must not exceed 500 grams")
    private BigDecimal dailyFats;
    
    @DecimalMin(value = "0.0", message = "Protein percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Protein percentage must not exceed 100")
    private BigDecimal proteinPercentage;
    
    @DecimalMin(value = "0.0", message = "Carbs percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Carbs percentage must not exceed 100")
    private BigDecimal carbsPercentage;
    
    @DecimalMin(value = "0.0", message = "Fats percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Fats percentage must not exceed 100")
    private BigDecimal fatsPercentage;
    
    @DecimalMin(value = "0.0", message = "Daily fiber must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Daily fiber must not exceed 100 grams")
    private BigDecimal dailyFiber;
    
    @DecimalMin(value = "0.0", message = "Daily sodium must be 0 or greater")
    @DecimalMax(value = "10000.0", message = "Daily sodium must not exceed 10000 mg")
    private BigDecimal dailySodium;
    
    @DecimalMin(value = "0.0", message = "Daily sugar must be 0 or greater")
    @DecimalMax(value = "10000.0", message = "Daily sugar must not exceed 10000 grams")
    private BigDecimal dailySugar;
    
    private GoalType goalType;
}
