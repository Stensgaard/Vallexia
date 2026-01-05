package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedFirstDayOfWeek} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class FirstDayOfWeekValidationStrategy implements EnumValidationStrategy<SupportedFirstDayOfWeek> {

  @Override
  public Optional<SupportedFirstDayOfWeek> validateString(String trimmedValue) {
    return SupportedFirstDayOfWeek.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedFirstDayOfWeek> getAllValues() {
    return SupportedFirstDayOfWeek.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedFirstDayOfWeek enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "First day of week must be one of the supported options: ";
  }

  @Override
  public Class<SupportedFirstDayOfWeek> getEnumClass() {
    return SupportedFirstDayOfWeek.class;
  }
}
