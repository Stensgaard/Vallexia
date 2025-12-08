package com.vallexia.common.validator;

import com.vallexia.common.enums.SupportedAllergy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * Validator implementation for {@link ValidAllergy}.
 * Ensures provided allergy values map to {@link com.vallexia.common.enums.SupportedAllergy}.
 *
 * <p>Values are trimmed before validation so surrounding whitespace does not cause false negatives.</p>
 * <p>For collections, validates each element in the collection.</p>
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-24
 */
public class ValidAllergyValidator implements ConstraintValidator<ValidAllergy, Object> {

    @Override
    public void initialize(ValidAllergy constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof SupportedAllergy) {
            return true;
        }
        if (value instanceof String allergy) {
            return isValidString(allergy);
        }
        if (value instanceof Collection<?> collection) {
            return isValidCollection(collection);
        }
        
        return false;
    }

    /**
     * Validates a string allergy value.
     * 
     * @param allergy the string to validate
     * @return true if valid or empty, false otherwise
     */
    private boolean isValidString(String allergy) {
        if (allergy.isEmpty()) {
            return true;
        }

        String trimmed = allergy.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        return SupportedAllergy.fromCode(trimmed).isPresent();
    }

    /**
     * Validates a collection of allergy values.
     * 
     * @param collection the collection to validate
     * @return true if all elements are valid, false otherwise
     */
    private boolean isValidCollection(Collection<?> collection) {
        for (Object element : collection) {
            if (!isValidElement(element)) {
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
        if (element instanceof SupportedAllergy) {
            return true; // Valid enum instance
        }
        if (element instanceof String allergyStr) {
            return isValidString(allergyStr);
        }
        return false; // Invalid type in collection
    }
}
