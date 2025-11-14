package com.vallexia.recipe.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for recipe validation errors.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RecipeValidationException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new RecipeValidationException with the specified message.
   * 
   * @param message the detail message
   */
  public RecipeValidationException(String message) {
    super(ErrorCode.RECIPE_VALIDATION_ERROR, message);
  }
  
  /**
   * Constructs a new RecipeValidationException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public RecipeValidationException(String message, Throwable cause) {
    super(ErrorCode.RECIPE_VALIDATION_ERROR, message, cause);
  }
}
