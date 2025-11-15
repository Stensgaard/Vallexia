package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedLocale;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidLocale annotation.
 * Validates that a locale string is one of the supported locales defined in SupportedLocale enum.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class ValidLocaleValidator implements ConstraintValidator<ValidLocale, String> {
    
    @Override
    public void initialize(ValidLocale constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String locale, ConstraintValidatorContext context) {
        // Null values are handled by @NotBlank or @NotNull annotations
        if (locale == null || locale.isEmpty()) {
            return true;
        }
        
        return SupportedLocale.isSupported(locale);
    }
}
