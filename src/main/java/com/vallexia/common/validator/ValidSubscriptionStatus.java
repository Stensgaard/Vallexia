package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a subscription status string matches one of the supported {@link com.vallexia.user.entity.enums.SubscriptionStatus} values.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@Documented
@Constraint(validatedBy = ValidSubscriptionStatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSubscriptionStatus {

    String message() default "Subscription status must be one of the supported values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
