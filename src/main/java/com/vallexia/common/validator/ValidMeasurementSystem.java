package com.vallexia.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure a measurement system string is one of the supported systems.
 * Uses the MeasurementSystem enum as the source of truth.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Documented
@Constraint(validatedBy = ValidMeasurementSystemValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMeasurementSystem {
    
    String message() default "Measurement system must be one of the supported systems";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
