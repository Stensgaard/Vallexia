package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that an allergy string matches one of the supported {@link com.vallexia.user.entity.enums.Allergy} values.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@Documented
@Constraint(validatedBy = ValidAllergyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAllergy {

    String message() default "Allergy must be one of the supported values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
