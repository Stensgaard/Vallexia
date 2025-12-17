package com.vallexia.common.validator;

import com.vallexia.user.entity.enums.SubscriptionStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Validator implementation for {@link ValidSubscriptionStatus}.
 * Ensures provided subscription status values map to {@link com.vallexia.user.entity.enums.SubscriptionStatus}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
public class ValidSubscriptionStatusValidator implements ConstraintValidator<ValidSubscriptionStatus, Object> {

    @Override
    public void initialize(ValidSubscriptionStatus constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SubscriptionStatus) {
            return true;
        }
        if (value instanceof String subscriptionStatus) {
            if (subscriptionStatus.isEmpty()) {
                return true;
            }

            String trimmed = subscriptionStatus.trim();
            if (trimmed.isEmpty()) {
                return true;
            }

            boolean isValid = Arrays.stream(SubscriptionStatus.values())
                    .anyMatch(item -> item.name().equalsIgnoreCase(trimmed));
            if (!isValid) {
                String supportedValues = getSupportedValuesAsString();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Subscription status must be one of the supported subscription statuses: " + supportedValues
                ).addConstraintViolation();
            }
            return isValid;
        }

        String supportedValues = getSupportedValuesAsString();
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Subscription status must be one of the supported subscription statuses: " + supportedValues
        ).addConstraintViolation();
        return false;
    }

    private String getSupportedValuesAsString() {
        return Arrays.stream(SubscriptionStatus.values())
            .map(SubscriptionStatus::name)
            .collect(Collectors.joining(", "));
    }
}
