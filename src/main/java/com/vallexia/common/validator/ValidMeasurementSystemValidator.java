package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.validator.strategy.MeasurementSystemValidationStrategy;

/**
 * Validator implementation for {@link ValidMeasurementSystem}.
 * Ensures provided measurement system values map to {@link com.vallexia.common.enums.SupportedMeasurementSystem}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-15
 */
public class ValidMeasurementSystemValidator extends AbstractEnumValidator<ValidMeasurementSystem, SupportedMeasurementSystem> {

    public ValidMeasurementSystemValidator() {
        super(new MeasurementSystemValidationStrategy());
    }
}
