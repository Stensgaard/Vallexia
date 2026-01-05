package com.vallexia.recipe.integration.exception;

/**
 * Exception thrown when Spoonacular API calls fail.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
public class SpoonacularApiException extends RuntimeException {
    
    public SpoonacularApiException(String message) {
        super(message);
    }
    
    public SpoonacularApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
