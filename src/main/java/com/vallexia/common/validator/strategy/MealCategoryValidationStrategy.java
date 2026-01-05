package com.vallexia.common.validator.strategy;

import com.vallexia.common.enums.SupportedMealCategory;
import com.vallexia.common.validator.EnumValidationStrategy;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SupportedMealCategory} enum.
 * Supports collection validation.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class MealCategoryValidationStrategy implements EnumValidationStrategy<SupportedMealCategory> {

  @Override
  public Optional<SupportedMealCategory> validateString(String trimmedValue) {
    return SupportedMealCategory.fromCode(trimmedValue);
  }

  @Override
  public Stream<SupportedMealCategory> getAllValues() {
    return SupportedMealCategory.getAll().stream();
  }

  @Override
  public String getDisplayValue(SupportedMealCategory enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return true;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Meal category must be one of the supported meal categories: ";
  }

  @Override
  public Class<SupportedMealCategory> getEnumClass() {
    return SupportedMealCategory.class;
  }
}
