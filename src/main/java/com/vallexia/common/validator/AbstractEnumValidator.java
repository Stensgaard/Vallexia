package com.vallexia.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Abstract base validator for enum validation that eliminates code duplication.
 * Handles common validation patterns: null checks, enum instance checks,
 * string validation with trimming, and optional collection validation.
 *
 * @param <A> the annotation type
 * @param <T> the enum type being validated
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public abstract class AbstractEnumValidator<A extends Annotation, T extends Enum<T>> implements ConstraintValidator<A, Object> {

  private final EnumValidationStrategy<T> strategy;

  /**
   * Constructs the validator with the provided strategy.
   *
   * @param strategy the enum validation strategy
   */
  protected AbstractEnumValidator(EnumValidationStrategy<T> strategy) {
    this.strategy = strategy;
  }

  @Override
  public void initialize(A constraintAnnotation) {
    // No initialization required
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    // Check if value is already the correct enum type
    if (strategy.getEnumClass().isInstance(value)) {
      return true;
    }

    // Handle String values
    if (value instanceof String stringValue) {
      return isValidString(stringValue, context);
    }

    // Handle collections if supported
    if (strategy.supportsCollections() && value instanceof Collection<?> collection) {
      return isValidCollection(collection, context);
    }

    // Invalid type
    String supportedValues = getSupportedValuesAsString();
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(
        buildErrorMessage(supportedValues)
    ).addConstraintViolation();
    return false;
  }

  /**
   * Validates a string value.
   *
   * @param stringValue the string to validate
   * @param context the constraint validator context
   * @return true if valid, false otherwise
   */
  private boolean isValidString(String stringValue, ConstraintValidatorContext context) {
    if (stringValue.isEmpty()) {
      return true;
    }

    String trimmed = stringValue.trim();
    if (trimmed.isEmpty()) {
      return true;
    }

    boolean isValid = strategy.validateString(trimmed).isPresent();
    if (!isValid) {
      String supportedValues = getSupportedValuesAsString();
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          buildErrorMessage(supportedValues)
      ).addConstraintViolation();
    }
    return isValid;
  }

  /**
   * Validates a collection of values.
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
            buildErrorMessage(supportedValues)
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

    if (strategy.getEnumClass().isInstance(element)) {
      return true; // Valid enum instance
    }

    if (element instanceof String elementStr) {
      if (elementStr.isEmpty()) {
        return true;
      }
      String trimmed = elementStr.trim();
      if (trimmed.isEmpty()) {
        return true;
      }
      return strategy.validateString(trimmed).isPresent();
    }

    return false; // Invalid type in collection
  }

  /**
   * Gets all supported values as a comma-separated string for error messages.
   *
   * @return comma-separated string of supported values
   */
  private String getSupportedValuesAsString() {
    return strategy.getAllValues()
        .map(strategy::getDisplayValue)
        .collect(Collectors.joining(", "));
  }

  /**
   * Builds the error message with the field name and supported values.
   *
   * @param supportedValues comma-separated supported values
   * @return error message
   */
  private String buildErrorMessage(String supportedValues) {
    return strategy.getErrorMessagePrefix() + supportedValues;
  }
}
