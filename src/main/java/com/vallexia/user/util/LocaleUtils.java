package com.vallexia.user.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for deriving locale-specific formatting preferences from country codes.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class LocaleUtils {
    
    // Countries that use comma as decimal separator
    private static final Set<String> COMMA_DECIMAL_COUNTRIES = new HashSet<>(Arrays.asList(
        "DE", "FR", "IT", "ES", "PT", "NL", "BE", "AT", "CH", "SE", "NO", "DK", "FI",
        "PL", "CZ", "SK", "HU", "RO", "BG", "HR", "SI", "GR", "RU", "BR", "AR", "CL",
        "CO", "PE", "VE", "EC", "UY", "PY", "BO", "ZA"
    ));
    
    // Countries that use space as thousands separator (when decimal is comma)
    private static final Set<String> SPACE_THOUSANDS_COUNTRIES = new HashSet<>(Arrays.asList(
        "FR", "SE", "NO", "FI", "DK"
    ));
    
    // Eurozone countries
    private static final Set<String> EUROZONE_COUNTRIES = new HashSet<>(Arrays.asList(
        "AT", "BE", "CY", "EE", "FI", "FR", "DE", "GR", "IE", "IT", "LV", "LT",
        "LU", "MT", "NL", "PT", "SK", "SI", "ES"
    ));
    
    // Map of country codes to currency codes
    private static final Map<String, String> COUNTRY_TO_CURRENCY = Map.ofEntries(
        Map.entry("US", "USD"),
        Map.entry("GB", "GBP"),
        Map.entry("CA", "CAD"),
        Map.entry("AU", "AUD"),
        Map.entry("NZ", "NZD"),
        Map.entry("JP", "JPY"),
        Map.entry("CN", "CNY"),
        Map.entry("IN", "INR"),
        Map.entry("BR", "BRL"),
        Map.entry("MX", "MXN"),
        Map.entry("AR", "ARS"),
        Map.entry("CL", "CLP"),
        Map.entry("CO", "COP"),
        Map.entry("PE", "PEN"),
        Map.entry("VE", "VES"),
        Map.entry("EC", "USD"),
        Map.entry("UY", "UYU"),
        Map.entry("PY", "PYG"),
        Map.entry("BO", "BOB"),
        Map.entry("ZA", "ZAR"),
        Map.entry("KR", "KRW"),
        Map.entry("SG", "SGD"),
        Map.entry("MY", "MYR"),
        Map.entry("TH", "THB"),
        Map.entry("ID", "IDR"),
        Map.entry("PH", "PHP"),
        Map.entry("VN", "VND"),
        Map.entry("CH", "CHF"),
        Map.entry("NO", "NOK"),
        Map.entry("SE", "SEK"),
        Map.entry("DK", "DKK"),
        Map.entry("PL", "PLN"),
        Map.entry("CZ", "CZK"),
        Map.entry("HU", "HUF"),
        Map.entry("RO", "RON"),
        Map.entry("BG", "BGN"),
        Map.entry("HR", "HRK"),
        Map.entry("TR", "TRY"),
        Map.entry("RU", "RUB"),
        Map.entry("IL", "ILS"),
        Map.entry("AE", "AED"),
        Map.entry("SA", "SAR")
    );
    
    /**
     * Get decimal separator based on country code.
     * Most European countries use comma, others use period.
     * 
     * @param country country code (ISO 3166-1 alpha-2), can be null
     * @return decimal separator ('.' or ',')
     */
    public static String getDecimalSeparator(String country) {
        if (country == null || country.isEmpty()) {
            return ".";
        }
        return COMMA_DECIMAL_COUNTRIES.contains(country.toUpperCase()) ? "," : ".";
    }
    
    /**
     * Get thousands separator based on country code.
     * Usually opposite of decimal separator.
     * 
     * @param country country code (ISO 3166-1 alpha-2), can be null
     * @return thousands separator (',' or '.' or ' ')
     */
    public static String getThousandsSeparator(String country) {
        if (country == null || country.isEmpty()) {
            return ",";
        }
        
        String decimalSep = getDecimalSeparator(country);
        
        // If decimal is comma, thousands is usually period or space
        if (",".equals(decimalSep)) {
            return SPACE_THOUSANDS_COUNTRIES.contains(country.toUpperCase()) ? " " : ".";
        }
        
        // If decimal is period, thousands is usually comma
        return ",";
    }
    
    /**
     * Get currency code based on country code.
     * 
     * @param country country code (ISO 3166-1 alpha-2), can be null
     * @return currency code (ISO 4217) or null if not found
     */
    public static String getCurrencyFromCountry(String country) {
        if (country == null || country.isEmpty()) {
            return null;
        }
        
        String countryUpper = country.toUpperCase();
        
        // Check if country is in Eurozone
        if (EUROZONE_COUNTRIES.contains(countryUpper)) {
            return "EUR";
        }
        
        return COUNTRY_TO_CURRENCY.get(countryUpper);
    }
}
