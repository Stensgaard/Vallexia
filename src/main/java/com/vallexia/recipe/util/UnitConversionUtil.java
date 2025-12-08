package com.vallexia.recipe.util;

import com.vallexia.common.enums.SupportedCountUnit;
import com.vallexia.common.enums.SupportedVolumeUnit;
import com.vallexia.common.enums.SupportedWeightUnit;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for converting between measurement units (metric and imperial).
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-15
 */
@Slf4j
public class UnitConversionUtil {
    
    private UnitConversionUtil() {
        // Utility class - prevent instantiation
    }
    
    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    // Mapping for metric to imperial weight unit conversions
    private static final Map<SupportedWeightUnit, SupportedWeightUnit> METRIC_TO_IMPERIAL = Map.of(
        SupportedWeightUnit.GRAM, SupportedWeightUnit.OUNCE,
        SupportedWeightUnit.MILLIGRAM, SupportedWeightUnit.OUNCE,
        SupportedWeightUnit.KILOGRAM, SupportedWeightUnit.POUND
    );
    
    // Mapping for imperial to metric weight unit conversions
    private static final Map<SupportedWeightUnit, SupportedWeightUnit> IMPERIAL_TO_METRIC = Map.of(
        SupportedWeightUnit.OUNCE, SupportedWeightUnit.GRAM,
        SupportedWeightUnit.POUND, SupportedWeightUnit.KILOGRAM
    );
    
    /**
     * Convert weight value from one unit to another.
     * 
     * @param value the value to convert
     * @param fromUnit the source unit
     * @param toUnit the target unit
     * @return converted value
     * @throws IllegalArgumentException if units are not weight units or conversion is not supported
     */
    public static BigDecimal convertWeight(BigDecimal value, String fromUnit, String toUnit) {
        if (value == null) {
            return null;
        }
        
        if (fromUnit == null || toUnit == null) {
            return value;
        }
        
        String fromUnitLower = fromUnit.toLowerCase();
        String toUnitLower = toUnit.toLowerCase();
        
        // If units are the same, return as-is
        if (fromUnitLower.equals(toUnitLower)) {
            return value;
        }
        
        // Convert to grams first (intermediate unit)
        BigDecimal valueInGrams = convertToGrams(value, fromUnit);
        
        // Convert from grams to target unit
        return convertFromGrams(valueInGrams, toUnit);
    }
    
    /**
     * Convert any unit to grams (metric base unit).
     * 
     * @param value the value to convert
     * @param unit the source unit
     * @return value in grams
     * @throws IllegalArgumentException if the unit is not a supported weight unit
     */
    public static BigDecimal convertToGrams(BigDecimal value, String unit) {
        if (value == null || unit == null) {
            return value;
        }
        
        Optional<SupportedWeightUnit> unitOpt = SupportedWeightUnit.fromDisplay(unit);
        if (unitOpt.isPresent()) {
            return value.multiply(unitOpt.get().getGrams());
        }
        
        // Unknown unit - log warning and throw exception
        log.warn("Unknown weight unit '{}' provided to convertToGrams(). Supported weight units: g, kg, mg, oz, lb", unit);
        throw new IllegalArgumentException("Unsupported weight unit: '" + unit + "'. Supported units: g, kg, mg, oz, lb");
    }
    
    /**
     * Convert grams to target unit.
     * 
     * @param valueInGrams value in grams
     * @param toUnit target unit
     * @return converted value
     * @throws IllegalArgumentException if the unit is not a supported weight unit
     */
    private static BigDecimal convertFromGrams(BigDecimal valueInGrams, String toUnit) {
        if (valueInGrams == null || toUnit == null) {
            return valueInGrams;
        }
        
        Optional<SupportedWeightUnit> unitOpt = SupportedWeightUnit.fromDisplay(toUnit);
        if (unitOpt.isPresent()) {
            SupportedWeightUnit unit = unitOpt.get();
            return valueInGrams.divide(unit.getGrams(), DECIMAL_SCALE, ROUNDING_MODE);
        }
        
        // Unknown unit - log warning and throw exception
        log.warn("Unknown weight unit '{}' provided to convertFromGrams(). Supported weight units: g, kg, mg, oz, lb", toUnit);
        throw new IllegalArgumentException("Unsupported weight unit: '" + toUnit + "'. Supported units: g, kg, mg, oz, lb");
    }
    
    /**
     * Convert volume value from one unit to another.
     * 
     * @param value the value to convert
     * @param fromUnit the source unit
     * @param toUnit the target unit
     * @return converted value
     * @throws IllegalArgumentException if units are not volume units or conversion is not supported
     */
    public static BigDecimal convertVolume(BigDecimal value, String fromUnit, String toUnit) {
        if (value == null) {
            return null;
        }
        
        if (fromUnit == null || toUnit == null) {
            return value;
        }
        
        String fromUnitLower = fromUnit.toLowerCase();
        String toUnitLower = toUnit.toLowerCase();
        
        // If units are the same, return as-is
        if (fromUnitLower.equals(toUnitLower)) {
            return value;
        }
        
        // Convert to milliliters first (intermediate unit)
        BigDecimal valueInMilliliters = convertToMilliliters(value, fromUnit);
        
        // Convert from milliliters to target unit
        return convertFromMilliliters(valueInMilliliters, toUnit);
    }
    
    /**
     * Convert any unit to milliliters (base volume unit).
     * 
     * @param value the value to convert
     * @param unit the source unit
     * @return value in milliliters
     * @throws IllegalArgumentException if the unit is not a supported volume unit
     */
    public static BigDecimal convertToMilliliters(BigDecimal value, String unit) {
        if (value == null || unit == null) {
            return value;
        }
        
        Optional<SupportedVolumeUnit> unitOpt = SupportedVolumeUnit.fromDisplay(unit);
        if (unitOpt.isPresent()) {
            return value.multiply(unitOpt.get().getMilliliters());
        }
        
        // Unknown unit - log warning and throw exception
        log.warn("Unknown volume unit '{}' provided to convertToMilliliters(). Supported volume units: ml, l, cup, tbsp, tsp, fl oz", unit);
        throw new IllegalArgumentException("Unsupported volume unit: '" + unit + "'. Supported units: ml, l, cup, tbsp, tsp, fl oz");
    }
    
    /**
     * Convert milliliters to target unit.
     * 
     * @param valueInMilliliters value in milliliters
     * @param toUnit target unit
     * @return converted value
     * @throws IllegalArgumentException if the unit is not a supported volume unit
     */
    private static BigDecimal convertFromMilliliters(BigDecimal valueInMilliliters, String toUnit) {
        if (valueInMilliliters == null || toUnit == null) {
            return valueInMilliliters;
        }
        
        Optional<SupportedVolumeUnit> unitOpt = SupportedVolumeUnit.fromDisplay(toUnit);
        if (unitOpt.isPresent()) {
            SupportedVolumeUnit unit = unitOpt.get();
            return valueInMilliliters.divide(unit.getMilliliters(), DECIMAL_SCALE, ROUNDING_MODE);
        }
        
        // Unknown unit - log warning and throw exception
        log.warn("Unknown volume unit '{}' provided to convertFromMilliliters(). Supported volume units: ml, l, cup, tbsp, tsp, fl oz", toUnit);
        throw new IllegalArgumentException("Unsupported volume unit: '" + toUnit + "'. Supported units: ml, l, cup, tbsp, tsp, fl oz");
    }
    
    /**
     * Get appropriate display unit based on measurement system.
     * For weight units, returns metric or imperial equivalent.
     * For volume and count units, returns original unit (no conversion).
     * 
     * @param unit the original unit
     * @param measurementSystem the measurement system ("METRIC" or "IMPERIAL")
     * @return display unit
     */
    public static String getDisplayUnit(String unit, String measurementSystem) {
        if (unit == null || measurementSystem == null) {
            return unit;
        }
        
        boolean isImperial = "IMPERIAL".equalsIgnoreCase(measurementSystem);
        
        // Check if it's a weight unit
        Optional<SupportedWeightUnit> weightUnitOpt = SupportedWeightUnit.fromDisplay(unit);
        if (weightUnitOpt.isPresent()) {
            SupportedWeightUnit weightUnit = weightUnitOpt.get();
            
            // Convert metric to imperial if needed
            if (weightUnit.isMetric() && isImperial) {
                SupportedWeightUnit imperialUnit = METRIC_TO_IMPERIAL.get(weightUnit);
                if (imperialUnit != null) {
                    return imperialUnit.getDisplay();
                }
            }
            
            // Convert imperial to metric if needed
            if (weightUnit.isImperial() && !isImperial) {
                SupportedWeightUnit metricUnit = IMPERIAL_TO_METRIC.get(weightUnit);
                if (metricUnit != null) {
                    return metricUnit.getDisplay();
                }
            }
            
            // Keep as-is for same system
            return weightUnit.getDisplay();
        }
        
        // Volume and count units - keep as-is (universal)
        return unit;
    }
    
    /**
     * Check if a unit is a weight unit.
     * 
     * @param unit the unit to check
     * @return true if weight unit, false otherwise
     */
    public static boolean isWeightUnit(String unit) {
        if (unit == null) {
            return false;
        }
        
        return SupportedWeightUnit.fromDisplay(unit).isPresent();
    }
    
    /**
     * Check if a unit is a volume unit.
     * 
     * @param unit the unit to check
     * @return true if volume unit, false otherwise
     */
    public static boolean isVolumeUnit(String unit) {
        if (unit == null) {
            return false;
        }

        return SupportedVolumeUnit.fromDisplay(unit).isPresent();
    }
    
    /**
     * Check if a unit is a count unit.
     * 
     * @param unit the unit to check
     * @return true if count unit, false otherwise
     */
    public static boolean isCountUnit(String unit) {
        if (unit == null) {
            return false;
        }

        return SupportedCountUnit.fromDisplay(unit).isPresent();
    }
}
