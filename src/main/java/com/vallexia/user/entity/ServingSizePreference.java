package com.vallexia.user.entity;

import lombok.Getter;

/**
 * Serving size preferences for meal planning.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum ServingSizePreference {
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large"),
    EXTRA_LARGE("Extra Large");
    
    private final String displayName;
    
    ServingSizePreference(String displayName) {
        this.displayName = displayName;
    }
}
