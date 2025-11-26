package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported meal categories used for recipe tagging and user meal planning.
 * 
 * <p>This enum contains the core meal types that are used for both recipe categorization
 * and user meal planning preferences. All meal categories are available for both purposes.
 *
 * <p>To add a new meal category:
 * <ol>
 *   <li>Add the enum constant with its display label</li>
 *   <li>Expose any necessary translation strings in the frontend</li>
 *   <li>The enums will automatically flow through {@link LocaleConfigController}</li>
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
    SNACK("Snack");

    private final String displayName;
    private static final Map<String, SupportedMealCategory> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    category -> category.name().toUpperCase(Locale.ROOT),
                    category -> category));

    SupportedMealCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get all supported meal categories.
     * 
     * @return List of all supported meal categories
     */
    public static List<SupportedMealCategory> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Get a supported meal category by code.
     * 
     * @param code the code to get
     * @return Optional containing the supported meal category, or empty if not found
     */
    public static Optional<SupportedMealCategory> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
}
