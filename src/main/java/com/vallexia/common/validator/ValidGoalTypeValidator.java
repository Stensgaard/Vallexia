package com.vallexia.common.validator;

import com.vallexia.nutrition.enums.GoalType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidGoalType}.
 * Ensures provided goal type values map to {@link com.vallexia.nutrition.enums.GoalType}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class ValidGoalTypeValidator implements ConstraintValidator<ValidGoalType, Object> {

    @Override
    public void initialize(ValidGoalType constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof GoalType) {
            return true;
        }
        if (value instanceof String goalType) {
            if (goalType.isEmpty()) {
                return true;
            }
            
            String trimmed = goalType.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            return GoalType.fromCode(trimmed).isPresent();
        }
        
        return false;
    }
}
