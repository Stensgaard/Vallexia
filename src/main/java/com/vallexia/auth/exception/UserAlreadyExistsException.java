package com.vallexia.auth.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for user already exists errors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
public class UserAlreadyExistsException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new UserAlreadyExistsException with the specified message.
   * 
   * @param message the detail message
   */
  public UserAlreadyExistsException(String message) {
    super(ErrorCode.USER_ALREADY_EXISTS, message);
  }
  
  /**
   * Constructs a new UserAlreadyExistsException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public UserAlreadyExistsException(String message, Throwable cause) {
    super(ErrorCode.USER_ALREADY_EXISTS, message, cause);
  }
}
