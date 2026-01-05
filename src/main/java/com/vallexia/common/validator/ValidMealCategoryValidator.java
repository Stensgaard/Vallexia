package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.validator.strategy.MealCategoryValidationStrategy;

/**
 * Validator implementation for {@link ValidMealCategory}.
 * Ensures provided meal category values map to {@link com.vallexia.common.enums.SupportedMealCategory}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-24
 */
public class ValidMealCategoryValidator extends AbstractEnumValidator<ValidMealCategory, SupportedMealCategory> {

    public ValidMealCategoryValidator() {
        super(new MealCategoryValidationStrategy());
    }
}
