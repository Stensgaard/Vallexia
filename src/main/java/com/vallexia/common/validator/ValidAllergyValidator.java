package com.vallexia.common.validator;

import com.vallexia.user.entity.enums.Allergy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidAllergy annotation.
 * Validates that an allergy string matches one of the supported Allergy enum values.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class ValidAllergyValidator implements ConstraintValidator<ValidAllergy, String> {

    @Override
    public void initialize(ValidAllergy constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        return Allergy.fromName(trimmed).isPresent();
    }
}
