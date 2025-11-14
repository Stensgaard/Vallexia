package com.vallexia.user.entity.enums;

import lombok.Getter;

/**
 * Dietary restrictions that users can specify.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum DietaryRestriction {
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
    
    DietaryRestriction(String displayName) {
        this.displayName = displayName;
    }
}
