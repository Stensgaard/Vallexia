package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.validator.strategy.CountryValidationStrategy;

/**
 * Validator implementation for {@link ValidCountry}.
 * Ensures provided country values map to {@link com.vallexia.common.enums.SupportedCountry}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-25
 */
public class ValidCountryValidator extends AbstractEnumValidator<ValidCountry, SupportedCountry> {

    public ValidCountryValidator() {
        super(new CountryValidationStrategy());
    }
}
