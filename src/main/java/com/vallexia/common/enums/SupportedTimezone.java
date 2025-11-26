package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enumeration of supported timezones that the frontend can present to users.
 * 
 * <p>To add a new timezone:
 * <ol>
 *   <li>Add the enum constant with its value and label</li>
 *   <li>Expose any necessary translation strings in the frontend</li>
 *   <li>The enums will automatically flow through {@link com.vallexia.common.controller.LocaleConfigController}</li>
 * </ol>
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedTimezone {
    UTC("UTC", "UTC (Coordinated Universal Time)"),
    AMERICA_NEW_YORK("America/New_York", "Eastern Time (US & Canada)"),
    AMERICA_LOS_ANGELES("America/Los_Angeles", "Pacific Time (US & Canada)"),
    EUROPE_COPENHAGEN("Europe/Copenhagen", "Copenhagen");

    private final String value;
    private final String label;
    private static final Map<String, SupportedTimezone> BY_VALUE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    tz -> tz.getValue().toLowerCase(Locale.ROOT),
                    tz -> tz));

    SupportedTimezone(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * Get all supported timezones.
     * 
     * @return List of all supported timezones
     */
    public static List<SupportedTimezone> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Resolve enum from IANA timezone identifier value.
     * Trims whitespace and matches case-insensitively against the timezone value
     * (e.g., "America/New_York", "UTC").
     *
     * @param value IANA timezone identifier
     * @return matching enum value if present
     */
    public static Optional<SupportedTimezone> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(BY_VALUE.get(normalized));
    }
}
