package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported count units (units without conversion).
 *
 * <p>To add a new count unit:
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
public enum SupportedCountUnit {
    PIECE("piece"),
    ITEM("item"),
    WHOLE("whole");

    private final String display;
    private static final Map<String, SupportedCountUnit> BY_DISPLAY = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    unit -> unit.getDisplay().toLowerCase(Locale.ROOT),
                    unit -> unit));
    /**
     * Normalized variation map for plural and abbreviated forms.
     */
    private static final Map<String, SupportedCountUnit> BY_VARIATION = Map.of(
            "pieces", PIECE,
            "pcs", PIECE,
            "pc", PIECE,
            "items", ITEM,
            "wholes", WHOLE
    );

    SupportedCountUnit(String display) {
        this.display = display;
    }

    /**
     * Get all supported count units.
     * 
     * @return List of all supported count units
     */
    public static List<SupportedCountUnit> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Find count unit by display string (case-insensitive, handles plurals and variations).
     * 
     * @param display the display string to match
     * @return Optional containing the matching unit, or empty if not found
     */
    public static Optional<SupportedCountUnit> fromDisplay(String display) {
        if (display == null || display.isBlank()) {
            return Optional.empty();
        }
        String normalized = display.trim().toLowerCase(Locale.ROOT);
        
        // Try exact match first using the map
        SupportedCountUnit exactMatch = BY_DISPLAY.get(normalized);
        if (exactMatch != null) {
            return Optional.of(exactMatch);
        }

        // Handle plurals and common variations through a lookup map
        SupportedCountUnit variationMatch = BY_VARIATION.get(normalized);
        if (variationMatch != null) {
            return Optional.of(variationMatch);
        }

        return Optional.empty();
    }
}
