package com.vallexia.user.entity;

import lombok.Getter;

/**
 * Meal types that users can track in their meal plans.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum MealType {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");
    
    private final String displayName;
    
    MealType(String displayName) {
        this.displayName = displayName;
    }
}
