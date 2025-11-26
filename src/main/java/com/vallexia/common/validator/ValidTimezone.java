package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures a timezone value matches SupportedTimezone.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@Documented
@Constraint(validatedBy = ValidTimezoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimezone {

    String message() default "Timezone must be one of the supported timezones";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
