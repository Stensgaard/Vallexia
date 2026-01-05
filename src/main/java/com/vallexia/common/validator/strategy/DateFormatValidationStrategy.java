package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedDateFormat} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class DateFormatValidationStrategy implements EnumValidationStrategy<SupportedDateFormat> {

  @Override
  public Optional<SupportedDateFormat> validateString(String trimmedValue) {
    return SupportedDateFormat.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedDateFormat> getAllValues() {
    return SupportedDateFormat.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedDateFormat enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Date format must be one of the supported formats: ";
  }

  @Override
  public Class<SupportedDateFormat> getEnumClass() {
    return SupportedDateFormat.class;
  }
}
