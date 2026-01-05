package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.validator.strategy.DateFormatValidationStrategy;

/**
 * Validator implementation for {@link ValidDateFormat}.
 * Ensures provided date format values map to {@link com.vallexia.common.enums.SupportedDateFormat}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-15
 */
public class ValidDateFormatValidator extends AbstractEnumValidator<ValidDateFormat, SupportedDateFormat> {

    public ValidDateFormatValidator() {
        super(new DateFormatValidationStrategy());
    }
}
