package com.vallexia.common.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        for (SupportedWeightUnit unit : values()) {
            switch (unit) {
                case GRAM:
                    if (normalized.equals("gram") || normalized.equals("grams")) {
                        return Optional.of(unit);
                    }
                    break;
                case KILOGRAM:
                    if (normalized.equals("kilogram") || normalized.equals("kilograms")) {
                        return Optional.of(unit);
                    }
                    break;
                case MILLIGRAM:
                    if (normalized.equals("milligram") || normalized.equals("milligrams")) {
                        return Optional.of(unit);
                    }
                    break;
                case OUNCE:
                    if (normalized.equals("ounce") || normalized.equals("ounces")) {
                        return Optional.of(unit);
                    }
                    break;
                case POUND:
                    if (normalized.equals("pound") || normalized.equals("pounds") || normalized.equals("lbs")) {
                        return Optional.of(unit);
                    }
                    break;
            }
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
