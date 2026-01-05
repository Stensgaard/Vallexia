package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.common.validator.strategy.TimezoneValidationStrategy;

/**
 * Validator implementation for {@link ValidTimezone}.
 * Ensures provided timezone values map to {@link com.vallexia.common.enums.SupportedTimezone}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-15
 */
public class ValidTimezoneValidator extends AbstractEnumValidator<ValidTimezone, SupportedTimezone> {

    public ValidTimezoneValidator() {
        super(new TimezoneValidationStrategy());
    }
}
