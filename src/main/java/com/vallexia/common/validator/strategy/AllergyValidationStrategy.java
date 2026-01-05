package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedAllergy} enum.
 * Supports collection validation.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class AllergyValidationStrategy implements EnumValidationStrategy<SupportedAllergy> {

  @Override
  public Optional<SupportedAllergy> validateString(String trimmedValue) {
    return SupportedAllergy.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedAllergy> getAllValues() {
    return SupportedAllergy.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedAllergy enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return true;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Allergy must be one of the supported allergies: ";
  }

  @Override
  public Class<SupportedAllergy> getEnumClass() {
    return SupportedAllergy.class;
  }
}
