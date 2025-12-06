package com.vallexia.common.mapper;

import com.vallexia.common.dto.UnitDto;
import com.vallexia.common.enums.SupportedCountUnit;
import com.vallexia.common.enums.SupportedVolumeUnit;
import com.vallexia.common.enums.SupportedWeightUnit;

/**
 * Mapper utility class for converting measurement unit enums to DTOs.
 * 
 * <p>This mapper provides static methods to convert unit enum types to their
 * corresponding DTOs. All methods perform null validation on input parameters
 * and will throw {@link IllegalArgumentException} if null values are provided.
 * 
 * <p>This mapper is used by {@link com.vallexia.common.controller.LocaleConfigController}
 * to build locale configuration responses for the frontend.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class UnitMapper {

    private UnitMapper() {}

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
     * Converts a {@link SupportedWeightUnit} enum to a {@link UnitDto}.
     * 
     * @param unit the weight unit enum to convert, must not be null
     * @return the unit DTO with code, display name, and conversion factor (grams)
     * @throws IllegalArgumentException if unit is null
     */
    public static UnitDto toWeightUnitDto(SupportedWeightUnit unit) {
        requireNonNull(unit, "unit");
        return UnitDto.builder()
                .code(unit.name())
                .display(unit.getDisplay())
                .conversion(unit.getGrams())
                .build();
    }

    /**
     * Converts a {@link SupportedVolumeUnit} enum to a {@link UnitDto}.
     * 
     * @param unit the volume unit enum to convert, must not be null
     * @return the unit DTO with code, display name, and conversion factor (milliliters)
     * @throws IllegalArgumentException if unit is null
     */
    public static UnitDto toVolumeUnitDto(SupportedVolumeUnit unit) {
        requireNonNull(unit, "unit");
        return UnitDto.builder()
                .code(unit.name())
                .display(unit.getDisplay())
                .conversion(unit.getMilliliters())
                .build();
    }

    /**
     * Converts a {@link SupportedCountUnit} enum to a {@link UnitDto}.
     * 
     * <p>Count units do not have a conversion factor as they represent discrete
     * quantities (e.g., "piece", "item"). The conversion field is set to null.
     * 
     * @param unit the count unit enum to convert, must not be null
     * @return the unit DTO with code and display name, conversion is null
     * @throws IllegalArgumentException if unit is null
     */
    public static UnitDto toCountUnitDto(SupportedCountUnit unit) {
        requireNonNull(unit, "unit");
        return UnitDto.builder()
                .code(unit.name())
                .display(unit.getDisplay())
                .conversion(null)
                .build();
    }
}
