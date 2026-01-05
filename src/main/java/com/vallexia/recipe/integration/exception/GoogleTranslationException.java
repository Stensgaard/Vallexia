package com.vallexia.recipe.integration.exception;

/**
 * Exception thrown when Google Cloud Translation API calls fail.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
public class GoogleTranslationException extends RuntimeException {
    
    public GoogleTranslationException(String message) {
        super(message);
    }
    
    public GoogleTranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
