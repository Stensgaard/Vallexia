package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedCuisineType} enum.
 * Supports collection validation.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class CuisineTypeValidationStrategy implements EnumValidationStrategy<SupportedCuisineType> {

  @Override
  public Optional<SupportedCuisineType> validateString(String trimmedValue) {
    return SupportedCuisineType.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedCuisineType> getAllValues() {
    return SupportedCuisineType.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedCuisineType enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return true;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Cuisine type must be one of the supported cuisine types: ";
  }

  @Override
  public Class<SupportedCuisineType> getEnumClass() {
    return SupportedCuisineType.class;
  }
}
