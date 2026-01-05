package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedLocale} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class LocaleValidationStrategy implements EnumValidationStrategy<SupportedLocale> {

  @Override
  public Optional<SupportedLocale> validateString(String trimmedValue) {
    return SupportedLocale.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedLocale> getAllValues() {
    return SupportedLocale.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedLocale enumValue) {
    return enumValue.getCode();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Locale must be one of the supported locales: ";
  }

  @Override
  public Class<SupportedLocale> getEnumClass() {
    return SupportedLocale.class;
  }
}
