package com.vallexia.common.mapper;

import com.vallexia.common.dto.*;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.enums.SupportedTimezone;

import java.util.stream.Collectors;

/**
 * Mapper utility class for converting locale and regional configuration enums to DTOs.
 * 
 * <p>This mapper provides static methods to convert locale-related enum types from
 * {@code com.vallexia.common.enums} to their corresponding DTOs. All methods perform
 * null validation on input parameters and will throw {@link IllegalArgumentException}
 * if null values are provided.
 * 
 * <p>This mapper is used by {@link com.vallexia.common.controller.LocaleConfigController}
 * to build locale configuration responses for the frontend.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class LocaleMapper {

    private LocaleMapper() {}

    /**
     * Validates that an object is not null, throwing IllegalArgumentException if it is.
     * 
     * @param obj the object to validate
     * @param paramName the parameter name for error message
     * @param <T> the type of the object
     * @return the object if not null
     * @throws IllegalArgumentException if obj is null
     */
    private static <T> T requireNonNull(T obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " must not be null");
        }
        return obj;
    }

    /**
     * Converts a {@link SupportedLocale} enum to a {@link LocaleDto}.
     * 
     * @param supportedLocale the locale enum to convert, must not be null
     * @return the locale DTO with code and name
     * @throws IllegalArgumentException if supportedLocale is null
     */
    public static LocaleDto toLocaleDto(SupportedLocale supportedLocale) {
        requireNonNull(supportedLocale, "supportedLocale");
        return LocaleDto.builder()
                .code(supportedLocale.getCode())
                .name(supportedLocale.name())
                .build();
    }

    /**
     * Converts a {@link SupportedCountry} enum to a {@link CountryDto}.
     * 
     * <p>This method accesses nested properties of the country enum. All nested
     * properties (locale, dateFormat, timezone, etc.) are expected to be non-null
     * for valid enum instances as they are set as final fields in the constructor.
     * 
     * @param country the country enum to convert, must not be null
     * @return the country DTO with all country configuration details
     * @throws IllegalArgumentException if country is null or if any nested property is null
     */
    public static CountryDto toCountryDto(SupportedCountry country) {
        requireNonNull(country, "country");
        requireNonNull(country.getLocale(), "country.locale");
        requireNonNull(country.getDefaultDateFormat(), "country.defaultDateFormat");
        requireNonNull(country.getDefaultTimezone(), "country.defaultTimezone");
        requireNonNull(country.getFirstDayOfWeek(), "country.firstDayOfWeek");
        requireNonNull(country.getMeasurementSystem(), "country.measurementSystem");
        
        return CountryDto.builder()
                .code(country.getCountryCode())
                .name(country.getDisplayName())
                .languageCode(country.getLocale().getCode())
                .defaultDateFormat(country.getDefaultDateFormat().getFormat())
                .defaultTimezone(country.getDefaultTimezone().getValue())
                .firstDayOfWeek(country.getFirstDayOfWeek().name())
                .measurementSystem(country.getMeasurementSystem().name())
                .build();
    }

    /**
     * Converts a {@link SupportedCurrency} enum to a {@link CurrencyDto}.
     * 
     * @param currency the currency enum to convert, must not be null
     * @return the currency DTO with code and name
     * @throws IllegalArgumentException if currency is null
     */
    public static CurrencyDto toCurrencyDto(SupportedCurrency currency) {
        requireNonNull(currency, "currency");
        return CurrencyDto.builder()
                .code(currency.getCode())
                .name(currency.getName())
                .build();
    }

    /**
     * Converts a {@link SupportedTimezone} enum to a {@link TimezoneDto}.
     * 
     * @param timezone the timezone enum to convert, must not be null
     * @return the timezone DTO with value and label
     * @throws IllegalArgumentException if timezone is null
     */
    public static TimezoneDto toTimezoneDto(SupportedTimezone timezone) {
        requireNonNull(timezone, "timezone");
        return TimezoneDto.builder()
                .value(timezone.getValue())
                .label(timezone.getLabel())
                .build();
    }

    /**
     * Converts a {@link SupportedCountry} enum to a {@link FormattingRuleDto}.
     * 
     * <p>This method extracts formatting rules (decimal separator, thousands separator,
     * currency code) from a country enum.
     * 
     * @param country the country enum to convert, must not be null
     * @return the formatting rule DTO with country-specific formatting information
     * @throws IllegalArgumentException if country is null
     */
    public static FormattingRuleDto toFormattingRuleDto(SupportedCountry country) {
        requireNonNull(country, "country");
        return FormattingRuleDto.builder()
                .countryCode(country.getCountryCode())
                .countryName(country.getDisplayName())
                .decimalSeparator(country.getDecimalSeparator())
                .thousandsSeparator(country.getThousandsSeparator())
                .currencyCode(country.getCurrencyCode())
                .build();
    }

    /**
     * Converts a {@link SupportedDateFormat} enum to a {@link DateFormatDto}.
     * 
     * <p>This method also converts the format tokens to DTOs using the private
     * {@link #toTokenDto(SupportedDateFormat.DateFormatToken)} method.
     * 
     * @param format the date format enum to convert, must not be null
     * @return the date format DTO with code, format string, and tokens
     * @throws IllegalArgumentException if format is null
     */
    public static DateFormatDto toDateFormatDto(SupportedDateFormat format) {
        requireNonNull(format, "format");
        return DateFormatDto.builder()
                .code(format.name())
                .format(format.getFormat())
                .tokens(format.getTokens().stream()
                        .map(LocaleMapper::toTokenDto)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Converts a {@link SupportedDateFormat.DateFormatToken} to a {@link DateFormatTokenDto}.
     * 
     * @param token the date format token to convert, must not be null
     * @return the date format token DTO with type and value
     * @throws IllegalArgumentException if token is null
     */
    private static DateFormatTokenDto toTokenDto(SupportedDateFormat.DateFormatToken token) {
        requireNonNull(token, "token");
        return DateFormatTokenDto.builder()
                .type(token.getType().name())
                .value(token.getValue())
                .build();
    }

    /**
     * Converts a {@link SupportedMeasurementSystem} enum to a {@link MeasurementSystemDto}.
     * 
     * @param measurementSystem the measurement system enum to convert, must not be null
     * @return the measurement system DTO with code and display name
     * @throws IllegalArgumentException if measurementSystem is null
     */
    public static MeasurementSystemDto toMeasurementSystemDto(SupportedMeasurementSystem measurementSystem) {
        requireNonNull(measurementSystem, "measurementSystem");
        return MeasurementSystemDto.builder()
                .code(measurementSystem.name())
                .name(measurementSystem.getDisplayName())
                .build();
    }

    /**
     * Converts a {@link SupportedFirstDayOfWeek} enum to a {@link FirstDayOfWeekDto}.
     * 
     * @param day the first day of week enum to convert, must not be null
     * @return the first day of week DTO with code and display name
     * @throws IllegalArgumentException if day is null
     */
    public static FirstDayOfWeekDto toFirstDayOfWeekDto(SupportedFirstDayOfWeek day) {
        requireNonNull(day, "day");
        return FirstDayOfWeekDto.builder()
                .code(day.name())
                .name(day.getDisplayName())
                .build();
    }

    /**
     * Converts a {@link SupportedMealCategory} enum to a {@link MealCategoryDto}.
     * 
     * @param category the meal category enum to convert, must not be null
     * @return the meal category DTO with code and display name
     * @throws IllegalArgumentException if category is null
     */
    public static MealCategoryDto toMealCategoryDto(SupportedMealCategory category) {
        requireNonNull(category, "category");
        return MealCategoryDto.builder()
                .code(category.name())
                .name(category.getDisplayName())
                .build();
    }
}
