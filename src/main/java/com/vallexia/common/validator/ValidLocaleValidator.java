package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedLocale;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidLocale annotation.
 * Validates that a locale string is one of the supported locales defined in SupportedLocale enum.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
public class ValidLocaleValidator implements ConstraintValidator<ValidLocale, String> {
    
    @Override
    public void initialize(ValidLocale constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String locale, ConstraintValidatorContext context) {
        if (locale == null || locale.isEmpty()) {
            return true;
        }
        return SupportedLocale.fromCode(locale).isPresent();
    }
}
