package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported first day of week options for user display preferences.
 * 
 * <p>To add a new first day of week option:
 * <ol>
 *   <li>Add the enum constant with its display label</li>
 *   <li>Expose any necessary translation strings in the frontend</li>
 *   <li>The enums will automatically flow through {@link com.vallexia.common.controller.LocaleConfigController}</li>
 * </ol>
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
    private static final Map<String, SupportedFirstDayOfWeek> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    day -> day.name().toUpperCase(Locale.ROOT),
                    day -> day));

    SupportedFirstDayOfWeek(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get all supported first day of week options.
     * 
     * @return List of all supported first day of week options
     */
    public static List<SupportedFirstDayOfWeek> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Get a supported first day of week by code.
     * 
     * @param code the code to get
     * @return Optional containing the supported first day of week, or empty if not found
     */
    public static Optional<SupportedFirstDayOfWeek> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
}
