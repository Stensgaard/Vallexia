package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedMeasurementSystem;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidMeasurementSystem}.
 * Ensures provided measurement system values map to {@link com.vallexia.common.enums.SupportedMeasurementSystem}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
public class ValidMeasurementSystemValidator implements ConstraintValidator<ValidMeasurementSystem, Object> {
    
    @Override
    public void initialize(ValidMeasurementSystem constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedMeasurementSystem) {
            return true;
        }
        if (value instanceof String measurementSystem) {
            if (measurementSystem.isEmpty()) {
                return true;
            }

            String trimmed = measurementSystem.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedMeasurementSystem.fromCode(trimmed).isPresent();
        }

        return false;
    }
}
