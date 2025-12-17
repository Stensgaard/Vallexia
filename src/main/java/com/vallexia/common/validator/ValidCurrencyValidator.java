package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.stream.Collectors;

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

            boolean isValid = SupportedCurrency.fromCode(trimmed).isPresent();
            if (!isValid) {
                String supportedValues = getSupportedValuesAsString();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Currency must be one of the supported currencies: " + supportedValues
                ).addConstraintViolation();
            }
            return isValid;
        }
        
        String supportedValues = getSupportedValuesAsString();
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Currency must be one of the supported currencies: " + supportedValues
        ).addConstraintViolation();
        return false;
    }

    private String getSupportedValuesAsString() {
        return SupportedCurrency.getAll().stream()
            .map(SupportedCurrency::getCode)
            .collect(Collectors.joining(", "));
    }
}
