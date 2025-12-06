package com.vallexia.recipe.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for recipe not found errors.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-14
 */
public class RecipeNotFoundException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new RecipeNotFoundException with the specified message.
   * 
   * @param message the detail message
   */
  public RecipeNotFoundException(String message) {
    super(ErrorCode.RECIPE_NOT_FOUND, message);
  }
  
  /**
   * Constructs a new RecipeNotFoundException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public RecipeNotFoundException(String message, Throwable cause) {
    super(ErrorCode.RECIPE_NOT_FOUND, message, cause);
  }
}
