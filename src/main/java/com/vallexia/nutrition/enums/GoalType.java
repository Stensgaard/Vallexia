package com.vallexia.nutrition.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Nutritional goal types for different fitness objectives.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-27
 */
@Getter
public enum GoalType {
    WEIGHT_LOSS("Weight Loss"),
    WEIGHT_GAIN("Weight Gain"),
    MUSCLE_GAIN("Muscle Gain"),
    MAINTENANCE("Maintenance"),
    ATHLETIC_PERFORMANCE("Athletic Performance"),
    GENERAL_HEALTH("General Health");
    
    private static final Map<String, GoalType> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    goalType -> goalType.name().toUpperCase(Locale.ROOT),
                    goalType -> goalType));
    
    private final String displayName;
    
    GoalType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Resolve enum from client-provided code in a locale-safe manner.
     * Trims whitespace, uppercases with {@link Locale#ROOT}, and matches
     * against the enum name (e.g., "WEIGHT_LOSS").
     *
     * @param code external code value
     * @return matching enum value if present
     */
    public static Optional<GoalType> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
}
