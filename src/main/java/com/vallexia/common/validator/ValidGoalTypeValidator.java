package com.vallexia.common.validator;

import com.vallexia.nutrition.enums.GoalType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.stream.Collectors;

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

            boolean isValid = GoalType.fromCode(trimmed).isPresent();
            if (!isValid) {
                String supportedValues = getSupportedValuesAsString();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Goal type must be one of the supported goal types: " + supportedValues
                ).addConstraintViolation();
            }
            return isValid;
        }
        
        String supportedValues = getSupportedValuesAsString();
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Goal type must be one of the supported goal types: " + supportedValues
        ).addConstraintViolation();
        return false;
    }

    private String getSupportedValuesAsString() {
        return Arrays.stream(GoalType.values())
            .map(GoalType::name)
            .collect(Collectors.joining(", "));
    }
}
