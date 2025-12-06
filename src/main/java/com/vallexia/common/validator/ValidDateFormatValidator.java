package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedDateFormat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidDateFormat}.
 * Ensures provided date format values map to {@link com.vallexia.common.enums.SupportedDateFormat}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
public class ValidDateFormatValidator implements ConstraintValidator<ValidDateFormat, Object> {
    
    @Override
    public void initialize(ValidDateFormat constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedDateFormat) {
            return true;
        }
        if (value instanceof String dateFormat) {
            if (dateFormat.isEmpty()) {
                return true;
            }

            String trimmed = dateFormat.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedDateFormat.isValidCode(trimmed);
        }
        
        return false;
    }
}
