package com.vallexia.user.entity;

import lombok.Getter;

/**
 * Cuisine types for user preferences.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum CuisineType {
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
    SOUTH_AMERICAN("South American"),
    FUSION("Fusion");
    
    private final String displayName;
    
    CuisineType(String displayName) {
        this.displayName = displayName;
    }
}
