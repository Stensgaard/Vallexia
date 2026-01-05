package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.validator.strategy.AllergyValidationStrategy;

/**
 * Validator implementation for {@link ValidAllergy}.
 * Ensures provided allergy values map to {@link com.vallexia.common.enums.SupportedAllergy}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-24
 */
public class ValidAllergyValidator extends AbstractEnumValidator<ValidAllergy, SupportedAllergy> {

    public ValidAllergyValidator() {
        super(new AllergyValidationStrategy());
    }
}
