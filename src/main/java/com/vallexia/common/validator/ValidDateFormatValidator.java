package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedDateFormat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidDateFormat annotation.
 * Validates that a date format string is one of the supported formats defined in DateFormat enum.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
public class ValidDateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {
    
    @Override
    public void initialize(ValidDateFormat constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String dateFormat, ConstraintValidatorContext context) {
        if (dateFormat == null || dateFormat.isEmpty()) {
            return true;
        }
        String trimmed = dateFormat.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        return SupportedDateFormat.isValidCode(trimmed);
    }
}
