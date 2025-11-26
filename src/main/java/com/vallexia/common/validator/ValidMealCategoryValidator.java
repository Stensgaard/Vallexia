package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedMealCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidMealCategory}.
 * Ensures provided meal category values map to {@link com.vallexia.common.enums.SupportedMealCategory}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
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
        
        return false;
    }
}
