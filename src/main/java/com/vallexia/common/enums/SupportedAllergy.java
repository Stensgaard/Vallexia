package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported food allergies used across the application.
 * 
 * <p>To add a new allergy:
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
public enum SupportedAllergy {
    DAIRY("Dairy", "dairy"),
    EGG("Egg", "egg"),
    GLUTEN("Gluten", "gluten"),
    GRAIN("Grain", "grain"),
    PEANUT("Peanut", "peanut"),
    SEAFOOD("Seafood", "seafood"),
    SESAME("Sesame", "sesame"),
    SHELLFISH("Shellfish", "shellfish"),
    SOY("Soy", "soy"),
    SULFITE("Sulfite", "sulfite"),
    TREE_NUT("Tree Nut", "tree nut"),
    WHEAT("Wheat", "wheat");
    
    private final String displayName;
    private final String spoonacularValue;
    private static final Map<String, SupportedAllergy> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    allergy -> allergy.name().toUpperCase(Locale.ROOT),
                    allergy -> allergy));
    
    SupportedAllergy(String displayName, String spoonacularValue) {
        this.displayName = displayName;
        this.spoonacularValue = spoonacularValue;
    }
    
    /**
     * Get the Spoonacular API value for this allergy/intolerance.
     * 
     * @return the Spoonacular API string value
     */
    public String getSpoonacularValue() {
        return spoonacularValue;
    }
    
    /**
     * Get all supported allergies.
     * 
     * @return List of all supported allergies
     */
    public static List<SupportedAllergy> getAll() {
        return Arrays.asList(values());
    }
    
    /**
     * Get a supported allergy by code.
     * 
     * @param code the code to get
     * @return Optional containing the supported allergy, or empty if not found
     */
    public static Optional<SupportedAllergy> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
    
    /**
     * Get a supported allergy by Spoonacular API value.
     * 
     * @param spoonacularValue the Spoonacular API value
     * @return Optional containing the supported allergy, or empty if not found
     */
    public static Optional<SupportedAllergy> fromSpoonacularValue(String spoonacularValue) {
        if (spoonacularValue == null || spoonacularValue.isBlank()) {
            return Optional.empty();
        }
        String normalized = spoonacularValue.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(allergy -> allergy.spoonacularValue.equalsIgnoreCase(normalized))
                .findFirst();
    }
}
