package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedMealCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Validator implementation for {@link ValidMealCategory}.
 * Ensures provided meal category values map to {@link com.vallexia.common.enums.SupportedMealCategory}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidMealCategoryValidator implements ConstraintValidator<ValidMealCategory, Object> {

    @Override
    public void initialize(ValidMealCategory constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedMealCategory) {
            return true;
        }
        if (value instanceof String mealCategory) {
            boolean isValid = isValidString(mealCategory);
            if (!isValid && !mealCategory.isEmpty() && !mealCategory.trim().isEmpty()) {
                String supportedValues = getSupportedValuesAsString();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Meal category must be one of the supported meal categories: " + supportedValues
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
            "Meal category must be one of the supported meal categories: " + supportedValues
        ).addConstraintViolation();
        return false;
    }

    /**
     * Validates a string meal category value.
     * 
     * @param mealCategory the string to validate
     * @return true if valid or empty, false otherwise
     */
    private boolean isValidString(String mealCategory) {
        if (mealCategory.isEmpty()) {
            return true;
        }

        String trimmed = mealCategory.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        return SupportedMealCategory.fromCode(trimmed).isPresent();
    }

    /**
     * Validates a collection of meal category values.
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
                    "Meal category must be one of the supported meal categories: " + supportedValues
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
        if (element instanceof SupportedMealCategory) {
            return true; // Valid enum instance
        }
        if (element instanceof String mealCategoryStr) {
            return isValidString(mealCategoryStr);
        }
        return false; // Invalid type in collection
    }

    private String getSupportedValuesAsString() {
        return SupportedMealCategory.getAll().stream()
            .map(SupportedMealCategory::name)
            .collect(Collectors.joining(", "));
    }
}
