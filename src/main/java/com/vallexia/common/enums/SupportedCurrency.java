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

    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(SupportedCurrency::getCode)
                .toList();
    }

    public static List<SupportedCurrency> getAll() {
        return Arrays.asList(values());
    }

    public static Optional<SupportedCurrency> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }

    public static boolean supports(String code) {
        return fromCode(code).isPresent();
    }
}
