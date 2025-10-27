package com.vallexia.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom Vallexia application exceptions.
 * Provides consistent error handling with error codes and HTTP status mapping.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Getter
public abstract class VallexiaException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  
  private final ErrorCode errorCode;
  private final HttpStatus httpStatus;
  
  /**
   * Constructs a new VallexiaException with the specified error code and message.
   * 
   * @param errorCode the error code
   * @param message the detail message
   */
  protected VallexiaException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = errorCode.getHttpStatus();
  }
  
  /**
   * Constructs a new VallexiaException with the specified error code, message, and cause.
   * 
   * @param errorCode the error code
   * @param message the detail message
   * @param cause the cause
   */
  protected VallexiaException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.httpStatus = errorCode.getHttpStatus();
  }
  
  /**
   * Gets the error code string.
   * 
   * @return the error code string
   */
  public String getCode() {
    return errorCode.getCode();
  }
}
