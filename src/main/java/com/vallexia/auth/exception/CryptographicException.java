package com.vallexia.auth.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Exception thrown when cryptographic operations fail.
 * Used for errors related to hashing, encryption, or other cryptographic functions.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-08
 */
public class CryptographicException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new CryptographicException with the specified message.
   * 
   * @param message the detail message
   */
  public CryptographicException(String message) {
    super(ErrorCode.AUTHENTICATION_ERROR, message);
  }
  
  /**
   * Constructs a new CryptographicException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public CryptographicException(String message, Throwable cause) {
    super(ErrorCode.AUTHENTICATION_ERROR, message, cause);
  }
}
