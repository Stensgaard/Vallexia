package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidFirstDayOfWeek annotation.
 * Validates that a first day of week value is one of the supported options defined in SupportedFirstDayOfWeek enum.
 * Only accepts SupportedFirstDayOfWeek enum instances or string/CharSequence values that can be resolved to the enum.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class ValidFirstDayOfWeekValidator implements ConstraintValidator<ValidFirstDayOfWeek, Object> {

    @Override
    public void initialize(ValidFirstDayOfWeek constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value instanceof SupportedFirstDayOfWeek) {
            return true;
        }

        if (value instanceof CharSequence sequence) {
            return SupportedFirstDayOfWeek.fromCode(sequence.toString()).isPresent();
        }

        return false;
    }
}
