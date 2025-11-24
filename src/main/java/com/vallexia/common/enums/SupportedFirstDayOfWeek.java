package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Supported first day of week options for user display preferences.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedFirstDayOfWeek {
    SUNDAY("Sunday"),
    MONDAY("Monday");

    private final String displayName;

    SupportedFirstDayOfWeek(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Resolve enum from client-provided code in a locale-safe manner.
     * Trims whitespace, uppercases with {@link Locale#ROOT}, and matches
     * against the enum name (e.g., "MONDAY").
     *
     * @param code external code value
     * @return matching enum value if present
     */
    public static Optional<SupportedFirstDayOfWeek> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(day -> day.name().equals(normalized))
                .findFirst();
    }
}
