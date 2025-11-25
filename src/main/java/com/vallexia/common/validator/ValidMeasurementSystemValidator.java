package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedMeasurementSystem;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidMeasurementSystem annotation.
 * Validates that a measurement system string is one of the supported systems defined in MeasurementSystem enum.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
public class ValidMeasurementSystemValidator implements ConstraintValidator<ValidMeasurementSystem, String> {
    
    @Override
    public void initialize(ValidMeasurementSystem constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String measurementSystem, ConstraintValidatorContext context) {
        if (measurementSystem == null || measurementSystem.isEmpty()) {
            return true;
        }

        return SupportedMeasurementSystem.fromCode(measurementSystem.trim()).isPresent();
    }
}
