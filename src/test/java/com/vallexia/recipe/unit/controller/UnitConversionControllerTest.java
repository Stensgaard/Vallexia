package com.vallexia.recipe.unit.controller;

import com.vallexia.recipe.controller.UnitConversionController;
import com.vallexia.recipe.dto.*;
import com.vallexia.recipe.service.UnitConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UnitConversionController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UnitConversionController Unit Tests")
class UnitConversionControllerTest {
    
    @Mock
    private UnitConversionService unitConversionService;
    
    @InjectMocks
    private UnitConversionController unitConversionController;
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should convert weight units successfully")
    void shouldConvertWeightUnitsSuccessfully() {
        // Given
        UnitConversionRequestDto request = new UnitConversionRequestDto();
        request.setValue(new BigDecimal("100"));
        request.setFromUnit("g");
        request.setToUnit("oz");
        
        BigDecimal expectedValue = new BigDecimal("3.5274");
        when(unitConversionService.convert(any(UnitConversionRequestDto.class)))
            .thenReturn(expectedValue);
        
        // When
        ResponseEntity<UnitConversionResponseDto> response = 
            unitConversionController.convert(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UnitConversionResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getConvertedValue()).isEqualByComparingTo(expectedValue);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should convert volume units successfully")
    void shouldConvertVolumeUnitsSuccessfully() {
        // Given
        UnitConversionRequestDto request = new UnitConversionRequestDto();
        request.setValue(new BigDecimal("1"));
        request.setFromUnit("cup");
        request.setToUnit("ml");
        
        BigDecimal expectedValue = new BigDecimal("236.5882");
        when(unitConversionService.convert(any(UnitConversionRequestDto.class)))
            .thenReturn(expectedValue);
        
        // When
        ResponseEntity<UnitConversionResponseDto> response = 
            unitConversionController.convert(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UnitConversionResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getConvertedValue()).isEqualByComparingTo(expectedValue);
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid weight unit")
    void shouldThrowIllegalArgumentExceptionForInvalidWeightUnit() {
        // Given
        UnitConversionRequestDto request = new UnitConversionRequestDto();
        request.setValue(new BigDecimal("100"));
        request.setFromUnit("invalid");
        request.setToUnit("g");
        
        when(unitConversionService.convert(any(UnitConversionRequestDto.class)))
            .thenThrow(new IllegalArgumentException("Unsupported weight unit: 'invalid'"));
        
        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
            unitConversionController.convert(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported weight unit");
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid volume unit")
    void shouldThrowIllegalArgumentExceptionForInvalidVolumeUnit() {
        // Given
        UnitConversionRequestDto request = new UnitConversionRequestDto();
        request.setValue(new BigDecimal("100"));
        request.setFromUnit("invalid");
        request.setToUnit("ml");
        
        when(unitConversionService.convert(any(UnitConversionRequestDto.class)))
            .thenThrow(new IllegalArgumentException("Unsupported volume unit: 'invalid'"));
        
        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
            unitConversionController.convert(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported volume unit");
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException for mismatched unit types")
    void shouldThrowIllegalArgumentExceptionForMismatchedUnitTypes() {
        // Given - mixing weight and volume units
        UnitConversionRequestDto request = new UnitConversionRequestDto();
        request.setValue(new BigDecimal("100"));
        request.setFromUnit("g");
        request.setToUnit("ml");
        
        when(unitConversionService.convert(any(UnitConversionRequestDto.class)))
            .thenThrow(new IllegalArgumentException("Invalid unit combination: 'g' to 'ml'. Units must be both weight or both volume."));
        
        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> 
            unitConversionController.convert(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid unit combination");
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should return same value when converting to same unit")
    void shouldReturnSameValueWhenConvertingToSameUnit() {
        // Given
        UnitConversionRequestDto request = new UnitConversionRequestDto();
        request.setValue(new BigDecimal("100"));
        request.setFromUnit("g");
        request.setToUnit("g");
        
        BigDecimal expectedValue = new BigDecimal("100");
        when(unitConversionService.convert(any(UnitConversionRequestDto.class)))
            .thenReturn(expectedValue);
        
        // When
        ResponseEntity<UnitConversionResponseDto> response = 
            unitConversionController.convert(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UnitConversionResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getConvertedValue()).isEqualByComparingTo(expectedValue);
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should get display unit successfully")
    void shouldGetDisplayUnitSuccessfully() {
        // Given
        GetDisplayUnitRequestDto request = new GetDisplayUnitRequestDto();
        request.setUnit("g");
        request.setMeasurementSystem("IMPERIAL");
        
        when(unitConversionService.getDisplayUnit("g", "IMPERIAL"))
            .thenReturn("oz");
        
        // When
        ResponseEntity<GetDisplayUnitResponseDto> response = 
            unitConversionController.getDisplayUnit(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        GetDisplayUnitResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDisplayUnit()).isEqualTo("oz");
    }
    
    @SuppressWarnings("null")
    @Test
    @DisplayName("Should check unit type successfully")
    void shouldCheckUnitTypeSuccessfully() {
        // Given
        UnitTypeCheckRequestDto request = new UnitTypeCheckRequestDto();
        request.setUnit("g");
        
        UnitTypeCheckResponseDto expectedResponse = UnitTypeCheckResponseDto.builder()
            .isWeightUnit(true)
            .isVolumeUnit(false)
            .isCountUnit(false)
            .build();
        
        when(unitConversionService.checkUnitType("g"))
            .thenReturn(expectedResponse);
        
        // When
        ResponseEntity<UnitTypeCheckResponseDto> response = 
            unitConversionController.checkUnitType(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UnitTypeCheckResponseDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.isWeightUnit()).isTrue();
        assertThat(body.isVolumeUnit()).isFalse();
        assertThat(body.isCountUnit()).isFalse();
    }
}
