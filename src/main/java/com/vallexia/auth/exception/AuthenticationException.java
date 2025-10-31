package com.vallexia.auth.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for authentication errors.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class AuthenticationException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new AuthenticationException with the specified message.
   * 
   * @param message the detail message
   */
  public AuthenticationException(String message) {
    super(ErrorCode.AUTHENTICATION_ERROR, message);
  }
  
  /**
   * Constructs a new AuthenticationException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public AuthenticationException(String message, Throwable cause) {
    super(ErrorCode.AUTHENTICATION_ERROR, message, cause);
  }
  
  /**
   * Constructs a new AuthenticationException with a custom error code and message.
   * 
   * @param errorCode the specific error code to use
   * @param message the detail message
   */
  public AuthenticationException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}

