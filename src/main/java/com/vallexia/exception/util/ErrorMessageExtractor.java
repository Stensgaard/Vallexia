package com.vallexia.exception.util;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for extracting readable error messages from exceptions.
 * Provides user-friendly error message formatting for API responses.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-01
 */
public class ErrorMessageExtractor {
  
  private ErrorMessageExtractor() {
    // Utility class - prevent instantiation
  }
  
  /**
   * Extracts a readable error message from HttpMessageNotReadableException.
   * Provides user-friendly messages for common deserialization errors like enum mismatches.
   * 
   * @param ex the HTTP message not readable exception
   * @return user-friendly error message
   */
  public static String extractReadableErrorMessage(HttpMessageNotReadableException ex) {
    String message = ex.getMessage();
    
    if (message == null) {
      return "Invalid request body format";
    }
    
    String enumError = extractEnumErrorMessage(message);
    if (enumError != null) {
      return enumError;
    }
    
    String jsonError = extractJsonErrorMessage(message);
    if (jsonError != null) {
      return jsonError;
    }
    
    // Default: return sanitized message
    return "Invalid request body format. Please check your JSON syntax and data types.";
  }
  
  /**
   * Extracts error message for enum deserialization errors.
   * 
   * @param message the exception message
   * @return formatted error message or null if not an enum error
   */
  private static String extractEnumErrorMessage(String message) {
    if (!message.contains("Cannot deserialize value") || !message.contains("Enum")) {
      return null;
    }
    
    int enumStart = message.indexOf("[");
    int enumEnd = message.indexOf("]");
    if (enumStart <= 0 || enumEnd <= enumStart) {
      return null;
    }
    
    String enumValues = message.substring(enumStart + 1, enumEnd);
    int valueStart = message.indexOf("from String \"");
    if (valueStart <= 0) {
      return null;
    }
    
    int quoteStart = valueStart + 12;
    int valueEnd = message.indexOf("\"", quoteStart + 1);
    if (valueEnd <= quoteStart) {
      return null;
    }
    
    String invalidValue = message.substring(quoteStart + 1, valueEnd);
    return String.format("Invalid enum value '%s'. Accepted values: %s", invalidValue, enumValues);
  }
  
  /**
   * Extracts error message for JSON parse errors.
   * 
   * @param message the exception message
   * @return formatted error message or null if not a JSON parse error
   */
  private static String extractJsonErrorMessage(String message) {
    if (!message.contains("JSON parse error")) {
      return null;
    }
    
    int errorStart = message.indexOf("JSON parse error:");
    if (errorStart < 0) {
      return null;
    }
    
    String errorDetail = message.substring(errorStart + 16).trim();
    int detailEnd = errorDetail.indexOf("\n");
    if (detailEnd > 0) {
      errorDetail = errorDetail.substring(0, detailEnd);
    }
    return "Invalid JSON format: " + errorDetail;
  }
  
  /**
   * Extracts validation errors from MethodArgumentNotValidException.
   * Safely handles both FieldError and ObjectError types.
   * Combines multiple errors for the same field by joining messages.
   * Handles indexed field names from Set/List validation by extracting base field names.
   * 
   * @param ex the validation exception
   * @return map of field/object names to error messages
   */
  public static Map<String, String> extractValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = null;
      String errorMessage = error.getDefaultMessage();
      
      // FieldError extends ObjectError, so check FieldError first for more specific handling
      if (error instanceof FieldError) {
        FieldError fieldError = (FieldError) error;
        fieldName = fieldError.getField();
        
        // Extract base field name from indexed field names (e.g., "dislikedIngredients[0]" -> "dislikedIngredients")
        // This handles validation errors for Set/List elements with nested constraints
        if (fieldName != null && fieldName.contains("[")) {
          fieldName = fieldName.substring(0, fieldName.indexOf("["));
        }
      } else {
        // Handle ObjectError (non-field level errors)
        ObjectError objectError = (ObjectError) error;
        fieldName = objectError.getObjectName();
      }
      
      // Only add to errors map if we have a valid field name
      if (fieldName != null && !fieldName.isEmpty() && errorMessage != null) {
        // Combine multiple errors for the same field
        if (errors.containsKey(fieldName)) {
          errors.put(fieldName, errors.get(fieldName) + "; " + errorMessage);
        } else {
          errors.put(fieldName, errorMessage);
        }
      }
    });
    
    return errors;
  }
}
