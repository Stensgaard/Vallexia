package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a cuisine type matches one of the {@link com.vallexia.common.enums.SupportedCuisineType}
 * entries.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
@Documented
@Constraint(validatedBy = ValidCuisineTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCuisineType {

    String message() default "Cuisine type must be one of the supported cuisine types";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
