package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.validator.strategy.FirstDayOfWeekValidationStrategy;

/**
 * Validator implementation for {@link ValidFirstDayOfWeek}.
 * Ensures provided first day of week values map to {@link com.vallexia.common.enums.SupportedFirstDayOfWeek}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-25
 */
public class ValidFirstDayOfWeekValidator extends AbstractEnumValidator<ValidFirstDayOfWeek, SupportedFirstDayOfWeek> {

    public ValidFirstDayOfWeekValidator() {
        super(new FirstDayOfWeekValidationStrategy());
    }
}
