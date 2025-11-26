package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidCurrency}.
 * Ensures provided currency values map to {@link com.vallexia.common.enums.SupportedCurrency}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, Object> {

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedCurrency) {
            return true;
        }
        if (value instanceof String currency) {
            if (currency.isEmpty()) {
                return true;
            }

            String trimmed = currency.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedCurrency.fromCode(trimmed).isPresent();
        }
        
        return false;
    }
}
