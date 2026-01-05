package com.vallexia.common.validator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Strategy interface for enum-specific validation logic.
 * Encapsulates the differences between various enum types and their validation methods.
 *
 * @param <T> the enum type being validated
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public interface EnumValidationStrategy<T extends Enum<T>> {

  /**
   * Validates a trimmed string value against the enum.
   *
   * @param trimmedValue the trimmed string value to validate
   * @return Optional containing the matching enum value, or empty if not found
   */
  Optional<T> validateString(String trimmedValue);

  /**
   * Returns all valid enum values for error message generation.
   *
   * @return Stream of all enum values
   */
  Stream<T> getAllValues();

  /**
   * Extracts the display string from an enum value for error messages.
   *
   * @param enumValue the enum value
   * @return display string representation
   */
  String getDisplayValue(T enumValue);

  /**
   * Indicates whether this validator supports collection validation.
   *
   * @return true if collections are supported, false otherwise
   */
  boolean supportsCollections();

  /**
   * Returns the error message prefix for constraint violations.
   * Should be in the format: "{field} must be one of the supported {plural}: "
   *
   * @return error message prefix (e.g., "Country must be one of the supported countries: ")
   */
  String getErrorMessagePrefix();

  /**
   * Gets the enum class for type checking.
   *
   * @return the enum class
   */
  Class<T> getEnumClass();
}
