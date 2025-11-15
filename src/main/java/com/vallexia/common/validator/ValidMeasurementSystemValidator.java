package com.vallexia.common.validator;

import com.vallexia.user.entity.enums.MeasurementSystem;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidMeasurementSystem annotation.
 * Validates that a measurement system string is one of the supported systems defined in MeasurementSystem enum.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class ValidMeasurementSystemValidator implements ConstraintValidator<ValidMeasurementSystem, String> {
    
    @Override
    public void initialize(ValidMeasurementSystem constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String measurementSystem, ConstraintValidatorContext context) {
        // Null values are handled by @NotBlank or @NotNull annotations
        if (measurementSystem == null || measurementSystem.isEmpty()) {
            return true;
        }
        
        return MeasurementSystem.isValid(measurementSystem);
    }
}
