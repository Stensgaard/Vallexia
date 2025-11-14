package com.vallexia.recipe.entity.enums;

import lombok.Getter;

/**
 * Recipe categories for organizing recipes.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum RecipeCategory {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack"),
    DESSERT("Dessert"),
    APPETIZER("Appetizer"),
    BEVERAGE("Beverage"),
    OTHER("Other");
    
    private final String displayName;
    
    RecipeCategory(String displayName) {
        this.displayName = displayName;
    }
}
