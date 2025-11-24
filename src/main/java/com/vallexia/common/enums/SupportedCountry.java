package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enumeration of supported countries in the application.
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
            SupportedCurrency.USD.getCode()
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
            SupportedCurrency.DKK.getCode()
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
    private final String currencyCode;

    SupportedCountry(String countryCode,
                     String displayName,
                     SupportedLocale locale,
                     SupportedDateFormat defaultDateFormat,
                     SupportedTimezone defaultTimezone,
                     SupportedFirstDayOfWeek firstDayOfWeek,
                     SupportedMeasurementSystem measurementSystem,
                     String decimalSeparator,
                     String thousandsSeparator,
                     String currencyCode) {
        this.countryCode = countryCode;
        this.displayName = displayName;
        this.locale = locale;
        this.defaultDateFormat = defaultDateFormat;
        this.defaultTimezone = defaultTimezone;
        this.firstDayOfWeek = firstDayOfWeek;
        this.measurementSystem = measurementSystem;
        this.decimalSeparator = decimalSeparator;
        this.thousandsSeparator = thousandsSeparator;
        this.currencyCode = currencyCode;
    }

    public static Optional<SupportedCountry> fromCountry(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(rule -> rule.countryCode.equalsIgnoreCase(countryCode))
                .findFirst();
    }

    public static List<SupportedCountry> getAll() {
        return Arrays.asList(values());
    }
}
