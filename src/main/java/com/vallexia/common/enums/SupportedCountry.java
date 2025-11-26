package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported countries in the application.
 *
 * <p>To register a new country:
 * <ol>
 *   <li>Add the enum constant with locale, date/time defaults, separators, and currency</li>
 *   <li>Ensure {@link SupportedCurrency}, {@link SupportedLocale}, and other referenced enums contain the value</li>
 *   <li>No controller changes are needed—{@code LocaleConfigController} will pick it up automatically</li>
 * </ol>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedCountry {
    US(
            "US",
            "United States",
            SupportedLocale.EN,
            SupportedDateFormat.MM_DD_YYYY,
            SupportedTimezone.AMERICA_NEW_YORK,
            SupportedFirstDayOfWeek.SUNDAY,
            SupportedMeasurementSystem.IMPERIAL,
            ".",
            ",",
            SupportedCurrency.USD
    ),
    DK(
            "DK",
            "Denmark",
            SupportedLocale.DA,
            SupportedDateFormat.DD_MM_YYYY_DOT,
            SupportedTimezone.EUROPE_COPENHAGEN,
            SupportedFirstDayOfWeek.MONDAY,
            SupportedMeasurementSystem.METRIC,
            ",",
            ".",
            SupportedCurrency.DKK
    );

    private final String countryCode;
    private final String displayName;
    private final SupportedLocale locale;
    private final SupportedDateFormat defaultDateFormat;
    private final SupportedTimezone defaultTimezone;
    private final SupportedFirstDayOfWeek firstDayOfWeek;
    private final SupportedMeasurementSystem measurementSystem;
    private final String decimalSeparator;
    private final String thousandsSeparator;
    private final SupportedCurrency currency;

    SupportedCountry(String countryCode,
                     String displayName,
                     SupportedLocale locale,
                     SupportedDateFormat defaultDateFormat,
                     SupportedTimezone defaultTimezone,
                     SupportedFirstDayOfWeek firstDayOfWeek,
                     SupportedMeasurementSystem measurementSystem,
                     String decimalSeparator,
                     String thousandsSeparator,
                     SupportedCurrency currency) {
        this.countryCode = countryCode;
        this.displayName = displayName;
        this.locale = locale;
        this.defaultDateFormat = defaultDateFormat;
        this.defaultTimezone = defaultTimezone;
        this.firstDayOfWeek = firstDayOfWeek;
        this.measurementSystem = measurementSystem;
        this.decimalSeparator = decimalSeparator;
        this.thousandsSeparator = thousandsSeparator;
        this.currency = currency;
    }

    private static final Map<String, SupportedCountry> BY_COUNTRY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    country -> country.countryCode.toUpperCase(Locale.ROOT),
                    country -> country));

    /**
     * Get the currency code for the country.
     * 
     * @return Currency code
     */
    public String getCurrencyCode() {
        return currency.getCode();
    }

    /**
     * Get a supported country by country code.
     * 
     * @param countryCode the country code
     * @return Optional containing the supported country, or empty if not found
     */
    public static Optional<SupportedCountry> fromCountry(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return Optional.empty();
        }
        String normalized = countryCode.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_COUNTRY_CODE.get(normalized));
    }

    /**
     * Get all supported countries.
     * 
     * @return List of all supported countries
     */
    public static List<SupportedCountry> getAll() {
        return Arrays.asList(values());
    }
}
