package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported cuisine types used across the application.
 * 
 * <p>To add a new cuisine type:
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
public enum SupportedCuisineType {
    AFRICAN("African", "african"),
    AMERICAN("American", "american"),
    BRITISH("British", "british"),
    CAJUN("Cajun", "cajun"),
    CARIBBEAN("Caribbean", "caribbean"),
    CHINESE("Chinese", "chinese"),
    EASTERN_EUROPEAN("Eastern European", "eastern european"),
    FRENCH("French", "french"),
    GERMAN("German", "german"),
    GREEK("Greek", "greek"),
    INDIAN("Indian", "indian"),
    IRISH("Irish", "irish"),
    ITALIAN("Italian", "italian"),
    JAPANESE("Japanese", "japanese"),
    JEWISH("Jewish", "jewish"),
    KOREAN("Korean", "korean"),
    LATIN_AMERICAN("Latin American", "latin american"),
    MEDITERRANEAN("Mediterranean", "mediterranean"),
    MEXICAN("Mexican", "mexican"),
    MIDDLE_EASTERN("Middle Eastern", "middle eastern"),
    NORDIC("Nordic", "nordic"),
    SOUTHERN("Southern", "southern"),
    SPANISH("Spanish", "spanish"),
    THAI("Thai", "thai"),
    VIETNAMESE("Vietnamese", "vietnamese");
    
    private final String displayName;
    private final String spoonacularValue;
    private static final Map<String, SupportedCuisineType> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    cuisineType -> cuisineType.name().toUpperCase(Locale.ROOT),
                    cuisineType -> cuisineType));
    
    SupportedCuisineType(String displayName, String spoonacularValue) {
        this.displayName = displayName;
        this.spoonacularValue = spoonacularValue;
    }
    
    /**
     * Get the Spoonacular API value for this cuisine type.
     * 
     * @return the Spoonacular API string value
     */
    public String getSpoonacularValue() {
        return spoonacularValue;
    }
    
    /**
     * Get all supported cuisine types.
     * 
     * @return List of all supported cuisine types
     */
    public static List<SupportedCuisineType> getAll() {
        return Arrays.asList(values());
    }
    
    /**
     * Get a supported cuisine type by code.
     * 
     * @param code the code to get
     * @return Optional containing the supported cuisine type, or empty if not found
     */
    public static Optional<SupportedCuisineType> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
    
    /**
     * Get a supported cuisine type by Spoonacular API value.
     * 
     * @param spoonacularValue the Spoonacular API value
     * @return Optional containing the supported cuisine type, or empty if not found
     */
    public static Optional<SupportedCuisineType> fromSpoonacularValue(String spoonacularValue) {
        if (spoonacularValue == null || spoonacularValue.isBlank()) {
            return Optional.empty();
        }
        String normalized = spoonacularValue.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(cuisine -> cuisine.spoonacularValue.equalsIgnoreCase(normalized))
                .findFirst();
    }
}
