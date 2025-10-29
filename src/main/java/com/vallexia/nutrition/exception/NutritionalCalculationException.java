package com.vallexia.nutrition.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a nutritional calculation fails.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class NutritionalCalculationException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new NutritionalCalculationException with the specified detail message.
   * 
   * @param message the detail message
   */
  public NutritionalCalculationException(String message) {
    super(ErrorCode.NUTRITIONAL_CALCULATION_ERROR, message);
  }
  
  /**
   * Constructs a new NutritionalCalculationException with the specified detail message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public NutritionalCalculationException(String message, Throwable cause) {
    super(ErrorCode.NUTRITIONAL_CALCULATION_ERROR, message, cause);
  }
}
