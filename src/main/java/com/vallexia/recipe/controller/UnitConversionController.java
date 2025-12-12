package com.vallexia.recipe.controller;

import com.vallexia.recipe.dto.*;
import com.vallexia.recipe.service.UnitConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

// TODO make api tests when this has been refactored to use spoonacular API

/**
 * REST controller for unit conversion operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-02
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/units")
@Tag(
    name = "Unit Conversion",
    description = "Operations for converting between measurement units")
public class UnitConversionController {
    
    private final UnitConversionService unitConversionService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param unitConversionService the unit conversion service
     */
    public UnitConversionController(UnitConversionService unitConversionService) {
        this.unitConversionService = unitConversionService;
    }
    
    /**
     * Convert a value from one unit to another.
     * Supports weight and volume unit conversions.
     * 
     * @param request the conversion request
     * @return conversion response with converted value
     */
    @Operation(
        summary = "Convert between units",
        description = "Converts a value from one unit to another. Supports weight (g, kg, mg, oz, lb) " +
            "and volume (ml, l, cup, tbsp, tsp, fl oz) unit conversions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversion successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request (invalid units or value)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/convert")
    public ResponseEntity<UnitConversionResponseDto> convert(
            @Valid @RequestBody UnitConversionRequestDto request) {
        log.debug("Converting {} {} to {}", request.getValue(), request.getFromUnit(), request.getToUnit());
        
        BigDecimal convertedValue = unitConversionService.convert(request);
        
        UnitConversionResponseDto response = UnitConversionResponseDto.builder()
            .convertedValue(convertedValue)
            .build();
        
        log.debug("Conversion result: {} {} = {} {}", 
            request.getValue(), request.getFromUnit(),
            convertedValue, request.getToUnit());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get appropriate display unit based on measurement system.
     * 
     * @param request the display unit request
     * @return display unit response
     */
    @Operation(
        summary = "Get display unit",
        description = "Returns the appropriate display unit based on measurement system. " +
            "For weight units, converts between metric and imperial. For volume and count units, returns original.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Display unit retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/display-unit")
    public ResponseEntity<GetDisplayUnitResponseDto> getDisplayUnit(
            @Valid @RequestBody GetDisplayUnitRequestDto request) {
        log.debug("Getting display unit for {} with system {}", request.getUnit(), request.getMeasurementSystem());
        
        String displayUnit = unitConversionService.getDisplayUnit(
            request.getUnit(),
            request.getMeasurementSystem()
        );
        
        GetDisplayUnitResponseDto response = GetDisplayUnitResponseDto.builder()
            .displayUnit(displayUnit)
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check unit type (weight, volume, or count).
     * 
     * @param request the unit type check request
     * @return unit type check response
     */
    @Operation(
        summary = "Check unit type",
        description = "Checks if a unit is a weight, volume, or count unit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unit type check completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/check-type")
    public ResponseEntity<UnitTypeCheckResponseDto> checkUnitType(
            @Valid @RequestBody UnitTypeCheckRequestDto request) {
        log.debug("Checking unit type for {}", request.getUnit());
        
        UnitTypeCheckResponseDto response = unitConversionService.checkUnitType(request.getUnit());
        
        return ResponseEntity.ok(response);
    }
}
