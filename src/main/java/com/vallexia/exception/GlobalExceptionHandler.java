package com.vallexia.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error handling, request tracing, and security-safe error messages.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
  
  private final ErrorResponseMapper errorResponseMapper;
  
  /**
   * Constructor for dependency injection.
   * 
   * @param errorResponseMapper the error response mapper
   */
  public GlobalExceptionHandler(ErrorResponseMapper errorResponseMapper) {
    this.errorResponseMapper = errorResponseMapper;
  }
  
  /**
   * Handle all VallexiaException subclasses.
   * 
   * @param ex VallexiaException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(VallexiaException.class)
  public ResponseEntity<ErrorResponseDto> handleVallexiaException(
      VallexiaException ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    log.error("VallexiaException [requestId={}]: {}", requestId, ex.getMessage());
    
    ErrorResponseDto error = errorResponseMapper.toErrorResponse(ex, request, requestId);
    
    return ResponseEntity.status(ex.getHttpStatus()).body(error);
  }
  
  /**
   * Handle validation exceptions.
   * Note: ValidationException extends VallexiaException, so this handler
   * provides more specific handling if needed. Currently delegates to base handler.
   * 
   * @param ex ValidationException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationException(
      ValidationException ex, WebRequest request) {
    // Delegate to VallexiaException handler for consistent processing
    return handleVallexiaException(ex, request);
  }
  
  /**
   * Handle method argument not valid exceptions (validation errors).
   * Fixed to properly handle both FieldError and ObjectError types.
   * 
   * @param ex MethodArgumentNotValidException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    log.error("Method argument validation error [requestId={}]: {}", requestId, ex.getMessage());
    
    Map<String, String> errors = extractValidationErrors(ex);
    
    ErrorResponseDto error = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.VALIDATION_ERROR,
        errors,
        request,
        requestId
    );
    
    return ResponseEntity.badRequest().body(error);
  }
  
  /**
   * Handle access denied exceptions from Spring Security.
   * Provides consistent error responses for unauthorized access attempts.
   * 
   * @param ex AccessDeniedException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
      AccessDeniedException ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    log.error("Access denied [requestId={}]: {}", requestId, ex.getMessage());
    
    ErrorResponseDto error = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.ACCESS_DENIED,
        null,
        request,
        requestId
    );
    
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }
  
  /**
   * Handle generic exceptions.
   * Provides security-safe error messages without leaking internal details.
   * 
   * @param ex Exception
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGenericException(
      Exception ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    
    // Log full details including stack trace for internal debugging
    log.error("Unexpected error [requestId={}]: {}", requestId, ex.getMessage(), ex);
    
    // Return sanitized generic message to client
    ErrorResponseDto error = errorResponseMapper.toGenericErrorResponse(ex, request, requestId);
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
  
  /**
   * Extracts validation errors from MethodArgumentNotValidException.
   * Safely handles both FieldError and ObjectError types.
   * 
   * @param ex the validation exception
   * @return map of field/object names to error messages
   */
  private Map<String, String> extractValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      // Safe type checking before casting
      if (error instanceof FieldError) {
        FieldError fieldError = (FieldError) error;
        String fieldName = fieldError.getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
      } else if (error instanceof ObjectError) {
        ObjectError objectError = (ObjectError) error;
        String objectName = objectError.getObjectName();
        String errorMessage = error.getDefaultMessage();
        errors.put(objectName, errorMessage);
      }
    });
    
    return errors;
  }
}
