package com.vallexia.common.validator.strategy;

import com.vallexia.common.validator.EnumValidationStrategy;
import com.vallexia.nutrition.enums.GoalType;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link GoalType} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class GoalTypeValidationStrategy implements EnumValidationStrategy<GoalType> {

  @Override
  public Optional<GoalType> validateString(String trimmedValue) {
    return GoalType.fromCode(trimmedValue);
  }

  @Override
  public Stream<GoalType> getAllValues() {
    return Stream.of(GoalType.values());
  }

  @Override
  public String getDisplayValue(GoalType enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Goal type must be one of the supported goal types: ";
  }

  @Override
  public Class<GoalType> getEnumClass() {
    return GoalType.class;
  }
}
