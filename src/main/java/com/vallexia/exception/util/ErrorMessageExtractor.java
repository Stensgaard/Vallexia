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
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class ErrorMessageExtractor {
  
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
    
    // Handle enum deserialization errors
    if (message.contains("Cannot deserialize value") && message.contains("Enum")) {
      // Extract enum values from message
      int enumStart = message.indexOf("[");
      int enumEnd = message.indexOf("]");
      if (enumStart > 0 && enumEnd > enumStart) {
        String enumValues = message.substring(enumStart + 1, enumEnd);
        // Extract the invalid value
        int valueStart = message.indexOf("from String \"");
        if (valueStart > 0) {
          int quoteStart = valueStart + 12; // Position of the opening quote
          int valueEnd = message.indexOf("\"", quoteStart + 1);
          if (valueEnd > quoteStart) {
            String invalidValue = message.substring(quoteStart + 1, valueEnd);
            return String.format(
                "Invalid enum value '%s'. Accepted values: %s",
                invalidValue, enumValues
            );
          }
        }
      }
    }
    
    // Handle generic JSON parse errors
    if (message.contains("JSON parse error")) {
      int errorStart = message.indexOf("JSON parse error:");
      if (errorStart >= 0) {
        String errorDetail = message.substring(errorStart + 16).trim();
        // Extract meaningful part before stack trace
        int detailEnd = errorDetail.indexOf("\n");
        if (detailEnd > 0) {
          errorDetail = errorDetail.substring(0, detailEnd);
        }
        return "Invalid JSON format: " + errorDetail;
      }
    }
    
    // Default: return sanitized message
    return "Invalid request body format. Please check your JSON syntax and data types.";
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
    
    ex.getBindingResult().getAllErrors().forEach((error) -> {
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
