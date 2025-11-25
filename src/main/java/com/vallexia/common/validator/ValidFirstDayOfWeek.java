package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures a first day of week value matches the supported set.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@Documented
@Constraint(validatedBy = ValidFirstDayOfWeekValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFirstDayOfWeek {

    String message() default "First day of week must be one of the supported options";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
