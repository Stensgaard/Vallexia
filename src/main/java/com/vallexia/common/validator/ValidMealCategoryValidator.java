package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedMealCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * Validator implementation for {@link ValidMealCategory}.
 * Ensures provided meal category values map to {@link com.vallexia.common.enums.SupportedMealCategory}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidMealCategoryValidator implements ConstraintValidator<ValidMealCategory, Object> {

    @Override
    public void initialize(ValidMealCategory constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedMealCategory) {
            return true;
        }
        if (value instanceof String mealCategory) {
            if (mealCategory.isEmpty()) {
                return true;
            }

            String trimmed = mealCategory.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return SupportedMealCategory.fromCode(trimmed).isPresent();
        }
        if (value instanceof Collection<?> collection) {
            // Validate each element in the collection
            for (Object element : collection) {
                if (element == null) {
                    continue; // Allow null elements
                }
                if (element instanceof SupportedMealCategory) {
                    continue; // Valid enum instance
                }
                if (element instanceof String mealCategoryStr) {
                    if (mealCategoryStr.isEmpty() || mealCategoryStr.trim().isEmpty()) {
                        continue; // Allow empty strings
                    }
                    if (!SupportedMealCategory.fromCode(mealCategoryStr.trim()).isPresent()) {
                        return false; // Invalid meal category code
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
