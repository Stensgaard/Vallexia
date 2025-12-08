package com.vallexia.common.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.Map.entry;

/**
 * Supported weight units with conversion metadata (base unit grams).
 * 
 * <p>To add a new weight unit:
 * <ol>
 *   <li>Add the enum constant with its display label and conversion factor</li>
 *   <li>Expose any necessary translation strings in the frontend</li>
 *   <li>The enums will automatically flow through {@link LocaleConfigController}</li>
 * </ol>
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedWeightUnit {
    GRAM("g", BigDecimal.ONE),
    KILOGRAM("kg", new BigDecimal("1000")),
    MILLIGRAM("mg", new BigDecimal("0.001")),
    OUNCE("oz", new BigDecimal("28.3495")),
    POUND("lb", new BigDecimal("453.592"));

    private final String display;
    private final BigDecimal grams;
    private static final Map<String, SupportedWeightUnit> BY_DISPLAY = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    unit -> unit.getDisplay().toLowerCase(Locale.ROOT),
                    unit -> unit));
    
    private static final Map<String, SupportedWeightUnit> BY_VARIATION = Map.ofEntries(
            entry("gram", GRAM),
            entry("grams", GRAM),
            entry("kilogram", KILOGRAM),
            entry("kilograms", KILOGRAM),
            entry("milligram", MILLIGRAM),
            entry("milligrams", MILLIGRAM),
            entry("ounce", OUNCE),
            entry("ounces", OUNCE),
            entry("pound", POUND),
            entry("pounds", POUND),
            entry("lbs", POUND)
    );

    SupportedWeightUnit(String display, BigDecimal grams) {
        this.display = display;
        this.grams = grams;
    }

    /**
     * Get all supported weight units.
     * 
     * @return List of all supported weight units
     */
    public static List<SupportedWeightUnit> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Find weight unit by display string (case-insensitive, handles plurals and variations).
     * 
     * @param display the display string to match
     * @return Optional containing the matching unit, or empty if not found
     */
    public static Optional<SupportedWeightUnit> fromDisplay(String display) {
        if (display == null || display.isBlank()) {
            return Optional.empty();
        }
        String normalized = display.trim().toLowerCase(Locale.ROOT);
        
        // Try exact match first using the map
        SupportedWeightUnit exactMatch = BY_DISPLAY.get(normalized);
        if (exactMatch != null) {
            return Optional.of(exactMatch);
        }
        
        // Handle plurals and common variations
        SupportedWeightUnit variationMatch = BY_VARIATION.get(normalized);
        if (variationMatch != null) {
            return Optional.of(variationMatch);
        }
        
        return Optional.empty();
    }

    /**
     * Check if this unit is a metric unit.
     * 
     * @return true if metric (gram, kilogram, milligram), false otherwise
     */
    public boolean isMetric() {
        return this == GRAM || this == KILOGRAM || this == MILLIGRAM;
    }

    /**
     * Check if this unit is an imperial unit.
     * 
     * @return true if imperial (ounce, pound), false otherwise
     */
    public boolean isImperial() {
        return this == OUNCE || this == POUND;
    }
}
