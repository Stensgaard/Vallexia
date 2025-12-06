package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure a date format string is one of the supported formats.
 * Uses the SupportedDateFormat enum as the source of truth.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
 */
@Documented
@Constraint(validatedBy = ValidDateFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateFormat {
    
    String message() default "Date format must be one of the supported formats";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
