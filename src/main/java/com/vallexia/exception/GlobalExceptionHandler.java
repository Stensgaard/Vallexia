package com.vallexia.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

import static com.vallexia.exception.util.ErrorMessageExtractor.extractReadableErrorMessage;
import static com.vallexia.exception.util.ErrorMessageExtractor.extractValidationErrors;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error handling, request tracing, and security-safe error messages.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
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
   * Handle HTTP message not readable exceptions (invalid JSON/enum deserialization).
   * Provides consistent error responses for malformed request bodies.
   * 
   * @param ex HttpMessageNotReadableException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    log.error("HTTP message not readable [requestId={}]: {}", requestId, ex.getMessage());
    
    String errorMessage = extractReadableErrorMessage(ex);
    Map<String, String> errors = new HashMap<>();
    errors.put("requestBody", errorMessage);
    
    ErrorResponseDto error = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.INVALID_INPUT,
        errors,
        request,
        requestId
    );
    
    return ResponseEntity.badRequest().body(error);
  }
  
  /**
   * Handle method argument type mismatch exceptions (invalid parameter types).
   * Provides consistent error responses for type conversion failures.
   * 
   * @param ex MethodArgumentTypeMismatchException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    log.error("Method argument type mismatch [requestId={}]: {}", requestId, ex.getMessage());
    
    String parameterName = ex.getName();
    Class<?> requiredType = ex.getRequiredType();
    Object value = ex.getValue();
    
    String expectedType = requiredType != null ? requiredType.getSimpleName() : "unknown";
    String actualValue = value != null ? value.toString() : "null";
    
    String errorMessage = String.format(
        "Invalid value for parameter '%s': '%s'. Expected type: %s",
        parameterName, actualValue, expectedType
    );
    
    // Provide specific message for date format errors
    if (requiredType != null && requiredType.getName().contains("LocalDateTime")) {
      errorMessage = String.format(
          "Invalid date format for parameter '%s': '%s'. Expected ISO 8601 format: YYYY-MM-DDTHH:mm:ss (e.g., 2024-01-01T00:00:00)",
          parameterName, actualValue
      );
    }
    
    Map<String, String> errors = new HashMap<>();
    errors.put(parameterName, errorMessage);
    
    ErrorResponseDto error = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.INVALID_INPUT,
        errors,
        request,
        requestId
    );
    
    return ResponseEntity.badRequest().body(error);
  }
  
  /**
   * Handle missing servlet request parameter exceptions.
   * Provides consistent error responses for missing required parameters.
   * 
   * @param ex MissingServletRequestParameterException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponseDto> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    log.error("Missing servlet request parameter [requestId={}]: {}", requestId, ex.getMessage());
    
    String parameterName = ex.getParameterName();
    String parameterType = ex.getParameterType();
    
    String errorMessage = String.format(
        "Required parameter '%s' of type '%s' is missing",
        parameterName, parameterType
    );
    
    Map<String, String> errors = new HashMap<>();
    errors.put(parameterName, errorMessage);
    
    ErrorResponseDto error = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.MISSING_REQUIRED_FIELD,
        errors,
        request,
        requestId
    );
    
    return ResponseEntity.badRequest().body(error);
  }
  
  /**
   * Handle illegal argument exceptions (validation errors).
   * Provides consistent error responses for invalid argument values.
   * 
   * @param ex IllegalArgumentException
   * @param request WebRequest
   * @return ErrorResponseDto
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    String requestId = errorResponseMapper.generateRequestId();
    log.error("Illegal argument [requestId={}]: {}", requestId, ex.getMessage());
    
    Map<String, String> errors = new HashMap<>();
    errors.put("argument", ex.getMessage());
    
    ErrorResponseDto error = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.VALIDATION_ERROR,
        errors,
        request,
        requestId
    );
    
    return ResponseEntity.badRequest().body(error);
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
}
