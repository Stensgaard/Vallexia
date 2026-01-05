package com.vallexia.common.validator;

import com.vallexia.common.validator.strategy.SubscriptionStatusValidationStrategy;
import com.vallexia.user.entity.enums.SubscriptionStatus;

/**
 * Validator implementation for {@link ValidSubscriptionStatus}.
 * Ensures provided subscription status values map to {@link com.vallexia.user.entity.enums.SubscriptionStatus}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-25
 */
public class ValidSubscriptionStatusValidator extends AbstractEnumValidator<ValidSubscriptionStatus, SubscriptionStatus> {

    public ValidSubscriptionStatusValidator() {
        super(new SubscriptionStatusValidationStrategy());
    }
}
