package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a country code matches one of the {@link com.vallexia.common.enums.SupportedCountry}
 * entries.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@Documented
@Constraint(validatedBy = ValidCountryValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountry {

    String message() default "Country must be one of the supported countries";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
