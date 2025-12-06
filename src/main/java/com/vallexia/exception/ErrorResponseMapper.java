package com.vallexia.exception;

import com.vallexia.security.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper for converting exceptions to ErrorResponseDto objects.
 * Centralizes error response building logic and provides consistent error formatting.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Component
public class ErrorResponseMapper {
  
  private final InputSanitizer inputSanitizer;
  
  /**
   * Constructor for dependency injection.
   * 
   * @param inputSanitizer the input sanitizer for cleaning error messages
   */
  public ErrorResponseMapper(InputSanitizer inputSanitizer) {
    this.inputSanitizer = inputSanitizer;
  }
  
  /**
   * Maps a VallexiaException to an ErrorResponseDto.
   * 
   * @param exception the exception to map
   * @param request the web request context
   * @param requestId unique request ID for tracing
   * @return ErrorResponseDto
   */
  public ErrorResponseDto toErrorResponse(
      VallexiaException exception, 
      WebRequest request, 
      String requestId) {
    
    String sanitizedMessage = inputSanitizer.sanitizeErrorMessage(exception.getMessage());
    
    return buildErrorResponse(
        exception.getCode(),
        sanitizedMessage,
        exception.getHttpStatus(),
        null,
        request,
        requestId
    );
  }
  
  /**
   * Maps a generic exception to an ErrorResponseDto with safe, generic message.
   * 
   * @param exception the exception to map
   * @param request the web request context
   * @param requestId unique request ID for tracing
   * @return ErrorResponseDto
   */
  public ErrorResponseDto toGenericErrorResponse(
      Exception exception,
      WebRequest request,
      String requestId) {
    
    return buildErrorResponse(
        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
        ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        null,
        request,
        requestId
    );
  }
  
  /**
   * Maps validation errors with field-level details.
   * 
   * @param errorCode the error code enum
   * @param validationErrors map of field names to error messages
   * @param request the web request context
   * @param requestId unique request ID for tracing
   * @return ErrorResponseDto
   */
  public ErrorResponseDto toValidationErrorResponse(
      ErrorCode errorCode,
      Map<String, String> validationErrors,
      WebRequest request,
      String requestId) {
    
    return buildErrorResponse(
        errorCode.getCode(),
        errorCode.getDefaultMessage(),
        errorCode.getHttpStatus(),
        validationErrors,
        request,
        requestId
    );
  }
  
  /**
   * Builds a simple error response for authentication entry points.
   * Used by AuthEntryPointJwt to provide consistent error responses.
   * 
   * @param errorCode the error code enum
   * @param path the request path
   * @param requestId unique request ID for tracing
   * @return ErrorResponseDto
   */
  public ErrorResponseDto toAuthenticationErrorResponse(
      ErrorCode errorCode,
      String path,
      String requestId) {
    
    return ErrorResponseDto.builder()
        .code(errorCode.getCode())
        .message(errorCode.getDefaultMessage())
        .httpStatus(errorCode.getHttpStatus().value())
        .details(null)
        .timestamp(LocalDateTime.now())
        .path(path)
        .requestId(requestId)
        .build();
  }
  
  /**
   * Builds a standardized error response DTO.
   * 
   * @param code error code
   * @param message error message
   * @param httpStatus HTTP status
   * @param details additional error details
   * @param request web request for extracting path
   * @param requestId unique request ID
   * @return ErrorResponseDto
   */
  private ErrorResponseDto buildErrorResponse(
      String code,
      String message,
      HttpStatus httpStatus,
      Map<String, String> details,
      WebRequest request,
      String requestId) {
    
    String path = extractPath(request);
    
    return ErrorResponseDto.builder()
        .code(code)
        .message(message)
        .httpStatus(httpStatus.value())
        .details(details)
        .timestamp(LocalDateTime.now())
        .path(path)
        .requestId(requestId)
        .build();
  }
  
  /**
   * Extracts the request path from WebRequest.
   * 
   * @param request the web request
   * @return the request path
   */
  private String extractPath(WebRequest request) {
    String description = request.getDescription(false);
    return description.replace("uri=", "");
  }
  
  /**
   * Generates a unique request ID for tracing.
   * 
   * @return unique request ID
   */
  public String generateRequestId() {
    return UUID.randomUUID().toString();
  }
}
