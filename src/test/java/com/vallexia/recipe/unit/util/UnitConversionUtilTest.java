package com.vallexia.recipe.unit.util;

import com.vallexia.recipe.util.UnitConversionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

/**
 * Unit tests for UnitConversionUtil.
 * Tests conversion between different units and unit type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@DisplayName("UnitConversionUtil Unit Tests")
class UnitConversionUtilTest {

    // Weight Conversion Tests

    @Test
    @DisplayName("convertToGrams should convert kilograms to grams")
    void convertToGrams_shouldConvertKilograms() {
        BigDecimal result = UnitConversionUtil.convertToGrams(BigDecimal.valueOf(2), "kg");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("convertToGrams should convert ounces to grams")
    void convertToGrams_shouldConvertOunces() {
        BigDecimal result = UnitConversionUtil.convertToGrams(BigDecimal.valueOf(1), "oz");
        assertThat(result).isEqualByComparingTo(new BigDecimal("28.3495"));
    }

    @Test
    @DisplayName("convertToGrams should convert pounds to grams")
    void convertToGrams_shouldConvertPounds() {
        BigDecimal result = UnitConversionUtil.convertToGrams(BigDecimal.valueOf(1), "lb");
        assertThat(result).isEqualByComparingTo(new BigDecimal("453.592"));
    }

    @Test
    @DisplayName("convertToGrams should handle grams as-is")
    void convertToGrams_shouldHandleGrams() {
        BigDecimal result = UnitConversionUtil.convertToGrams(BigDecimal.valueOf(100), "g");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("convertToGrams should handle plurals and variations")
    void convertToGrams_shouldHandleVariations() {
        assertThat(UnitConversionUtil.convertToGrams(BigDecimal.valueOf(1), "kilogram"))
                .isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(UnitConversionUtil.convertToGrams(BigDecimal.valueOf(1), "pounds"))
                .isEqualByComparingTo(new BigDecimal("453.592"));
        assertThat(UnitConversionUtil.convertToGrams(BigDecimal.valueOf(1), "lbs"))
                .isEqualByComparingTo(new BigDecimal("453.592"));
    }

    @Test
    @DisplayName("convertToGrams should throw IllegalArgumentException for unknown units")
    void convertToGrams_shouldThrowExceptionForUnknownUnits() {
        assertThatThrownBy(() -> UnitConversionUtil.convertToGrams(BigDecimal.valueOf(100), "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported weight unit");
    }
    
    @Test
    @DisplayName("convertToGrams should throw IllegalArgumentException for empty string")
    void convertToGrams_shouldThrowExceptionForEmptyString() {
        assertThatThrownBy(() -> UnitConversionUtil.convertToGrams(BigDecimal.valueOf(100), ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported weight unit");
    }

    @Test
    @DisplayName("convertToGrams should handle null values")
    void convertToGrams_shouldHandleNullValues() {
        assertThat(UnitConversionUtil.convertToGrams(null, "kg")).isNull();
        assertThat(UnitConversionUtil.convertToGrams(BigDecimal.valueOf(100), null))
                .isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("convertWeight should convert between different weight units")
    void convertWeight_shouldConvertBetweenUnits() {
        // kg to oz
        BigDecimal result = UnitConversionUtil.convertWeight(BigDecimal.valueOf(1), "kg", "oz");
        assertThat(result).isNotNull();
        
        // oz to lb
        BigDecimal result2 = UnitConversionUtil.convertWeight(BigDecimal.valueOf(16), "oz", "lb");
        assertThat(result2).isNotNull();
    }

    @Test
    @DisplayName("convertWeight should return same value for identical units")
    void convertWeight_shouldReturnSameForIdenticalUnits() {
        BigDecimal result = UnitConversionUtil.convertWeight(BigDecimal.valueOf(100), "g", "g");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("convertWeight should handle null values")
    void convertWeight_shouldHandleNullValues() {
        assertThat(UnitConversionUtil.convertWeight(null, "kg", "g")).isNull();
    }

    // Volume Conversion Tests

    @Test
    @DisplayName("convertToMilliliters should convert liters to milliliters")
    void convertToMilliliters_shouldConvertLiters() {
        BigDecimal result = UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(2), "l");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("convertToMilliliters should convert cups to milliliters")
    void convertToMilliliters_shouldConvertCups() {
        BigDecimal result = UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(1), "cup");
        assertThat(result).isEqualByComparingTo(new BigDecimal("236.588"));
    }

    @Test
    @DisplayName("convertToMilliliters should convert tablespoons to milliliters")
    void convertToMilliliters_shouldConvertTablespoons() {
        BigDecimal result = UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(1), "tbsp");
        assertThat(result).isEqualByComparingTo(new BigDecimal("14.7868"));
    }

    @Test
    @DisplayName("convertToMilliliters should handle milliliters as-is")
    void convertToMilliliters_shouldHandleMilliliters() {
        BigDecimal result = UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(100), "ml");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("convertToMilliliters should handle plurals and variations")
    void convertToMilliliters_shouldHandleVariations() {
        assertThat(UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(1), "cups"))
                .isEqualByComparingTo(new BigDecimal("236.588"));
        assertThat(UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(1), "tablespoons"))
                .isEqualByComparingTo(new BigDecimal("14.7868"));
        assertThat(UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(1), "fluid ounces"))
                .isEqualByComparingTo(new BigDecimal("29.5735"));
    }

    @Test
    @DisplayName("convertVolume should convert between different volume units")
    void convertVolume_shouldConvertBetweenUnits() {
        // cups to ml
        BigDecimal result = UnitConversionUtil.convertVolume(BigDecimal.valueOf(1), "cup", "ml");
        assertThat(result).isEqualByComparingTo(new BigDecimal("236.588"));
        
        // tbsp to tsp (14.7868ml / 4.92892ml ≈ 3.0)
        BigDecimal result2 = UnitConversionUtil.convertVolume(BigDecimal.valueOf(1), "tbsp", "tsp");
        assertThat(result2).isCloseTo(BigDecimal.valueOf(3), offset(BigDecimal.valueOf(0.01)));
    }

    @Test
    @DisplayName("convertVolume should return same value for identical units")
    void convertVolume_shouldReturnSameForIdenticalUnits() {
        BigDecimal result = UnitConversionUtil.convertVolume(BigDecimal.valueOf(100), "ml", "ml");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
    }
    
    @Test
    @DisplayName("convertToMilliliters should throw IllegalArgumentException for unknown units")
    void convertToMilliliters_shouldThrowExceptionForUnknownUnits() {
        assertThatThrownBy(() -> UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(100), "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported volume unit");
    }
    
    @Test
    @DisplayName("convertToMilliliters should throw IllegalArgumentException for empty string")
    void convertToMilliliters_shouldThrowExceptionForEmptyString() {
        assertThatThrownBy(() -> UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(100), ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported volume unit");
    }
    
    @Test
    @DisplayName("convertWeight should throw IllegalArgumentException for unknown units")
    void convertWeight_shouldThrowExceptionForUnknownUnits() {
        assertThatThrownBy(() -> UnitConversionUtil.convertWeight(BigDecimal.valueOf(100), "unknown", "g"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported weight unit");
        
        assertThatThrownBy(() -> UnitConversionUtil.convertWeight(BigDecimal.valueOf(100), "g", "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported weight unit");
    }
    
    @Test
    @DisplayName("convertVolume should throw IllegalArgumentException for unknown units")
    void convertVolume_shouldThrowExceptionForUnknownUnits() {
        assertThatThrownBy(() -> UnitConversionUtil.convertVolume(BigDecimal.valueOf(100), "unknown", "ml"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported volume unit");
        
        assertThatThrownBy(() -> UnitConversionUtil.convertVolume(BigDecimal.valueOf(100), "ml", "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported volume unit");
    }

    // Unit Type Checking Tests

    @Test
    @DisplayName("isWeightUnit should correctly identify weight units")
    void isWeightUnit_shouldIdentifyWeightUnits() {
        assertThat(UnitConversionUtil.isWeightUnit("g")).isTrue();
        assertThat(UnitConversionUtil.isWeightUnit("kg")).isTrue();
        assertThat(UnitConversionUtil.isWeightUnit("oz")).isTrue();
        assertThat(UnitConversionUtil.isWeightUnit("pound")).isTrue();
        assertThat(UnitConversionUtil.isWeightUnit("lbs")).isTrue();
        assertThat(UnitConversionUtil.isWeightUnit("cup")).isFalse();
        assertThat(UnitConversionUtil.isWeightUnit("ml")).isFalse();
        assertThat(UnitConversionUtil.isWeightUnit(null)).isFalse();
    }

    @Test
    @DisplayName("isVolumeUnit should correctly identify volume units")
    void isVolumeUnit_shouldIdentifyVolumeUnits() {
        assertThat(UnitConversionUtil.isVolumeUnit("ml")).isTrue();
        assertThat(UnitConversionUtil.isVolumeUnit("cup")).isTrue();
        assertThat(UnitConversionUtil.isVolumeUnit("tbsp")).isTrue();
        assertThat(UnitConversionUtil.isVolumeUnit("tablespoon")).isTrue();
        assertThat(UnitConversionUtil.isVolumeUnit("fl oz")).isTrue();
        assertThat(UnitConversionUtil.isVolumeUnit("g")).isFalse();
        assertThat(UnitConversionUtil.isVolumeUnit("kg")).isFalse();
        assertThat(UnitConversionUtil.isVolumeUnit(null)).isFalse();
    }

    @Test
    @DisplayName("isCountUnit should correctly identify count units")
    void isCountUnit_shouldIdentifyCountUnits() {
        assertThat(UnitConversionUtil.isCountUnit("piece")).isTrue();
        assertThat(UnitConversionUtil.isCountUnit("pieces")).isTrue();
        assertThat(UnitConversionUtil.isCountUnit("pcs")).isTrue();
        assertThat(UnitConversionUtil.isCountUnit("item")).isTrue();
        assertThat(UnitConversionUtil.isCountUnit("whole")).isTrue();
        assertThat(UnitConversionUtil.isCountUnit("g")).isFalse();
        assertThat(UnitConversionUtil.isCountUnit("cup")).isFalse();
        assertThat(UnitConversionUtil.isCountUnit(null)).isFalse();
    }

    // Display Unit Tests

    @Test
    @DisplayName("getDisplayUnit should convert metric to imperial for weight units")
    void getDisplayUnit_shouldConvertMetricToImperial() {
        String result = UnitConversionUtil.getDisplayUnit("g", "IMPERIAL");
        assertThat(result).isEqualTo("oz");
        
        String result2 = UnitConversionUtil.getDisplayUnit("kg", "IMPERIAL");
        assertThat(result2).isEqualTo("lb");
    }

    @Test
    @DisplayName("getDisplayUnit should convert imperial to metric for weight units")
    void getDisplayUnit_shouldConvertImperialToMetric() {
        String result = UnitConversionUtil.getDisplayUnit("oz", "METRIC");
        assertThat(result).isEqualTo("g");
        
        String result2 = UnitConversionUtil.getDisplayUnit("lb", "METRIC");
        assertThat(result2).isEqualTo("kg");
    }

    @Test
    @DisplayName("getDisplayUnit should keep same unit for matching system")
    void getDisplayUnit_shouldKeepSameForMatchingSystem() {
        String result = UnitConversionUtil.getDisplayUnit("g", "METRIC");
        assertThat(result).isEqualTo("g");
        
        String result2 = UnitConversionUtil.getDisplayUnit("oz", "IMPERIAL");
        assertThat(result2).isEqualTo("oz");
    }

    @Test
    @DisplayName("getDisplayUnit should keep volume and count units unchanged")
    void getDisplayUnit_shouldKeepVolumeAndCountUnchanged() {
        assertThat(UnitConversionUtil.getDisplayUnit("cup", "METRIC")).isEqualTo("cup");
        assertThat(UnitConversionUtil.getDisplayUnit("ml", "IMPERIAL")).isEqualTo("ml");
        assertThat(UnitConversionUtil.getDisplayUnit("piece", "METRIC")).isEqualTo("piece");
    }

    @Test
    @DisplayName("getDisplayUnit should handle null values")
    void getDisplayUnit_shouldHandleNullValues() {
        assertThat(UnitConversionUtil.getDisplayUnit(null, "METRIC")).isNull();
        assertThat(UnitConversionUtil.getDisplayUnit("g", null)).isEqualTo("g");
    }
    
    @Test
    @DisplayName("convertToGrams should handle negative values")
    void convertToGrams_shouldHandleNegativeValues() {
        BigDecimal result = UnitConversionUtil.convertToGrams(BigDecimal.valueOf(-1), "kg");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(-1000));
    }
    
    @Test
    @DisplayName("convertToMilliliters should handle negative values")
    void convertToMilliliters_shouldHandleNegativeValues() {
        BigDecimal result = UnitConversionUtil.convertToMilliliters(BigDecimal.valueOf(-1), "l");
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(-1000));
    }
}
