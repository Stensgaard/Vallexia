package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure a locale string is one of the supported locales.
 * Uses the SupportedLocale enum as the source of truth.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Documented
@Constraint(validatedBy = ValidLocaleValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLocale {
    
    String message() default "Locale must be one of the supported locales";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
