package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Measurement system options for user preferences.
 *
 * <p>To add a new measurement system:
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
public enum SupportedMeasurementSystem {
    METRIC("Metric"),
    IMPERIAL("Imperial");

    private final String displayName;

    SupportedMeasurementSystem(String displayName) {
        this.displayName = displayName;
    }

    public static Optional<SupportedMeasurementSystem> fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(ms -> ms.name().equalsIgnoreCase(code))
                .findFirst();
    }

    public static List<SupportedMeasurementSystem> getAll() {
        return Arrays.asList(values());
    }
}
