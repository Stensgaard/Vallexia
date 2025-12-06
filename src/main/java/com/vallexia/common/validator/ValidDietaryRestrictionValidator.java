package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedDietaryRestriction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * Validator implementation for {@link ValidDietaryRestriction}.
 * Ensures provided dietary restriction values map to {@link com.vallexia.common.enums.SupportedDietaryRestriction}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidDietaryRestrictionValidator implements ConstraintValidator<ValidDietaryRestriction, Object> {

    @Override
    public void initialize(ValidDietaryRestriction constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedDietaryRestriction) {
            return true;
        }
        if (value instanceof String dietaryRestriction) {
            if (dietaryRestriction.isEmpty()) {
                return true;
            }

            String trimmed = dietaryRestriction.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedDietaryRestriction.fromCode(trimmed).isPresent();
        }
        if (value instanceof Collection<?> collection) {
            // Validate each element in the collection
            for (Object element : collection) {
                if (element == null) {
                    continue; // Allow null elements
                }
                if (element instanceof SupportedDietaryRestriction) {
                    continue; // Valid enum instance
                }
                if (element instanceof String restrictionStr) {
                    if (restrictionStr.isEmpty() || restrictionStr.trim().isEmpty()) {
                        continue; // Allow empty strings
                    }
                    if (!SupportedDietaryRestriction.fromCode(restrictionStr.trim()).isPresent()) {
                        return false; // Invalid dietary restriction code
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
