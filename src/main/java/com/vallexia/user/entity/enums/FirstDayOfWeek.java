package com.vallexia.user.entity.enums;

import lombok.Getter;

/**
 * First day of week options for user display preferences.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum FirstDayOfWeek {
    SUNDAY(0, "Sunday"),
    MONDAY(1, "Monday");
    
    private final int value;
    private final String displayName;
    
    FirstDayOfWeek(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    /**
     * Get FirstDayOfWeek enum from integer value.
     * 
     * @param value integer value (0 for Sunday, 1 for Monday)
     * @return FirstDayOfWeek enum
     * @throws IllegalArgumentException if value is not 0 or 1
     */
    public static FirstDayOfWeek fromValue(int value) {
        for (FirstDayOfWeek day : values()) {
            if (day.value == value) {
                return day;
            }
        }

        // TODO: Add a custom exception for this
        throw new IllegalArgumentException("Invalid first day of week value: " + value);
    }
}
