package com.vallexia.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing inputs to prevent log injection, XSS,
 * information leakage, and other security vulnerabilities.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Component
public class InputSanitizer {
  
  private static final int MAX_DESCRIPTION_LENGTH = 500;
  private static final int MAX_USERNAME_LENGTH = 255;
  private static final int MAX_USER_AGENT_LENGTH = 500;
  private static final int MAX_REQUEST_URI_LENGTH = 500;
  private static final int MAX_IP_ADDRESS_LENGTH = 50;
  private static final int MAX_ERROR_MESSAGE_LENGTH = 500;
  
  @Value("${spring.profiles.active:dev}")
  private String activeProfile;
  
  // Patterns for detecting sensitive information in error messages
  private static final Pattern SQL_ERROR_PATTERN = Pattern.compile(
      "(?i)(sql|query|database|table|column|constraint|duplicate key|foreign key|syntax error)", 
      Pattern.CASE_INSENSITIVE);
  
  private static final Pattern FILE_PATH_PATTERN = Pattern.compile(
      "(?i)([a-zA-Z]:\\\\|/[a-zA-Z_\\-./]+/|C:\\\\|\\\\\\\\)", 
      Pattern.CASE_INSENSITIVE);
  
  private static final Pattern STACK_TRACE_PATTERN = Pattern.compile(
      "(?i)(\\s+at\\s+[a-zA-Z0-9_.]+\\(|Exception|Error:|Caused by:)", 
      Pattern.CASE_INSENSITIVE);
  
  private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
      "(?i)(com\\.|org\\.|java\\.|javax\\.)[a-zA-Z0-9_.]+",
      Pattern.CASE_INSENSITIVE);
  
  private static final Pattern IP_PATTERN = Pattern.compile(
      "(?i)\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
      Pattern.CASE_INSENSITIVE);
  
  /**
   * Sanitizes a string by removing control characters and truncating to max length.
   * Internal helper method used by other sanitize methods.
   * 
   * @param input the input string
   * @param maxLength maximum allowed length
   * @return sanitized string
   */
  private String sanitize(String input, int maxLength) {
    if (input == null) {
      return null;
    }
    
    // Remove control characters except newline and tab
    String sanitized = input.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
    
    // Remove null bytes
    sanitized = sanitized.replace("\0", "");
    
    // Truncate to max length
    if (sanitized.length() > maxLength) {
      sanitized = sanitized.substring(0, maxLength - 3) + "...";
      log.debug("Input truncated from {} to {} characters", input.length(), maxLength);
    }
    
    return sanitized;
  }
  
  /**
   * Sanitizes description field.
   */
  public String sanitizeDescription(String description) {
    return sanitize(description, MAX_DESCRIPTION_LENGTH);
  }
  
  /**
   * Sanitizes username field.
   */
  public String sanitizeUsername(String username) {
    return sanitize(username, MAX_USERNAME_LENGTH);
  }
  
  /**
   * Sanitizes user agent field.
   */
  public String sanitizeUserAgent(String userAgent) {
    return sanitize(userAgent, MAX_USER_AGENT_LENGTH);
  }
  
  /**
   * Sanitizes request URI field.
   */
  public String sanitizeRequestUri(String requestUri) {
    return sanitize(requestUri, MAX_REQUEST_URI_LENGTH);
  }
  
  /**
   * Sanitizes IP address field.
   */
  public String sanitizeIpAddress(String ipAddress) {
    if (ipAddress == null) {
      return null;
    }
    
    // Basic IP address validation
    String sanitized = sanitize(ipAddress, MAX_IP_ADDRESS_LENGTH);
    
    // Remove any characters that are not valid in IP addresses
    sanitized = sanitized.replaceAll("[^0-9a-fA-F:.%]", "");
    
    // Return null if result is empty after sanitization
    if (sanitized == null || sanitized.trim().isEmpty()) {
      return null;
    }
    
    return sanitized;
  }
  
  /**
   * Sanitizes error messages to prevent information leakage.
   * Filters out sensitive patterns such as SQL errors, file paths, stack traces,
   * and internal class names. In production mode, provides generic messages.
   * 
   * @param errorMessage the error message to sanitize
   * @return sanitized error message safe for client consumption
   */
  public String sanitizeErrorMessage(String errorMessage) {
    if (errorMessage == null) {
      return "An error occurred while processing your request";
    }
    
    // In development mode, allow more detailed errors but still sanitize
    boolean isDevelopment = "dev".equalsIgnoreCase(activeProfile);
    
    // First, check if message contains sensitive patterns
    if (containsSensitiveInformation(errorMessage)) {
      if (isDevelopment) {
        // In dev, sanitize but keep some information
        String sanitized = removeSensitivePatterns(errorMessage);
        return sanitize(sanitized, MAX_ERROR_MESSAGE_LENGTH);
      } else {
        // In production, use generic message
        return getGenericErrorMessage(errorMessage);
      }
    }
    
    // No sensitive data detected, sanitize and return
    return sanitize(errorMessage, MAX_ERROR_MESSAGE_LENGTH);
  }
  
  /**
   * Checks if the error message contains sensitive information.
   * 
   * @param message the message to check
   * @return true if sensitive information is detected
   */
  private boolean containsSensitiveInformation(String message) {
    if (message == null) {
      return false;
    }
    
    return SQL_ERROR_PATTERN.matcher(message).find() ||
           FILE_PATH_PATTERN.matcher(message).find() ||
           STACK_TRACE_PATTERN.matcher(message).find() ||
           CLASS_NAME_PATTERN.matcher(message).find() ||
           IP_PATTERN.matcher(message).find();
  }
  
  /**
   * Removes sensitive patterns from error message while keeping structure.
   * 
   * @param message the message to clean
   * @return cleaned message
   */
  private String removeSensitivePatterns(String message) {
    String cleaned = message;
    
    // Replace file paths with generic indicator
    cleaned = FILE_PATH_PATTERN.matcher(cleaned).replaceAll("[PATH]");
    
    // Replace IP addresses with generic indicator
    cleaned = IP_PATTERN.matcher(cleaned).replaceAll("[IP]");
    
    // Replace class names with generic indicator
    cleaned = CLASS_NAME_PATTERN.matcher(cleaned).replaceAll("[CLASS]");
    
    // Remove stack trace elements
    cleaned = STACK_TRACE_PATTERN.matcher(cleaned).replaceAll("");
    
    return cleaned.trim();
  }
  
  /**
   * Provides a generic error message based on the type of error detected.
   * 
   * @param originalMessage the original error message
   * @return generic error message
   */
  private String getGenericErrorMessage(String originalMessage) {
    if (SQL_ERROR_PATTERN.matcher(originalMessage).find()) {
      return "A database error occurred. Please contact support if the problem persists";
    }
    
    if (FILE_PATH_PATTERN.matcher(originalMessage).find() ||
        STACK_TRACE_PATTERN.matcher(originalMessage).find()) {
      return "A system error occurred. Please contact support if the problem persists";
    }
    
    return "An error occurred while processing your request";
  }
}
