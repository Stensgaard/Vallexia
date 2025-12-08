package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Supported date formats for user preferences.
 *
 * <p>To introduce a new format:
 * <ol>
 *   <li>Add an enum constant with the display pattern and {@link DateFormatToken} sequence</li>
 *   <li>Reference the enum from any country or user-preference defaults</li>
 *   <li>The frontend receives the new format automatically through the locale config endpoint</li>
 * </ol>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedDateFormat {
    MM_DD_YYYY("MM/DD/YYYY", DateFormatToken.sequence(
            DateFormatToken.month(),
            DateFormatToken.literal("/"),
            DateFormatToken.day(),
            DateFormatToken.literal("/"),
            DateFormatToken.year()
    )),
    DD_MM_YYYY("DD/MM/YYYY", DateFormatToken.sequence(
            DateFormatToken.day(),
            DateFormatToken.literal("/"),
            DateFormatToken.month(),
            DateFormatToken.literal("/"),
            DateFormatToken.year()
    )),
    YYYY_MM_DD("YYYY-MM-DD", DateFormatToken.sequence(
            DateFormatToken.year(),
            DateFormatToken.literal("-"),
            DateFormatToken.month(),
            DateFormatToken.literal("-"),
            DateFormatToken.day()
    )),
    DD_MM_YYYY_DOT("DD.MM.YYYY", DateFormatToken.sequence(
            DateFormatToken.day(),
            DateFormatToken.literal("."),
            DateFormatToken.month(),
            DateFormatToken.literal("."),
            DateFormatToken.year()
    ));

    private final String format;
    private final List<DateFormatToken> tokens;
    private static final Map<String, SupportedDateFormat> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    dateFormat -> dateFormat.name().toUpperCase(Locale.ROOT),
                    dateFormat -> dateFormat));

    SupportedDateFormat(String format, List<DateFormatToken> tokens) {
        this.format = format;
        this.tokens = tokens;
    }

    /**
     * Get all supported date formats.
     * 
     * @return List of all supported date formats
     */
    public static List<SupportedDateFormat> getAll() {
        return Arrays.asList(values());
    }

    /**
     * Get a supported date format by code.
     * 
     * @param code the code to get
     * @return Optional containing the supported date format, or empty if not found
     */
    public static Optional<SupportedDateFormat> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }

    /**
     * Check if a code is valid.
     * 
     * @param code the code to check
     * @return True if the code is valid, false otherwise
     */
    public static boolean isValidCode(String code) {
        return fromCode(code).isPresent();
    }

    @Getter
    public static class DateFormatToken {
        private final Type type;
        private final String value;

        private DateFormatToken(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        public static DateFormatToken day() {
            return new DateFormatToken(Type.DAY, null);
        }

        public static DateFormatToken month() {
            return new DateFormatToken(Type.MONTH, null);
        }

        public static DateFormatToken year() {
            return new DateFormatToken(Type.YEAR, null);
        }

        public static DateFormatToken literal(String value) {
            return new DateFormatToken(Type.LITERAL, value);
        }

        public static List<DateFormatToken> sequence(DateFormatToken... tokens) {
            return Arrays.asList(tokens);
        }

        public enum Type {
            DAY,
            MONTH,
            YEAR,
            LITERAL
        }
    }

}
