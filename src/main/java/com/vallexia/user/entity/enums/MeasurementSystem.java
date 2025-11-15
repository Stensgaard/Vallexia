package com.vallexia.user.entity.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Measurement system options for user display preferences.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum MeasurementSystem {
    METRIC("Metric"),
    IMPERIAL("Imperial");
    
    private final String displayName;
    
    MeasurementSystem(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Check if a string is a valid measurement system.
     * 
     * @param system the system string to check (e.g., "METRIC", "IMPERIAL")
     * @return true if the system is valid, false otherwise
     */
    public static boolean isValid(String system) {
        if (system == null || system.isEmpty()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(ms -> ms.name().equals(system));
    }
    
    /**
     * Get the MeasurementSystem enum value from a string.
     * 
     * @param system the system string (e.g., "METRIC", "IMPERIAL")
     * @return the MeasurementSystem enum value, or null if not found
     */
    public static MeasurementSystem fromString(String system) {
        if (system == null || system.isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(ms -> ms.name().equals(system))
                .findFirst()
                .orElse(null);
    }
}
