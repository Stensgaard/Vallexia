package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedDietaryRestriction} enum.
 * Supports collection validation.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class DietaryRestrictionValidationStrategy implements EnumValidationStrategy<SupportedDietaryRestriction> {

  @Override
  public Optional<SupportedDietaryRestriction> validateString(String trimmedValue) {
    return SupportedDietaryRestriction.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedDietaryRestriction> getAllValues() {
    return SupportedDietaryRestriction.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedDietaryRestriction enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return true;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Dietary restriction must be one of the supported dietary restrictions: ";
  }

  @Override
  public Class<SupportedDietaryRestriction> getEnumClass() {
    return SupportedDietaryRestriction.class;
  }
}
