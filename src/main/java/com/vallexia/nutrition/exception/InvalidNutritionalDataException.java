package com.vallexia.nutrition.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when nutritional data is invalid or outside acceptable ranges.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidNutritionalDataException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new InvalidNutritionalDataException with the specified detail message.
   * 
   * @param message the detail message
   */
  public InvalidNutritionalDataException(String message) {
    super(ErrorCode.INVALID_NUTRITIONAL_DATA, message);
  }
  
  /**
   * Constructs a new InvalidNutritionalDataException with the specified detail message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public InvalidNutritionalDataException(String message, Throwable cause) {
    super(ErrorCode.INVALID_NUTRITIONAL_DATA, message, cause);
  }
}
