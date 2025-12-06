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
    AMERICAN("American"),
    ITALIAN("Italian"),
    MEXICAN("Mexican"),
    CHINESE("Chinese"),
    JAPANESE("Japanese"),
    THAI("Thai"),
    INDIAN("Indian"),
    FRENCH("French"),
    MEDITERRANEAN("Mediterranean"),
    GREEK("Greek"),
    SPANISH("Spanish"),
    GERMAN("German"),
    BRITISH("British"),
    KOREAN("Korean"),
    VIETNAMESE("Vietnamese"),
    MIDDLE_EASTERN("Middle Eastern"),
    CARIBBEAN("Caribbean"),
    AFRICAN("African"),
    SOUTH_AMERICAN("South American");
    
    private final String displayName;
    private static final Map<String, SupportedCuisineType> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    cuisineType -> cuisineType.name().toUpperCase(Locale.ROOT),
                    cuisineType -> cuisineType));
    
    SupportedCuisineType(String displayName) {
        this.displayName = displayName;
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
}
