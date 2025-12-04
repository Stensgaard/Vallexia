package com.vallexia.recipe.service;

import com.vallexia.recipe.dto.UnitConversionRequestDto;
import com.vallexia.recipe.dto.UnitTypeCheckResponseDto;
import com.vallexia.recipe.util.UnitConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for unit conversion operations.
 * Handles business logic for converting between measurement units,
 * determining display units, and checking unit types.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class UnitConversionService {
    
    /**
     * Convert a value from one unit to another.
     * Auto-detects conversion type (weight or volume) from the units.
     * 
     * @param request the conversion request containing value, fromUnit, and toUnit
     * @return converted value
     * @throws IllegalArgumentException if units are invalid or unit types don't match
     */
    public BigDecimal convert(UnitConversionRequestDto request) {
        log.debug("Converting {} {} to {}", request.getValue(), request.getFromUnit(), request.getToUnit());
        
        // Auto-detect conversion type from units
        if (UnitConversionUtil.isWeightUnit(request.getFromUnit()) && 
            UnitConversionUtil.isWeightUnit(request.getToUnit())) {
            return UnitConversionUtil.convertWeight(
                request.getValue(),
                request.getFromUnit(),
                request.getToUnit()
            );
        } else if (UnitConversionUtil.isVolumeUnit(request.getFromUnit()) && 
                   UnitConversionUtil.isVolumeUnit(request.getToUnit())) {
            return UnitConversionUtil.convertVolume(
                request.getValue(),
                request.getFromUnit(),
                request.getToUnit()
            );
        } else {
            log.warn("Invalid unit combination: {} to {}. Units must be both weight or both volume.", 
                request.getFromUnit(), request.getToUnit());
            throw new IllegalArgumentException(
                String.format("Invalid unit combination: '%s' to '%s'. Units must be both weight or both volume.", 
                    request.getFromUnit(), request.getToUnit()));
        }
    }
    
    /**
     * Get appropriate display unit based on measurement system.
     * For weight units, converts between metric and imperial.
     * For volume and count units, returns original unit.
     * 
     * @param unit the original unit
     * @param measurementSystem the measurement system ("METRIC" or "IMPERIAL")
     * @return display unit
     */
    public String getDisplayUnit(String unit, String measurementSystem) {
        log.debug("Getting display unit for {} with system {}", unit, measurementSystem);
        return UnitConversionUtil.getDisplayUnit(unit, measurementSystem);
    }
    
    /**
     * Check unit type (weight, volume, or count).
     * 
     * @param unit the unit to check
     * @return UnitTypeCheckResponseDto with boolean flags for each unit type
     */
    public UnitTypeCheckResponseDto checkUnitType(String unit) {
        log.debug("Checking unit type for {}", unit);
        
        return UnitTypeCheckResponseDto.builder()
            .isWeightUnit(UnitConversionUtil.isWeightUnit(unit))
            .isVolumeUnit(UnitConversionUtil.isVolumeUnit(unit))
            .isCountUnit(UnitConversionUtil.isCountUnit(unit))
            .build();
    }
}
