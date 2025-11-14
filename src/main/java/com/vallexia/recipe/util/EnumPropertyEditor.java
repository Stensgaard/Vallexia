package com.vallexia.recipe.util;

import java.beans.PropertyEditorSupport;

/**
 * Custom property editor for enum types that converts empty strings to null.
 * This allows Spring to properly handle "All" options in dropdowns by treating
 * empty strings as "no filter" rather than attempting invalid enum conversion.
 * 
 * @param <T> the enum type
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class EnumPropertyEditor<T extends Enum<T>> extends PropertyEditorSupport {
    private final Class<T> enumClass;
    
    /**
     * Constructor for enum property editor.
     * 
     * @param enumClass the enum class type
     */
    public EnumPropertyEditor(Class<T> enumClass) {
        this.enumClass = enumClass;
    }
    
    /**
     * Converts text to enum value, handling empty strings and invalid values.
     * 
     * @param text the text to convert
     */
    @Override
    public void setAsText(String text) {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
        } else {
            try {
                setValue(Enum.valueOf(enumClass, text.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid enum value - set to null to treat as "no filter"
                setValue(null);
            }
        }
    }
}
