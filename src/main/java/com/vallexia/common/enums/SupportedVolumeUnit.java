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
 * Supported volume units with conversion metadata (base unit milliliter).
 * 
 * <p>To add a new volume unit:
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
public enum SupportedVolumeUnit {
    CUP("cup", new BigDecimal("236.588")),
    TABLESPOON("tbsp", new BigDecimal("14.7868")),
    TEASPOON("tsp", new BigDecimal("4.92892")),
    MILLILITER("ml", BigDecimal.ONE),
    LITER("l", new BigDecimal("1000")),
    FLUID_OUNCE("fl oz", new BigDecimal("29.5735"));

    private final String display;
    private final BigDecimal milliliters;
    private static final Map<String, SupportedVolumeUnit> BY_DISPLAY = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    unit -> unit.getDisplay().toLowerCase(Locale.ROOT),
                    unit -> unit));

    SupportedVolumeUnit(String display, BigDecimal milliliters) {
        this.display = display;
        this.milliliters = milliliters;
    }

    /**
     * Get all supported volume units.
     * 
     * @return List of all supported volume units
     */
    public static List<SupportedVolumeUnit> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Find volume unit by display string (case-insensitive, handles plurals and variations).
     * 
     * @param display the display string to match
     * @return Optional containing the matching unit, or empty if not found
     */
    public static Optional<SupportedVolumeUnit> fromDisplay(String display) {
        if (display == null || display.isBlank()) {
            return Optional.empty();
        }
        String normalized = display.trim().toLowerCase(Locale.ROOT);
        
        // Try exact match first using the map
        SupportedVolumeUnit exactMatch = BY_DISPLAY.get(normalized);
        if (exactMatch != null) {
            return Optional.of(exactMatch);
        }
        
        // Handle plurals and common variations
        for (SupportedVolumeUnit unit : values()) {
            switch (unit) {
                case CUP:
                    if (normalized.equals("cups")) {
                        return Optional.of(unit);
                    }
                    break;
                case TABLESPOON:
                    if (normalized.equals("tablespoon") || normalized.equals("tablespoons")) {
                        return Optional.of(unit);
                    }
                    break;
                case TEASPOON:
                    if (normalized.equals("teaspoon") || normalized.equals("teaspoons")) {
                        return Optional.of(unit);
                    }
                    break;
                case MILLILITER:
                    if (normalized.equals("milliliter") || normalized.equals("milliliters")) {
                        return Optional.of(unit);
                    }
                    break;
                case LITER:
                    if (normalized.equals("liter") || normalized.equals("liters")) {
                        return Optional.of(unit);
                    }
                    break;
                case FLUID_OUNCE:
                    if (normalized.equals("fluid ounce") || normalized.equals("fluid ounces")) {
                        return Optional.of(unit);
                    }
                    break;
            }
        }
        
        return Optional.empty();
    }
}
