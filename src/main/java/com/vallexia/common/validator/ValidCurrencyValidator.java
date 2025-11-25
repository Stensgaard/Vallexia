package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidCurrency annotation.
 * Validates that a currency string is one of the supported currencies defined in SupportedCurrency enum.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null || currency.isEmpty()) {
            return true;
        }
        return SupportedCurrency.fromCode(currency).isPresent();
    }
}
