package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedCuisineType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Validator implementation for {@link ValidCuisineType}.
 * Ensures provided cuisine type values map to {@link com.vallexia.common.enums.SupportedCuisineType}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidCuisineTypeValidator implements ConstraintValidator<ValidCuisineType, Object> {

    @Override
    public void initialize(ValidCuisineType constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedCuisineType) {
            return true;
        }
        if (value instanceof String cuisineType) {
            boolean isValid = isValidString(cuisineType);
            if (!isValid && !cuisineType.isEmpty() && !cuisineType.trim().isEmpty()) {
                String supportedValues = getSupportedValuesAsString();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Cuisine type must be one of the supported cuisine types: " + supportedValues
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
            "Cuisine type must be one of the supported cuisine types: " + supportedValues
        ).addConstraintViolation();
        return false;
    }

    /**
     * Validates a string cuisine type value.
     *
     * @param cuisineType the string to validate
     * @return true if valid or empty, false otherwise
     */
    private boolean isValidString(String cuisineType) {
        if (cuisineType.isEmpty()) {
            return true;
        }

        String trimmed = cuisineType.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        return SupportedCuisineType.fromCode(trimmed).isPresent();
    }

    /**
     * Validates a collection of cuisine type values.
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
                    "Cuisine type must be one of the supported cuisine types: " + supportedValues
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
        if (element instanceof SupportedCuisineType) {
            return true; // Valid enum instance
        }
        if (element instanceof String cuisineTypeStr) {
            return isValidString(cuisineTypeStr);
        }
        return false; // Invalid type in collection
    }

    private String getSupportedValuesAsString() {
        return SupportedCuisineType.getAll().stream()
            .map(SupportedCuisineType::name)
            .collect(Collectors.joining(", "));
    }
}
