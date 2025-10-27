package com.vallexia.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Immutable Data Transfer Object for error responses.
 * Provides consistent error information to API clients.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
  
  /**
   * Error code for categorizing the error.
   */
  String code;
  
  /**
   * Human-readable error message.
   */
  String message;
  
  /**
   * HTTP status code.
   */
  Integer httpStatus;
  
  /**
   * Additional error details (e.g., validation errors).
   */
  Map<String, String> details;
  
  /**
   * Timestamp when the error occurred.
   */
  LocalDateTime timestamp;
  
  /**
   * Request path where the error occurred.
   */
  String path;
  
  /**
   * Unique request ID for tracing.
   */
  String requestId;
}
