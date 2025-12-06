package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported dietary restrictions used across the application.
 * 
 * <p>To add a new dietary restriction:
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
public enum SupportedDietaryRestriction {
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    GLUTEN_FREE("Gluten-Free"),
    DAIRY_FREE("Dairy-Free"),
    NUT_FREE("Nut-Free"),
    SOY_FREE("Soy-Free"),
    EGG_FREE("Egg-Free"),
    LOW_CARB("Low-Carb"),
    KETO("Keto"),
    PALEO("Paleo"),
    MEDITERRANEAN("Mediterranean"),
    LOW_SODIUM("Low-Sodium"),
    LOW_FAT("Low-Fat"),
    HIGH_PROTEIN("High-Protein"),
    HALAL("Halal"),
    KOSHER("Kosher");
    
    private final String displayName;
    private static final Map<String, SupportedDietaryRestriction> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    restriction -> restriction.name().toUpperCase(Locale.ROOT),
                    restriction -> restriction));
    
    SupportedDietaryRestriction(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get all supported dietary restrictions.
     * 
     * @return List of all supported dietary restrictions
     */
    public static List<SupportedDietaryRestriction> getAll() {
        return Arrays.asList(values());
    }
    
    /**
     * Get a supported dietary restriction by code.
     * 
     * @param code the code to get
     * @return Optional containing the supported dietary restriction, or empty if not found
     */
    public static Optional<SupportedDietaryRestriction> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
}
