package com.vallexia.auth.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for account disabled errors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
public class AccountDisabledException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new AccountDisabledException with the specified message.
   * 
   * @param message the detail message
   */
  public AccountDisabledException(String message) {
    super(ErrorCode.ACCOUNT_DISABLED, message);
  }
  
  /**
   * Constructs a new AccountDisabledException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public AccountDisabledException(String message, Throwable cause) {
    super(ErrorCode.ACCOUNT_DISABLED, message, cause);
  }
}
