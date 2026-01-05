package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedMeasurementSystem} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class MeasurementSystemValidationStrategy implements EnumValidationStrategy<SupportedMeasurementSystem> {

  @Override
  public Optional<SupportedMeasurementSystem> validateString(String trimmedValue) {
    return SupportedMeasurementSystem.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedMeasurementSystem> getAllValues() {
    return SupportedMeasurementSystem.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedMeasurementSystem enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Measurement system must be one of the supported systems: ";
  }

  @Override
  public Class<SupportedMeasurementSystem> getEnumClass() {
    return SupportedMeasurementSystem.class;
  }
}
