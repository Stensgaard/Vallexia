package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Measurement system options for user preferences.
 *
 * <p>To add a new measurement system:
 * <ol>
 *   <li>Add the enum constant with its display label</li>
 *   <li>Expose any necessary translation strings in the frontend</li>
 *   <li>The enums will automatically flow through {@link LocaleConfigController}</li>
 * </ol>
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedMeasurementSystem {
    METRIC("Metric"),
    IMPERIAL("Imperial");

    private final String displayName;
    private static final Map<String, SupportedMeasurementSystem> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    ms -> ms.name().toUpperCase(Locale.ROOT),
                    ms -> ms));

    SupportedMeasurementSystem(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get all supported measurement systems.
     * 
     * @return List of all supported measurement systems
     */
    public static List<SupportedMeasurementSystem> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Get a supported measurement system by code.
     * 
     * @param code the code to get
     * @return Optional containing the supported measurement system, or empty if not found
     */
    public static Optional<SupportedMeasurementSystem> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
}
