package com.vallexia.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of standardized error codes used throughout the application.
 * Each error code maps to an HTTP status code and provides a consistent
 * error code string for client consumption.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Getter
public enum ErrorCode {
  
  // Authentication & Authorization Errors (4xx)
  AUTHENTICATION_ERROR("AUTH_001", HttpStatus.UNAUTHORIZED, "Authentication failed"),
  INVALID_CREDENTIALS("AUTH_002", HttpStatus.UNAUTHORIZED, "Invalid username or password"),
  TOKEN_EXPIRED("AUTH_003", HttpStatus.UNAUTHORIZED, "Authentication token has expired"),
  INVALID_TOKEN("AUTH_004", HttpStatus.UNAUTHORIZED, "Invalid authentication token"),
  ACCESS_DENIED("AUTH_005", HttpStatus.FORBIDDEN, "You do not have permission to access this resource"),
  ACCOUNT_LOCKED("AUTH_006", HttpStatus.LOCKED, "Account is temporarily locked due to multiple failed login attempts"),
  ACCOUNT_DISABLED("AUTH_007", HttpStatus.FORBIDDEN, "Account is disabled"),
  
  // User Management Errors (4xx)
  USER_NOT_FOUND("USER_001", HttpStatus.NOT_FOUND, "User not found"),
  USER_ALREADY_EXISTS("USER_002", HttpStatus.CONFLICT, "User already exists"),
  INVALID_USER_DATA("USER_003", HttpStatus.BAD_REQUEST, "Invalid user data provided"),
  
  // Validation Errors (4xx)
  VALIDATION_ERROR("VAL_001", HttpStatus.BAD_REQUEST, "Validation failed"),
  INVALID_INPUT("VAL_002", HttpStatus.BAD_REQUEST, "Invalid input provided"),
  MISSING_REQUIRED_FIELD("VAL_003", HttpStatus.BAD_REQUEST, "Required field is missing"),
  
  // Nutritional Data Errors (4xx)
  INVALID_NUTRITIONAL_DATA("NUT_001", HttpStatus.BAD_REQUEST, "Invalid nutritional data provided"),
  NUTRITIONAL_CALCULATION_ERROR("NUT_002", HttpStatus.INTERNAL_SERVER_ERROR, "Error calculating nutritional information"),
  
  // Recipe Errors (4xx)
  RECIPE_NOT_FOUND("REC_001", HttpStatus.NOT_FOUND, "Recipe not found"),
  RECIPE_VALIDATION_ERROR("REC_002", HttpStatus.BAD_REQUEST, "Invalid recipe data provided"),
  RECIPE_ACCESS_DENIED("REC_003", HttpStatus.FORBIDDEN, "You do not have permission to access this recipe"),
  INGREDIENT_NOT_FOUND("REC_004", HttpStatus.NOT_FOUND, "Ingredient not found"),
  RECIPE_ALREADY_FAVORITED("REC_005", HttpStatus.CONFLICT, "Recipe is already in favorites"),
  INVALID_RECIPE_SERVINGS("REC_006", HttpStatus.BAD_REQUEST, "Invalid recipe servings value"),
  
  // Resource Errors (4xx)
  RESOURCE_NOT_FOUND("RES_001", HttpStatus.NOT_FOUND, "Requested resource not found"),
  RESOURCE_CONFLICT("RES_002", HttpStatus.CONFLICT, "Resource conflict detected"),
  METHOD_NOT_ALLOWED("RES_003", HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint"),
  
  // Store Errors (4xx/5xx)
  STORE_NOT_FOUND("STORE_001", HttpStatus.NOT_FOUND, "Store not found"),
  STORE_SCRAPING_ERROR("STORE_002", HttpStatus.INTERNAL_SERVER_ERROR, "Error scraping store flyer"),
  
  // Server Errors (5xx)
  INTERNAL_SERVER_ERROR("SYS_001", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
  SERVICE_UNAVAILABLE("SYS_002", HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),
  DATABASE_ERROR("SYS_003", HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed");
  
  private final String code;
  private final HttpStatus httpStatus;
  private final String defaultMessage;
  
  /**
   * Constructor for ErrorCode enum.
   * 
   * @param code the error code string
   * @param httpStatus the HTTP status code
   * @param defaultMessage the default error message
   */
  ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
    this.code = code;
    this.httpStatus = httpStatus;
    this.defaultMessage = defaultMessage;
  }
}
