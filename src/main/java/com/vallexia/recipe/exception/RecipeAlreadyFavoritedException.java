package com.vallexia.recipe.exception;

import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.VallexiaException;

/**
 * Custom exception for recipe already favorited errors.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
public class RecipeAlreadyFavoritedException extends VallexiaException {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructs a new RecipeAlreadyFavoritedException with the specified message.
   * 
   * @param message the detail message
   */
  public RecipeAlreadyFavoritedException(String message) {
    super(ErrorCode.RECIPE_ALREADY_FAVORITED, message);
  }
  
  /**
   * Constructs a new RecipeAlreadyFavoritedException with the specified message and cause.
   * 
   * @param message the detail message
   * @param cause the cause
   */
  public RecipeAlreadyFavoritedException(String message, Throwable cause) {
    super(ErrorCode.RECIPE_ALREADY_FAVORITED, message, cause);
  }
}
