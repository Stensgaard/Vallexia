package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedCurrency} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class CurrencyValidationStrategy implements EnumValidationStrategy<SupportedCurrency> {

  @Override
  public Optional<SupportedCurrency> validateString(String trimmedValue) {
    return SupportedCurrency.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedCurrency> getAllValues() {
    return SupportedCurrency.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedCurrency enumValue) {
    return enumValue.getCode();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Currency must be one of the supported currencies: ";
  }

  @Override
  public Class<SupportedCurrency> getEnumClass() {
    return SupportedCurrency.class;
  }
}
