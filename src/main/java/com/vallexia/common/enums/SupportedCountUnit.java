package com.vallexia.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Supported count units (units without conversion).
 *
 * <p>To add a new count unit:
 * <ol>
 *   <li>Add the enum constant with its display label</li>
 *   <li>Expose any necessary translation strings in the frontend</li>
 *   <li>The enums will automatically flow through {@link com.vallexia.common.controller.LocaleConfigController}</li>
 * </ol>
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
