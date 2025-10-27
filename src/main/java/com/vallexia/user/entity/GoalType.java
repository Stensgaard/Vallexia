package com.vallexia.user.entity;

import lombok.Getter;

/**
 * Nutritional goal types for different fitness objectives.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum GoalType {
    WEIGHT_LOSS("Weight Loss"),
    WEIGHT_GAIN("Weight Gain"),
    MUSCLE_GAIN("Muscle Gain"),
    MAINTENANCE("Maintenance"),
    ATHLETIC_PERFORMANCE("Athletic Performance"),
    GENERAL_HEALTH("General Health");
    
    private final String displayName;
    
    GoalType(String displayName) {
        this.displayName = displayName;
    }
}
