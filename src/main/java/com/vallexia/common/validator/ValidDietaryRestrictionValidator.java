package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedDietaryRestriction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Validator implementation for {@link ValidDietaryRestriction}.
 * Ensures provided dietary restriction values map to {@link com.vallexia.common.enums.SupportedDietaryRestriction}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidDietaryRestrictionValidator implements ConstraintValidator<ValidDietaryRestriction, Object> {

    @Override
    public void initialize(ValidDietaryRestriction constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedDietaryRestriction) {
            return true;
        }
        if (value instanceof String dietaryRestriction) {
            boolean isValid = isValidString(dietaryRestriction);
            if (!isValid && !dietaryRestriction.isEmpty() && !dietaryRestriction.trim().isEmpty()) {
                String supportedValues = getSupportedValuesAsString();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Dietary restriction must be one of the supported dietary restrictions: " + supportedValues
                ).addConstraintViolation();
            }
            return isValid;
        }
        if (value instanceof Collection<?> collection) {
            return isValidCollection(collection, context);
        }
        
        String supportedValues = getSupportedValuesAsString();
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Dietary restriction must be one of the supported dietary restrictions: " + supportedValues
        ).addConstraintViolation();
        return false;
    }

    /**
     * Validates a string dietary restriction value.
     *
     * @param dietaryRestriction the string to validate
     * @return true if valid or empty, false otherwise
     */
    private boolean isValidString(String dietaryRestriction) {
        if (dietaryRestriction.isEmpty()) {
            return true;
        }

        String trimmed = dietaryRestriction.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        return SupportedDietaryRestriction.fromCode(trimmed).isPresent();
    }

    /**
     * Validates a collection of dietary restriction values.
     *
     * @param collection the collection to validate
     * @param context the constraint validator context
     * @return true if all elements are valid, false otherwise
     */
    private boolean isValidCollection(Collection<?> collection, ConstraintValidatorContext context) {
        for (Object element : collection) {
            if (!isValidElement(element)) {
                String supportedValues = getSupportedValuesAsString();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Dietary restriction must be one of the supported dietary restrictions: " + supportedValues
                ).addConstraintViolation();
                return false;
            }
        }
        return true;
    }

    /**
     * Validates a single element from a collection.
     *
     * @param element the element to validate
     * @return true if valid, null, or empty, false otherwise
     */
    private boolean isValidElement(Object element) {
        if (element == null) {
            return true; // Allow null elements
        }
        if (element instanceof SupportedDietaryRestriction) {
            return true; // Valid enum instance
        }
        if (element instanceof String restrictionStr) {
            return isValidString(restrictionStr);
        }
        return false; // Invalid type in collection
    }

    private String getSupportedValuesAsString() {
        return SupportedDietaryRestriction.getAll().stream()
            .map(SupportedDietaryRestriction::name)
            .collect(Collectors.joining(", "));
    }
}
