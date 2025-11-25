package com.vallexia.common.validator;

import com.vallexia.user.entity.enums.GoalType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidGoalType annotation.
 * Validates that a goal type string is one of the supported options defined in GoalType enum.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class ValidGoalTypeValidator implements ConstraintValidator<ValidGoalType, String> {

    @Override
    public void initialize(ValidGoalType constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return GoalType.fromCode(value).isPresent();
    }
}
