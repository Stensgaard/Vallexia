package com.vallexia.exception.unit.util;

import com.vallexia.exception.util.ErrorMessageExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ErrorMessageExtractor.
 * Tests error message extraction logic for various exception types.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-01
 */
@DisplayName("ErrorMessageExtractor Unit Tests")
class ErrorMessageExtractorTest {
  
  // ==================== extractReadableErrorMessage() Tests ====================
  
  @Test
  @DisplayName("Should return default message when exception message is null")
  void shouldReturnDefaultMessageWhenExceptionMessageIsNull() {
    // Given
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn(null);
    
    // When
    String result = ErrorMessageExtractor.extractReadableErrorMessage(ex);
    
    // Then
    assertThat(result).isEqualTo("Invalid request body format");
  }
  
  @Test
  @DisplayName("Should extract enum deserialization error message with invalid value and accepted values")
  void shouldExtractEnumDeserializationErrorMessage() {
    // Given
    String errorMessage = "Cannot deserialize value of type `com.vallexia.user.entity.enums.GoalType` " +
        "from String \"MAINTAIN\": not one of the values accepted for Enum class: " +
        "[ATHLETIC_PERFORMANCE, MAINTENANCE, WEIGHT_GAIN, WEIGHT_LOSS, MUSCLE_GAIN, GENERAL_HEALTH]";
    
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn(errorMessage);
    
    // When
    String result = ErrorMessageExtractor.extractReadableErrorMessage(ex);
    
    // Then
    assertThat(result).contains("Invalid enum value 'MAINTAIN'");
    assertThat(result).contains("Accepted values:");
    assertThat(result).contains("MAINTENANCE");
    assertThat(result).contains("WEIGHT_GAIN");
  }
  
  @Test
  @DisplayName("Should extract enum error with single accepted value")
  void shouldExtractEnumErrorWithSingleAcceptedValue() {
    // Given
    String errorMessage = "Cannot deserialize value of type `TestEnum` " +
        "from String \"INVALID\": not one of the values accepted for Enum class: [VALID]";
    
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn(errorMessage);
    
    // When
    String result = ErrorMessageExtractor.extractReadableErrorMessage(ex);
    
    // Then
    assertThat(result).contains("Invalid enum value 'INVALID'");
    assertThat(result).contains("VALID");
  }
  
  @Test
  @DisplayName("Should return generic JSON parse error message")
  void shouldReturnGenericJsonParseErrorMessage() {
    // Given
    String errorMessage = "JSON parse error: Unexpected character ('@' (code 64)): " +
        "was expecting a comma or closing brace\n" +
        "at [Source: (String)\"...\"; line: 5, column: 12]";
    
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn(errorMessage);
    
    // When
    String result = ErrorMessageExtractor.extractReadableErrorMessage(ex);
    
    // Then
    assertThat(result).startsWith("Invalid JSON format:");
    assertThat(result).contains("Unexpected character");
  }
  
  @Test
  @DisplayName("Should handle JSON parse error without newline")
  void shouldHandleJsonParseErrorWithoutNewline() {
    // Given
    String errorMessage = "JSON parse error: Unexpected end-of-input";
    
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn(errorMessage);
    
    // When
    String result = ErrorMessageExtractor.extractReadableErrorMessage(ex);
    
    // Then
    assertThat(result).startsWith("Invalid JSON format:");
    assertThat(result).contains("Unexpected end-of-input");
  }
  
  @Test
  @DisplayName("Should return default message for unrecognized error format")
  void shouldReturnDefaultMessageForUnrecognizedErrorFormat() {
    // Given
    String errorMessage = "Some unexpected error format that doesn't match known patterns";
    
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn(errorMessage);
    
    // When
    String result = ErrorMessageExtractor.extractReadableErrorMessage(ex);
    
    // Then
    assertThat(result).isEqualTo("Invalid request body format. Please check your JSON syntax and data types.");
  }
  
  @Test
  @DisplayName("Should handle enum error with missing enum values in message")
  void shouldHandleEnumErrorWithMissingEnumValues() {
    // Given
    String errorMessage = "Cannot deserialize value of type `TestEnum` from String \"INVALID\"";
    
    HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
    when(ex.getMessage()).thenReturn(errorMessage);
    
    // When
    String result = ErrorMessageExtractor.extractReadableErrorMessage(ex);
    
    // Then
    assertThat(result).isEqualTo("Invalid request body format. Please check your JSON syntax and data types.");
  }
  
  // ==================== extractValidationErrors() Tests ====================
  
  @Test
  @DisplayName("Should extract single field error")
  void shouldExtractSingleFieldError() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "email", null, false, 
        new String[]{"NotBlank.email"}, new Object[0], "Email is required"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(1);
    assertThat(errors).containsKey("email");
    assertThat(errors.get("email")).isEqualTo("Email is required");
  }
  
  // Helper class for testing
  @SuppressWarnings("unused")
  private static class TestObject {
    private String email;
    private String password;
    private String username;
  }
  
  // Helper method to create mock MethodParameter
  private MethodParameter createMockMethodParameter() {
    return mock(MethodParameter.class);
  }
  
  @Test
  @DisplayName("Should extract multiple field errors")
  void shouldExtractMultipleFieldErrors() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "email", null, false, 
        new String[]{"NotBlank.email"}, new Object[0], "Email is required"));
    bindingResult.addError(new FieldError("testObject", "password", null, false, 
        new String[]{"Size.password"}, new Object[0], "Password must be at least 8 characters"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(2);
    assertThat(errors).containsKey("email");
    assertThat(errors).containsKey("password");
    assertThat(errors.get("email")).isEqualTo("Email is required");
    assertThat(errors.get("password")).isEqualTo("Password must be at least 8 characters");
  }
  
  @Test
  @DisplayName("Should extract indexed field names and extract base field name")
  void shouldExtractIndexedFieldNamesAndExtractBaseFieldName() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "preferredCuisines[0]", "", false, 
        new String[]{"NotNull.preferredCuisines[]"}, new Object[0], "Preferred cuisine cannot be null"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(1);
    assertThat(errors).containsKey("preferredCuisines");
    assertThat(errors).doesNotContainKey("preferredCuisines[0]");
    assertThat(errors.get("preferredCuisines")).isEqualTo("Preferred cuisine cannot be null");
  }
  
  @Test
  @DisplayName("Should combine multiple errors for the same field")
  void shouldCombineMultipleErrorsForSameField() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "username", "ab", false, 
        new String[]{"Size.username"}, new Object[0], "Username must be between 3 and 20 characters"));
    bindingResult.addError(new FieldError("testObject", "username", "ab", false, 
        new String[]{"Pattern.username"}, new Object[0], "Username must contain only letters, numbers, underscores, and dashes"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(1);
    assertThat(errors).containsKey("username");
    assertThat(errors.get("username")).contains("Username must be between 3 and 20 characters");
    assertThat(errors.get("username")).contains("Username must contain only letters, numbers, underscores, and dashes");
    assertThat(errors.get("username")).contains("; ");
  }
  
  @Test
  @DisplayName("Should extract ObjectError for non-field level errors")
  void shouldExtractObjectErrorForNonFieldLevelErrors() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new ObjectError("testObject", 
        new String[]{"CustomValidator.testObject"}, new Object[0], "Custom validation failed"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(1);
    assertThat(errors).containsKey("testObject");
    assertThat(errors.get("testObject")).isEqualTo("Custom validation failed");
  }
  
  @Test
  @DisplayName("Should handle empty error list")
  void shouldHandleEmptyErrorList() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).isEmpty();
  }
  
  @Test
  @DisplayName("Should skip errors with null field names")
  void shouldSkipErrorsWithNullFieldNames() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "email", null, false, 
        new String[]{"NotBlank.email"}, new Object[0], "Email is required"));
    
    // Create a mock FieldError with null field name
    FieldError nullFieldError = mock(FieldError.class);
    when(nullFieldError.getField()).thenReturn(null);
    when(nullFieldError.getDefaultMessage()).thenReturn("Some message");
    
    BindingResult mockBindingResult = mock(BindingResult.class);
    when(mockBindingResult.getAllErrors()).thenReturn(
        java.util.List.of(bindingResult.getFieldError("email"), nullFieldError)
    );
    
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    when(ex.getBindingResult()).thenReturn(mockBindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(1);
    assertThat(errors).containsKey("email");
    assertThat(errors.get("email")).isEqualTo("Email is required");
  }
  
  @Test
  @DisplayName("Should skip errors with null error messages")
  void shouldSkipErrorsWithNullErrorMessages() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "email", null, false, 
        new String[]{"NotBlank.email"}, new Object[0], "Email is required"));
    
    // Create a FieldError with null message using reflection
    FieldError nullMessageError = new FieldError("testObject", "password", null, false, 
        new String[]{"NotBlank.password"}, new Object[0], "");
    ReflectionTestUtils.setField(nullMessageError, "defaultMessage", (String) null);
    
    BindingResult mockBindingResult = mock(BindingResult.class);
    when(mockBindingResult.getAllErrors()).thenReturn(
        java.util.List.of(bindingResult.getFieldError("email"), nullMessageError)
    );
    
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    when(ex.getBindingResult()).thenReturn(mockBindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(1);
    assertThat(errors).containsKey("email");
    assertThat(errors).doesNotContainKey("password");
  }
  
  @Test
  @DisplayName("Should handle field error with empty field name")
  void shouldHandleFieldErrorWithEmptyFieldName() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "", null, false, 
        new String[]{"NotBlank"}, new Object[0], "Some error"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).isEmpty();
  }
  
  @Test
  @DisplayName("Should handle multiple indexed fields correctly")
  void shouldHandleMultipleIndexedFieldsCorrectly() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "allergies[0]", "", false, 
        new String[]{"NotBlank"}, new Object[0], "Allergy cannot be blank"));
    bindingResult.addError(new FieldError("testObject", "allergies[1]", "", false, 
        new String[]{"Size"}, new Object[0], "Allergy name too long"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(1);
    assertThat(errors).containsKey("allergies");
    assertThat(errors.get("allergies")).contains("Allergy cannot be blank");
    assertThat(errors.get("allergies")).contains("Allergy name too long");
    assertThat(errors.get("allergies")).contains("; ");
  }
  
  @Test
  @DisplayName("Should handle mixed FieldError and ObjectError")
  void shouldHandleMixedFieldErrorAndObjectError() {
    // Given
    TestObject testObject = new TestObject();
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(testObject, "testObject");
    bindingResult.addError(new FieldError("testObject", "email", null, false, 
        new String[]{"NotBlank.email"}, new Object[0], "Email is required"));
    bindingResult.addError(new ObjectError("testObject", 
        new String[]{"CustomValidator"}, new Object[0], "Custom validation failed"));
    
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(createMockMethodParameter(), bindingResult);
    
    // When
    Map<String, String> errors = ErrorMessageExtractor.extractValidationErrors(ex);
    
    // Then
    assertThat(errors).hasSize(2);
    assertThat(errors).containsKey("email");
    assertThat(errors).containsKey("testObject");
    assertThat(errors.get("email")).isEqualTo("Email is required");
    assertThat(errors.get("testObject")).isEqualTo("Custom validation failed");
  }
}
