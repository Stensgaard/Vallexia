package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedTimezone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidTimezone}.
 * Ensures provided timezone values map to {@link com.vallexia.common.enums.SupportedTimezone}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
public class ValidTimezoneValidator implements ConstraintValidator<ValidTimezone, Object> {

    @Override
    public void initialize(ValidTimezone constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedTimezone) {
            return true;
        }
        if (value instanceof String timezone) {
            if (timezone.isEmpty()) {
                return true;
            }

            String trimmed = timezone.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedTimezone.fromValue(trimmed).isPresent();
        }

        return false;
    }
}
