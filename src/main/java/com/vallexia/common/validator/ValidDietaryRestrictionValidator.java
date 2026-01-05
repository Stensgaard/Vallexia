package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.validator.strategy.DietaryRestrictionValidationStrategy;

/**
 * Validator implementation for {@link ValidDietaryRestriction}.
 * Ensures provided dietary restriction values map to {@link com.vallexia.common.enums.SupportedDietaryRestriction}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-24
 */
public class ValidDietaryRestrictionValidator extends AbstractEnumValidator<ValidDietaryRestriction, SupportedDietaryRestriction> {

    public ValidDietaryRestrictionValidator() {
        super(new DietaryRestrictionValidationStrategy());
    }
}
