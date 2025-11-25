package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidFirstDayOfWeek annotation.
 * Validates that a first day of week value is one of the supported options defined in SupportedFirstDayOfWeek enum.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class ValidFirstDayOfWeekValidator implements ConstraintValidator<ValidFirstDayOfWeek, String> {

    @Override
    public void initialize(ValidFirstDayOfWeek constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return SupportedFirstDayOfWeek.fromCode(value).isPresent();
    }
}
