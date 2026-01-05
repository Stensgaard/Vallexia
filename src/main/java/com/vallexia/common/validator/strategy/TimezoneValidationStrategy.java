package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedTimezone} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class TimezoneValidationStrategy implements EnumValidationStrategy<SupportedTimezone> {

  @Override
  public Optional<SupportedTimezone> validateString(String trimmedValue) {
    return SupportedTimezone.fromValue(trimmedValue);
  }

  @Override
  public Stream<SupportedTimezone> getAllValues() {
    return SupportedTimezone.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedTimezone enumValue) {
    return enumValue.getValue();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Timezone must be one of the supported timezones: ";
  }

  @Override
  public Class<SupportedTimezone> getEnumClass() {
    return SupportedTimezone.class;
  }
}
