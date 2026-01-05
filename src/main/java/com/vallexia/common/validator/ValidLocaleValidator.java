package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.validator.strategy.LocaleValidationStrategy;

/**
 * Validator implementation for {@link ValidLocale}.
 * Ensures provided locale values map to {@link com.vallexia.common.enums.SupportedLocale}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-15
 */
public class ValidLocaleValidator extends AbstractEnumValidator<ValidLocale, SupportedLocale> {

    public ValidLocaleValidator() {
        super(new LocaleValidationStrategy());
    }
}
