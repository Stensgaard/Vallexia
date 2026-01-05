package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.validator.strategy.CuisineTypeValidationStrategy;

/**
 * Validator implementation for {@link ValidCuisineType}.
 * Ensures provided cuisine type values map to {@link com.vallexia.common.enums.SupportedCuisineType}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-24
 */
public class ValidCuisineTypeValidator extends AbstractEnumValidator<ValidCuisineType, SupportedCuisineType> {

    public ValidCuisineTypeValidator() {
        super(new CuisineTypeValidationStrategy());
    }
}
