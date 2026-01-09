package com.vallexia.store.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for store flyer scraping errors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-01-XX
 */
public class StoreScrapingException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new StoreScrapingException with the specified message.
   * 
   * @param message the detail message
   */
  public StoreScrapingException(String message) {
    super(ErrorCode.STORE_SCRAPING_ERROR, message);
  }
  
  /**
   * Constructs a new StoreScrapingException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public StoreScrapingException(String message, Throwable cause) {
    super(ErrorCode.STORE_SCRAPING_ERROR, message, cause);
  }
}


