package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCuisineType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * Validator implementation for {@link ValidCuisineType}.
 * Ensures provided cuisine type values map to {@link com.vallexia.common.enums.SupportedCuisineType}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidCuisineTypeValidator implements ConstraintValidator<ValidCuisineType, Object> {

    @Override
    public void initialize(ValidCuisineType constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedCuisineType) {
            return true;
        }
        if (value instanceof String cuisineType) {
            if (cuisineType.isEmpty()) {
                return true;
            }

            String trimmed = cuisineType.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedCuisineType.fromCode(trimmed).isPresent();
        }
        if (value instanceof Collection<?> collection) {
            // Validate each element in the collection
            for (Object element : collection) {
                if (element == null) {
                    continue; // Allow null elements
                }
                if (element instanceof SupportedCuisineType) {
                    continue; // Valid enum instance
                }
                if (element instanceof String cuisineTypeStr) {
                    if (cuisineTypeStr.isEmpty() || cuisineTypeStr.trim().isEmpty()) {
                        continue; // Allow empty strings
                    }
                    if (!SupportedCuisineType.fromCode(cuisineTypeStr.trim()).isPresent()) {
                        return false; // Invalid cuisine type code
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
