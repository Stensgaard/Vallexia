package com.vallexia.user.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for user not found errors.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class UserNotFoundException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new UserNotFoundException with the specified message.
   * 
   * @param message the detail message
   */
  public UserNotFoundException(String message) {
    super(ErrorCode.USER_NOT_FOUND, message);
  }
  
  /**
   * Constructs a new UserNotFoundException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public UserNotFoundException(String message, Throwable cause) {
    super(ErrorCode.USER_NOT_FOUND, message, cause);
  }
}

