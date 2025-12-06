package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidFirstDayOfWeek}.
 * Ensures provided first day of week values map to {@link com.vallexia.common.enums.SupportedFirstDayOfWeek}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
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
        if (value instanceof String firstDayOfWeek) {
            if (firstDayOfWeek.isEmpty()) {
                return true;
            }
            
            String trimmed = firstDayOfWeek.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedFirstDayOfWeek.fromCode(trimmed).isPresent();
        }

        return false;
    }
}
