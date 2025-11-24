package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Supported count units (units without conversion).
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Getter
public enum SupportedCountUnit {
    PIECE("piece"),
    ITEM("item"),
    WHOLE("whole");

    private final String display;

    SupportedCountUnit(String display) {
        this.display = display;
    }

    public static List<SupportedCountUnit> getAll() {
        return Arrays.asList(values());
    }
}
