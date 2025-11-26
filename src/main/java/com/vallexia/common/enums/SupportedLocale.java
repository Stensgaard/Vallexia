package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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

    private final String code;
    private static final Map<String, SupportedLocale> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    locale -> locale.getCode().toLowerCase(Locale.ROOT),
                    locale -> locale));

    SupportedLocale(String code) {
        this.code = code;
    }

    /**
     * Get all supported locale enum values.
     *
     * @return List of all SupportedLocale enum values
     */
    public static List<SupportedLocale> getAll() {
        return Arrays.asList(values());
    }
    
    /**
     * Get the SupportedLocale enum value from a locale code.
     * 
     * @param localeCode the locale code
     * @return Optional containing the SupportedLocale enum value if found
     */
    public static Optional<SupportedLocale> fromCode(String localeCode) {
        if (localeCode == null || localeCode.isBlank()) {
            return Optional.empty();
        }
        String normalized = localeCode.trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
}
