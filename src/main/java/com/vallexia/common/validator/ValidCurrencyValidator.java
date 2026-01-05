package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.validator.strategy.CurrencyValidationStrategy;

/**
 * Validator implementation for {@link ValidCurrency}.
 * Ensures provided currency values map to {@link com.vallexia.common.enums.SupportedCurrency}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-24
 */
public class ValidCurrencyValidator extends AbstractEnumValidator<ValidCurrency, SupportedCurrency> {

    public ValidCurrencyValidator() {
        super(new CurrencyValidationStrategy());
    }
}
