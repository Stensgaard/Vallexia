package com.vallexia.exception;

/**
 * Custom exception for validation errors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
public class ValidationException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new ValidationException with the specified message.
   * 
   * @param message the detail message
   */
  public ValidationException(String message) {
    super(ErrorCode.VALIDATION_ERROR, message);
  }
  
  /**
   * Constructs a new ValidationException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public ValidationException(String message, Throwable cause) {
    super(ErrorCode.VALIDATION_ERROR, message, cause);
  }
}
