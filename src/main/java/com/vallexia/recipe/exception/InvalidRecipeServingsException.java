package com.vallexia.recipe.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for invalid recipe servings errors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
public class InvalidRecipeServingsException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new InvalidRecipeServingsException with the specified message.
   * 
   * @param message the detail message
   */
  public InvalidRecipeServingsException(String message) {
    super(ErrorCode.INVALID_RECIPE_SERVINGS, message);
  }
  
  /**
   * Constructs a new InvalidRecipeServingsException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public InvalidRecipeServingsException(String message, Throwable cause) {
    super(ErrorCode.INVALID_RECIPE_SERVINGS, message, cause);
  }
}
