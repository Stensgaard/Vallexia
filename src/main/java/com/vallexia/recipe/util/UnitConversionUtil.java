package com.vallexia.recipe.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for converting between measurement units (metric and imperial).
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class UnitConversionUtil {
    
    // Weight units
    private static final Set<String> METRIC_WEIGHT_UNITS = new HashSet<>(Arrays.asList(
        "g", "gram", "grams", "kg", "kilogram", "kilograms", "mg", "milligram", "milligrams"
    ));
    
    private static final Set<String> IMPERIAL_WEIGHT_UNITS = new HashSet<>(Arrays.asList(
        "oz", "ounce", "ounces", "lb", "pound", "pounds", "lbs"
    ));
    
    // Volume units (universal, no conversion needed)
    private static final Set<String> VOLUME_UNITS = new HashSet<>(Arrays.asList(
        "cup", "cups", "tbsp", "tablespoon", "tablespoons", "tsp", "teaspoon", "teaspoons",
        "ml", "milliliter", "milliliters", "l", "liter", "liters", "fl oz", "fluid ounce", "fluid ounces"
    ));
    
    // Count units (universal, no conversion needed)
    private static final Set<String> COUNT_UNITS = new HashSet<>(Arrays.asList(
        "piece", "pieces", "item", "items", "whole", "wholes", "pcs", "pc"
    ));
    
    // Conversion factors
    private static final BigDecimal OUNCES_TO_GRAMS = BigDecimal.valueOf(28.35);
    private static final BigDecimal POUNDS_TO_GRAMS = BigDecimal.valueOf(453.59);
    private static final BigDecimal KILOGRAMS_TO_GRAMS = BigDecimal.valueOf(1000.0);
    private static final BigDecimal MILLIGRAMS_TO_GRAMS = BigDecimal.valueOf(0.001);
    
    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
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
        
        String fromUnitLower = fromUnit != null ? fromUnit.toLowerCase() : "";
        String toUnitLower = toUnit != null ? toUnit.toLowerCase() : "";
        
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
     */
    public static BigDecimal convertToGrams(BigDecimal value, String unit) {
        if (value == null || unit == null) {
            return value;
        }
        
        String unitLower = unit.toLowerCase();
        
        // Already in grams
        if (unitLower.equals("g") || unitLower.equals("gram") || unitLower.equals("grams")) {
            return value;
        }
        
        // Metric units
        if (unitLower.equals("kg") || unitLower.equals("kilogram") || unitLower.equals("kilograms")) {
            return value.multiply(KILOGRAMS_TO_GRAMS);
        }
        if (unitLower.equals("mg") || unitLower.equals("milligram") || unitLower.equals("milligrams")) {
            return value.multiply(MILLIGRAMS_TO_GRAMS);
        }
        
        // Imperial units
        if (unitLower.equals("oz") || unitLower.equals("ounce") || unitLower.equals("ounces")) {
            return value.multiply(OUNCES_TO_GRAMS);
        }
        if (unitLower.equals("lb") || unitLower.equals("lbs") || unitLower.equals("pound") || unitLower.equals("pounds")) {
            return value.multiply(POUNDS_TO_GRAMS);
        }
        
        // Unknown unit, assume grams
        return value;
    }
    
    /**
     * Convert grams to target unit.
     * 
     * @param valueInGrams value in grams
     * @param toUnit target unit
     * @return converted value
     */
    private static BigDecimal convertFromGrams(BigDecimal valueInGrams, String toUnit) {
        if (valueInGrams == null || toUnit == null) {
            return valueInGrams;
        }
        
        String unitLower = toUnit.toLowerCase();
        
        // Metric units
        if (unitLower.equals("g") || unitLower.equals("gram") || unitLower.equals("grams")) {
            return valueInGrams;
        }
        if (unitLower.equals("kg") || unitLower.equals("kilogram") || unitLower.equals("kilograms")) {
            return valueInGrams.divide(KILOGRAMS_TO_GRAMS, DECIMAL_SCALE, ROUNDING_MODE);
        }
        if (unitLower.equals("mg") || unitLower.equals("milligram") || unitLower.equals("milligrams")) {
            return valueInGrams.divide(MILLIGRAMS_TO_GRAMS, DECIMAL_SCALE, ROUNDING_MODE);
        }
        
        // Imperial units
        if (unitLower.equals("oz") || unitLower.equals("ounce") || unitLower.equals("ounces")) {
            return valueInGrams.divide(OUNCES_TO_GRAMS, DECIMAL_SCALE, ROUNDING_MODE);
        }
        if (unitLower.equals("lb") || unitLower.equals("lbs") || unitLower.equals("pound") || unitLower.equals("pounds")) {
            return valueInGrams.divide(POUNDS_TO_GRAMS, DECIMAL_SCALE, ROUNDING_MODE);
        }
        
        // Unknown unit, return as grams
        return valueInGrams;
    }
    
    /**
     * Convert to metric unit (grams for weights).
     * 
     * @param value the value to convert
     * @param unit the source unit
     * @return value in metric units
     */
    public static BigDecimal convertToMetric(BigDecimal value, String unit) {
        return convertToGrams(value, unit);
    }
    
    /**
     * Convert to imperial unit (ounces for weights).
     * 
     * @param value the value to convert
     * @param unit the source unit
     * @return value in imperial units (ounces)
     */
    public static BigDecimal convertToImperial(BigDecimal value, String unit) {
        if (value == null || unit == null) {
            return value;
        }
        
        // Convert to grams first
        BigDecimal valueInGrams = convertToGrams(value, unit);
        
        // Convert to ounces
        return valueInGrams.divide(OUNCES_TO_GRAMS, DECIMAL_SCALE, ROUNDING_MODE);
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
        
        String unitLower = unit.toLowerCase();
        boolean isImperial = "IMPERIAL".equalsIgnoreCase(measurementSystem);
        
        // Weight units - convert based on system
        if (METRIC_WEIGHT_UNITS.contains(unitLower)) {
            if (isImperial) {
                // Convert metric to imperial
                if (unitLower.equals("g") || unitLower.equals("gram") || unitLower.equals("grams")) {
                    return "oz";
                }
                if (unitLower.equals("kg") || unitLower.equals("kilogram") || unitLower.equals("kilograms")) {
                    return "lb";
                }
                if (unitLower.equals("mg") || unitLower.equals("milligram") || unitLower.equals("milligrams")) {
                    return "oz"; // Convert mg to oz (very small, but still convert)
                }
            }
            // Metric system - keep as-is
            return unit;
        }
        
        if (IMPERIAL_WEIGHT_UNITS.contains(unitLower)) {
            if (!isImperial) {
                // Convert imperial to metric
                if (unitLower.equals("oz") || unitLower.equals("ounce") || unitLower.equals("ounces")) {
                    return "g";
                }
                if (unitLower.equals("lb") || unitLower.equals("lbs") || unitLower.equals("pound") || unitLower.equals("pounds")) {
                    return "kg";
                }
            }
            // Imperial system - keep as-is
            return unit;
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
        String unitLower = unit.toLowerCase();
        return METRIC_WEIGHT_UNITS.contains(unitLower) || IMPERIAL_WEIGHT_UNITS.contains(unitLower);
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
        return VOLUME_UNITS.contains(unit.toLowerCase());
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
        return COUNT_UNITS.contains(unit.toLowerCase());
    }
}
