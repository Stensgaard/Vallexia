package com.vallexia.recipe.unit.service;

import com.vallexia.recipe.dto.UnitConversionRequestDto;
import com.vallexia.recipe.dto.UnitTypeCheckResponseDto;
import com.vallexia.recipe.service.UnitConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for UnitConversionService.
 * Tests business logic for unit conversions, display units, and unit type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UnitConversionService Unit Tests")
class UnitConversionServiceTest {
  
  @InjectMocks
  private UnitConversionService unitConversionService;
  
  // ==================== convert() Tests ====================
  
  @Test
  @DisplayName("Should convert weight units successfully")
  void shouldConvertWeightUnitsSuccessfully() {
    // Given
    UnitConversionRequestDto request = new UnitConversionRequestDto();
    request.setValue(new BigDecimal("100"));
    request.setFromUnit("g");
    request.setToUnit("oz");
    
    // When
    BigDecimal result = unitConversionService.convert(request);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result).isGreaterThan(BigDecimal.ZERO);
    // 100g ≈ 3.5274 oz
    assertThat(result).isCloseTo(new BigDecimal("3.5274"), org.assertj.core.data.Offset.offset(new BigDecimal("0.01")));
  }
  
  @Test
  @DisplayName("Should convert volume units successfully")
  void shouldConvertVolumeUnitsSuccessfully() {
    // Given
    UnitConversionRequestDto request = new UnitConversionRequestDto();
    request.setValue(new BigDecimal("1"));
    request.setFromUnit("cup");
    request.setToUnit("ml");
    
    // When
    BigDecimal result = unitConversionService.convert(request);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result).isGreaterThan(BigDecimal.ZERO);
    // 1 cup ≈ 236.588 ml
    assertThat(result).isCloseTo(new BigDecimal("236.588"), org.assertj.core.data.Offset.offset(new BigDecimal("0.1")));
  }
  
  @Test
  @DisplayName("Should return same value when converting to same unit")
  void shouldReturnSameValueWhenConvertingToSameUnit() {
    // Given
    UnitConversionRequestDto request = new UnitConversionRequestDto();
    request.setValue(new BigDecimal("100"));
    request.setFromUnit("g");
    request.setToUnit("g");
    
    // When
    BigDecimal result = unitConversionService.convert(request);
    
    // Then
    assertThat(result).isEqualByComparingTo(new BigDecimal("100"));
  }
  
  @Test
  @DisplayName("Should throw IllegalArgumentException for mismatched unit types")
  void shouldThrowIllegalArgumentExceptionForMismatchedUnitTypes() {
    // Given - mixing weight and volume units
    UnitConversionRequestDto request = new UnitConversionRequestDto();
    request.setValue(new BigDecimal("100"));
    request.setFromUnit("g");
    request.setToUnit("ml");
    
    // When & Then
    assertThatThrownBy(() -> unitConversionService.convert(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid unit combination")
        .hasMessageContaining("Units must be both weight or both volume");
  }
  
  @Test
  @DisplayName("Should throw IllegalArgumentException for invalid weight unit")
  void shouldThrowIllegalArgumentExceptionForInvalidWeightUnit() {
    // Given
    UnitConversionRequestDto request = new UnitConversionRequestDto();
    request.setValue(new BigDecimal("100"));
    request.setFromUnit("invalid");
    request.setToUnit("g");
    
    // When & Then
    assertThatThrownBy(() -> unitConversionService.convert(request))
        .isInstanceOf(IllegalArgumentException.class);
  }
  
  @Test
  @DisplayName("Should throw IllegalArgumentException for invalid volume unit")
  void shouldThrowIllegalArgumentExceptionForInvalidVolumeUnit() {
    // Given
    UnitConversionRequestDto request = new UnitConversionRequestDto();
    request.setValue(new BigDecimal("100"));
    request.setFromUnit("invalid");
    request.setToUnit("ml");
    
    // When & Then
    assertThatThrownBy(() -> unitConversionService.convert(request))
        .isInstanceOf(IllegalArgumentException.class);
  }
  
  // ==================== getDisplayUnit() Tests ====================
  
  @Test
  @DisplayName("Should return display unit for weight unit with metric system")
  void shouldReturnDisplayUnitForWeightUnitWithMetricSystem() {
    // When
    String result = unitConversionService.getDisplayUnit("oz", "METRIC");
    
    // Then
    assertThat(result).isEqualTo("g");
  }
  
  @Test
  @DisplayName("Should return display unit for weight unit with imperial system")
  void shouldReturnDisplayUnitForWeightUnitWithImperialSystem() {
    // When
    String result = unitConversionService.getDisplayUnit("g", "IMPERIAL");
    
    // Then
    assertThat(result).isEqualTo("oz");
  }
  
  @Test
  @DisplayName("Should return original unit for volume unit")
  void shouldReturnOriginalUnitForVolumeUnit() {
    // When
    String result = unitConversionService.getDisplayUnit("ml", "IMPERIAL");
    
    // Then
    assertThat(result).isEqualTo("ml");
  }
  
  // ==================== checkUnitType() Tests ====================
  
  @Test
  @DisplayName("Should correctly identify weight unit")
  void shouldCorrectlyIdentifyWeightUnit() {
    // When
    UnitTypeCheckResponseDto result = unitConversionService.checkUnitType("g");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.isWeightUnit()).isTrue();
    assertThat(result.isVolumeUnit()).isFalse();
    assertThat(result.isCountUnit()).isFalse();
  }
  
  @Test
  @DisplayName("Should correctly identify volume unit")
  void shouldCorrectlyIdentifyVolumeUnit() {
    // When
    UnitTypeCheckResponseDto result = unitConversionService.checkUnitType("ml");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.isWeightUnit()).isFalse();
    assertThat(result.isVolumeUnit()).isTrue();
    assertThat(result.isCountUnit()).isFalse();
  }
  
  @Test
  @DisplayName("Should correctly identify count unit")
  void shouldCorrectlyIdentifyCountUnit() {
    // When
    UnitTypeCheckResponseDto result = unitConversionService.checkUnitType("piece");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.isWeightUnit()).isFalse();
    assertThat(result.isVolumeUnit()).isFalse();
    assertThat(result.isCountUnit()).isTrue();
  }
  
  @Test
  @DisplayName("Should return all false for unknown unit")
  void shouldReturnAllFalseForUnknownUnit() {
    // When
    UnitTypeCheckResponseDto result = unitConversionService.checkUnitType("unknown");
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.isWeightUnit()).isFalse();
    assertThat(result.isVolumeUnit()).isFalse();
    assertThat(result.isCountUnit()).isFalse();
  }
}
