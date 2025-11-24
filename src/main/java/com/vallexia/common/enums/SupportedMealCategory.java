package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Supported meal categories used for recipe tagging and meal-plan selections.
 *
 * <p>To add a new meal category:
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
public enum SupportedMealCategory {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack"),
    DESSERT("Dessert"),
    APPETIZER("Appetizer"),
    BEVERAGE("Beverage");

    private final String displayName;

    SupportedMealCategory(String displayName) {
        this.displayName = displayName;
    }

    public static List<SupportedMealCategory> getAll() {
        return Arrays.asList(values());
    }

    public static Optional<SupportedMealCategory> fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(normalized))
                .findFirst();
    }
}
