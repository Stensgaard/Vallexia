package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported currencies in the application.
 *
 * <p>To add a currency:
 * <ol>
 *   <li>Add the enum constant with ISO code and display name</li>
 *   <li>Update any dependent countries in {@link SupportedCountry}</li>
 *   <li>No further wiring is necessary; lookup helpers pick up the new value automatically</li>
 * </ol>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedCurrency {
    USD("USD", "US Dollar"),
    DKK("DKK", "Danish Krone");

    private final String code;
    private final String name;
    private static final Map<String, SupportedCurrency> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(SupportedCurrency::getCode, currency -> currency));

    SupportedCurrency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Get all supported currencies.
     * 
     * @return List of all supported currencies
     */
    public static List<SupportedCurrency> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Get a supported currency by code.
     * 
     * @param code the currency code
     * @return Optional containing the supported currency, or empty if not found
     */
    public static Optional<SupportedCurrency> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }
}
