package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedAllergy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * Validator implementation for {@link ValidAllergy}.
 * Ensures provided allergy values map to {@link com.vallexia.common.enums.SupportedAllergy}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidAllergyValidator implements ConstraintValidator<ValidAllergy, Object> {

    @Override
    public void initialize(ValidAllergy constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedAllergy) {
            return true;
        }
        if (value instanceof String allergy) {
            if (allergy.isEmpty()) {
                return true;
            }

            String trimmed = allergy.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedAllergy.fromCode(trimmed).isPresent();
        }
        if (value instanceof Collection<?> collection) {
            // Validate each element in the collection
            for (Object element : collection) {
                if (element == null) {
                    continue; // Allow null elements
                }
                if (element instanceof SupportedAllergy) {
                    continue; // Valid enum instance
                }
                if (element instanceof String allergyStr) {
                    if (allergyStr.isEmpty() || allergyStr.trim().isEmpty()) {
                        continue; // Allow empty strings
                    }
                    if (!SupportedAllergy.fromCode(allergyStr.trim()).isPresent()) {
                        return false; // Invalid allergy code
                    }
                } else {
                    return false; // Invalid type in collection
                }
            }
            return true;
        }
        
        return false;
    }
}
