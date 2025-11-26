package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCountry;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidCountry}.
 * Ensures provided country values map to {@link com.vallexia.common.enums.SupportedCountry}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class ValidCountryValidator implements ConstraintValidator<ValidCountry, Object> {

    @Override
    public void initialize(ValidCountry constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedCountry) {
            return true;
        }
        if (value instanceof String countryCode) {
            if (countryCode.isEmpty()) {
                return true;
            }
            
            String trimmed = countryCode.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedCountry.fromCountry(trimmed).isPresent();
        }

        return false;
    }
}
