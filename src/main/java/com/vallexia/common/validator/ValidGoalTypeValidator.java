package com.vallexia.common.validator;

import com.vallexia.common.validator.strategy.GoalTypeValidationStrategy;
import com.vallexia.nutrition.enums.GoalType;

/**
 * Validator implementation for {@link ValidGoalType}.
 * Ensures provided goal type values map to {@link com.vallexia.nutrition.enums.GoalType}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-25
 */
public class ValidGoalTypeValidator extends AbstractEnumValidator<ValidGoalType, GoalType> {

    public ValidGoalTypeValidator() {
        super(new GoalTypeValidationStrategy());
    }
}
