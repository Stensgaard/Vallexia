package com.vallexia.user.entity.enums;

import lombok.Getter;

/**
 * Common food allergies that users can specify.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum Allergy {
    PEANUTS("Peanuts"),
    TREE_NUTS("Tree Nuts"),
    MILK("Milk"),
    EGGS("Eggs"),
    FISH("Fish"),
    SHELLFISH("Shellfish"),
    SOY("Soy"),
    WHEAT("Wheat"),
    SESAME("Sesame"),
    MUSTARD("Mustard"),
    CELERY("Celery"),
    LUPIN("Lupin"),
    SULFITES("Sulfites");
    
    private final String displayName;
    
    Allergy(String displayName) {
        this.displayName = displayName;
    }
}
