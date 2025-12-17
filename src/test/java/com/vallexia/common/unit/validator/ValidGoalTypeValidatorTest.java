package com.vallexia.common.unit.validator;

import com.vallexia.common.validator.ValidGoalTypeValidator;
import com.vallexia.nutrition.enums.GoalType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ValidGoalTypeValidator.
 * Tests goal type validation with null safety, enum instances, string codes, and type checking.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidGoalTypeValidator Unit Tests")
class ValidGoalTypeValidatorTest {

  private ValidGoalTypeValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ValidGoalTypeValidator();
    validator.initialize(null);
  }

  /**
   * Creates a mocked ConstraintValidatorContext for testing validation failures.
   * 
   * @return a mocked context with proper method chaining setup
   */
  private ConstraintValidatorContext createMockContext() {
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    ConstraintValidatorContext.ConstraintViolationBuilder builder = 
        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    
    doNothing().when(context).disableDefaultConstraintViolation();
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
    doReturn(context).when(builder).addConstraintViolation();
    
    return context;
  }

  // ==================== Null and Empty Value Tests ====================

  @Test
  @DisplayName("Should allow null values")
  void shouldAllowNullValues() {
    // When/Then
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  @DisplayName("Should allow empty or whitespace-only strings")
  void shouldAllowEmptyOrWhitespaceStrings() {
    // When/Then
    assertThat(validator.isValid("", null)).isTrue();
    assertThat(validator.isValid("   ", null)).isTrue();
  }

  // ==================== Enum Instance Tests ====================

  @Test
  @DisplayName("Should accept supported goal type enum instances")
  void shouldAcceptSupportedGoalTypeEnumInstances() {
    // When/Then
    assertThat(validator.isValid(GoalType.WEIGHT_LOSS, null)).isTrue();
    assertThat(validator.isValid(GoalType.WEIGHT_GAIN, null)).isTrue();
    assertThat(validator.isValid(GoalType.MUSCLE_GAIN, null)).isTrue();
    assertThat(validator.isValid(GoalType.MAINTENANCE, null)).isTrue();
    assertThat(validator.isValid(GoalType.ATHLETIC_PERFORMANCE, null)).isTrue();
    assertThat(validator.isValid(GoalType.GENERAL_HEALTH, null)).isTrue();
  }

  // ==================== String Code Tests ====================

  @Test
  @DisplayName("Should accept supported goal type codes case-insensitively")
  void shouldAcceptSupportedGoalTypeCodes() {
    // When/Then
    assertThat(validator.isValid("WEIGHT_LOSS", null)).isTrue();
    assertThat(validator.isValid("weight_loss", null)).isTrue();
    assertThat(validator.isValid("MAINTENANCE", null)).isTrue();
    assertThat(validator.isValid("maintenance", null)).isTrue();
    assertThat(validator.isValid("MUSCLE_GAIN", null)).isTrue();
    assertThat(validator.isValid("muscle_gain", null)).isTrue();
  }

  @Test
  @DisplayName("Should trim goal type codes before validation")
  void shouldTrimGoalTypeCodesBeforeValidation() {
    // When/Then
    assertThat(validator.isValid("  WEIGHT_LOSS  ", null)).isTrue();
    assertThat(validator.isValid("\tMAINTENANCE\n", null)).isTrue();
  }

  @Test
  @DisplayName("Should reject unknown or invalid goal type codes")
  void shouldRejectUnknownGoalTypeCodes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid("INVALID", context)).isFalse();
    assertThat(validator.isValid("CUSTOM_GOAL", context)).isFalse();
    assertThat(validator.isValid("WEIGHT", context)).isFalse();
  }

  // ==================== Type Validation Tests ====================

  @Test
  @DisplayName("Should reject non-string and non-enum types")
  void shouldRejectNonStringAndNonEnumTypes() {
    // Given
    ConstraintValidatorContext context = createMockContext();
    
    // When/Then
    assertThat(validator.isValid(123, context)).isFalse();
    assertThat(validator.isValid(true, context)).isFalse();
    assertThat(validator.isValid(new Object(), context)).isFalse();
  }
}
