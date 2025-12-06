package com.vallexia.auth.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for account locked errors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
public class AccountLockedException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new AccountLockedException with the specified message.
   * 
   * @param message the detail message
   */
  public AccountLockedException(String message) {
    super(ErrorCode.ACCOUNT_LOCKED, message);
  }
  
  /**
   * Constructs a new AccountLockedException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public AccountLockedException(String message, Throwable cause) {
    super(ErrorCode.ACCOUNT_LOCKED, message, cause);
  }
}
