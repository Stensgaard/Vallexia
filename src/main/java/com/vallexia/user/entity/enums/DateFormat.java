package com.vallexia.user.entity.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Date format options for user display preferences.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public enum DateFormat {
    MM_DD_YYYY("MM/DD/YYYY"),
    DD_MM_YYYY("DD/MM/YYYY"),
    YYYY_MM_DD("YYYY-MM-DD"),
    DD_MM_YYYY_DOT("DD.MM.YYYY");
    
    private final String format;
    
    DateFormat(String format) {
        this.format = format;
    }
    
    /**
     * Check if a format string is a valid date format.
     * 
     * @param format the format string to check
     * @return true if the format is valid, false otherwise
     */
    public static boolean isValidFormat(String format) {
        if (format == null || format.isEmpty()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(df -> df.getFormat().equals(format));
    }
    
    /**
     * Get the DateFormat enum value from a format string.
     * 
     * @param format the format string
     * @return the DateFormat enum value, or null if not found
     */
    public static DateFormat fromFormat(String format) {
        if (format == null || format.isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(df -> df.getFormat().equals(format))
                .findFirst()
                .orElse(null);
    }
}
