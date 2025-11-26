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

    SupportedDateFormat(String format, List<DateFormatToken> tokens) {
        this.format = format;
        this.tokens = tokens;
    }

    public static boolean isValidFormat(String format) {
        if (format == null || format.isEmpty()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(item -> item.getFormat().equals(format));
    }

    public static Optional<SupportedDateFormat> fromFormat(String format) {
        if (format == null || format.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(item -> item.getFormat().equals(format))
                .findFirst();
    }

    private static final Map<String, SupportedDateFormat> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    format -> format.name().toUpperCase(Locale.ROOT),
                    format -> format));

    public static Optional<SupportedDateFormat> fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(BY_CODE.get(normalized));
    }

    public static boolean isValidCode(String code) {
        return fromCode(code).isPresent();
    }

    public static List<SupportedDateFormat> getAll() {
        return Arrays.asList(values());
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
