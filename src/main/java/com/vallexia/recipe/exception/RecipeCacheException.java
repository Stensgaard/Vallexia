package com.vallexia.recipe.exception;

/**
 * Exception thrown when recipe cache operations fail.
 * 
 * @since 2026-01-05
 */
public class RecipeCacheException extends RuntimeException {
    
    public RecipeCacheException(String message) {
        super(message);
    }
    
    public RecipeCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
