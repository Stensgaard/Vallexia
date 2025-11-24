package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumeration of supported locales in the application.
 * 
 * To add a new locale:
 * 1. Add a new enum value here (e.g., FR("fr"))
 * 2. Add the corresponding translation file (e.g., fr.json) in the frontend locales directory
 * 3. All validation and API endpoints will automatically support the new locale
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Getter
public enum SupportedLocale {
    EN("en"),
    DA("da");

    private static final Set<String> CACHED_CODES =
            Collections.unmodifiableSet(
                    Arrays.stream(values())
                            .map(locale -> locale.getCode().toLowerCase())
                            .collect(Collectors.toSet()));

    private final String code;

    SupportedLocale(String code) {
        this.code = code;
    }

    /**
     * Get all supported locale codes as a Set.
     *
     * @return Set of locale codes (e.g., ["en", "da"])
     */
    public static Set<String> getAllCodes() {
        return CACHED_CODES;
    }
    
    /**
     * Get all supported locale codes as a sorted array.
     * 
     * @return Array of locale codes sorted alphabetically
     */
    public static String[] getAllCodesArray() {
        return CACHED_CODES.stream()
                .sorted()
                .toArray(String[]::new);
    }
    
    /**
     * Check if a locale code is supported.
     * 
     * @param localeCode the locale code to check
     * @return true if the locale is supported, false otherwise
     */
    public static boolean isSupported(String localeCode) {
        if (localeCode == null || localeCode.isEmpty()) {
            return false;
        }
        return CACHED_CODES.contains(localeCode.toLowerCase());
    }
    
    /**
     * Get the SupportedLocale enum value from a locale code.
     * 
     * @param localeCode the locale code
     * @return Optional containing the SupportedLocale enum value if found
     */
    public static Optional<SupportedLocale> fromCode(String localeCode) {
        if (localeCode == null || localeCode.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(locale -> locale.getCode().equalsIgnoreCase(localeCode))
                .findFirst();
    }
}
