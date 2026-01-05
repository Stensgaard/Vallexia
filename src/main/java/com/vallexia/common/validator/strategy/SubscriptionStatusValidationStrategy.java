package com.vallexia.common.validator.strategy;

import com.vallexia.common.validator.EnumValidationStrategy;
import com.vallexia.user.entity.enums.SubscriptionStatus;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validation strategy for {@link SubscriptionStatus} enum.
 *
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-22
 */
public class SubscriptionStatusValidationStrategy implements EnumValidationStrategy<SubscriptionStatus> {

  @Override
  public Optional<SubscriptionStatus> validateString(String trimmedValue) {
    return Arrays.stream(SubscriptionStatus.values())
        .filter(item -> item.name().equalsIgnoreCase(trimmedValue))
        .findFirst();
  }

  @Override
  public Stream<SubscriptionStatus> getAllValues() {
    return Arrays.stream(SubscriptionStatus.values());
  }

  @Override
  public String getDisplayValue(SubscriptionStatus enumValue) {
    return enumValue.name();
  }

  @Override
  public boolean supportsCollections() {
    return false;
  }

  @Override
  public String getErrorMessagePrefix() {
    return "Subscription status must be one of the supported subscription statuses: ";
  }

  @Override
  public Class<SubscriptionStatus> getEnumClass() {
    return SubscriptionStatus.class;
  }
}
