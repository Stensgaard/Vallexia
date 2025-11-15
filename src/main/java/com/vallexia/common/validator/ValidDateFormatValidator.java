package com.vallexia.common.validator;

import com.vallexia.user.entity.enums.DateFormat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidDateFormat annotation.
 * Validates that a date format string is one of the supported formats defined in DateFormat enum.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class ValidDateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {
    
    @Override
    public void initialize(ValidDateFormat constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String dateFormat, ConstraintValidatorContext context) {
        // Null values are handled by @NotBlank or @NotNull annotations
        if (dateFormat == null || dateFormat.isEmpty()) {
            return true;
        }
        
        return DateFormat.isValidFormat(dateFormat);
    }
}
