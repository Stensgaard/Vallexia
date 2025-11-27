package com.vallexia.nutrition.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Shared constants for nutritional calculations across the application.
 * 
 * <p>This class provides standardized values for:
 * <ul>
 *   <li>Decimal precision and rounding mode for calculations</li>
 *   <li>Caloric values per gram for macronutrients (Atwater system)</li>
 * </ul>
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-27-11
 */
public final class NutritionalConstants {
    
    /**
     * Decimal scale for all nutritional calculations (2 decimal places).
     */
    public static final int DECIMAL_SCALE = 2;
    
    /**
     * Rounding mode for all nutritional calculations (half up).
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Calories per gram of protein (Atwater system).
     */
    public static final BigDecimal PROTEIN_CALORIES_PER_GRAM = BigDecimal.valueOf(4);
    
    /**
     * Calories per gram of carbohydrates (Atwater system).
     */
    public static final BigDecimal CARB_CALORIES_PER_GRAM = BigDecimal.valueOf(4);
    
    /**
     * Calories per gram of fats (Atwater system).
     */
    public static final BigDecimal FAT_CALORIES_PER_GRAM = BigDecimal.valueOf(9);
    
    /**
     * Private constructor to prevent instantiation.
     */
    private NutritionalConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
