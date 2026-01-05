package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedCountry} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class CountryValidationStrategy implements EnumValidationStrategy<SupportedCountry> {

  @Override
  public Optional<SupportedCountry> validateString(String trimmedValue) {
    return SupportedCountry.fromCountry(trimmedValue);
  }

  @Override
  public Stream<SupportedCountry> getAllValues() {
    return SupportedCountry.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedCountry enumValue) {
    return enumValue.getCountryCode();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Country must be one of the supported countries: ";
  }

  @Override
  public Class<SupportedCountry> getEnumClass() {
    return SupportedCountry.class;
  }
}
