package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedLocale;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidLocale}.
 * Ensures provided locale values map to {@link com.vallexia.common.enums.SupportedLocale}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
public class ValidLocaleValidator implements ConstraintValidator<ValidLocale, Object> {
    
    @Override
    public void initialize(ValidLocale constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedLocale) {
            return true;
        }
        if (value instanceof String locale) {
            if (locale.isEmpty()) {
                return true;
            }

            String trimmed = locale.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedLocale.fromCode(trimmed).isPresent();
        }

        return false;
    }
}
