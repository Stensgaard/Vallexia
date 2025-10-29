package com.vallexia.security.unit.util;

import com.vallexia.security.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InputSanitizer.
 * Tests input sanitization, edge cases, and security pattern filtering.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@DisplayName("InputSanitizer Unit Tests")
class InputSanitizerTest {
  
  private InputSanitizer inputSanitizer;
  
  @BeforeEach
  void setUp() {
    inputSanitizer = new InputSanitizer();
  }
  
  // ==================== Basic Sanitization Tests ====================
  
  @Test
  @DisplayName("Should return null when input is null")
  void shouldReturnNullWhenInputIsNull() {
    // When
    String result = inputSanitizer.sanitize(null, 100);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should remove control characters from input")
  void shouldRemoveControlCharacters() {
    // Given - string with control characters
    String input = "Hello\u0001World\u0002Test";
    
    // When
    String result = inputSanitizer.sanitize(input, 100);
    
    // Then
    assertThat(result).isEqualTo("HelloWorldTest");
    assertThat(result).doesNotContain("\u0001", "\u0002");
  }
  
  @Test
  @DisplayName("Should preserve newline and tab characters")
  void shouldPreserveNewlineAndTab() {
    // Given - string with newline and tab
    String input = "Line1\nLine2\tTabbed";
    
    // When
    String result = inputSanitizer.sanitize(input, 100);
    
    // Then
    assertThat(result).contains("\n", "\t");
    assertThat(result).isEqualTo("Line1\nLine2\tTabbed");
  }
  
  @Test
  @DisplayName("Should truncate input exceeding max length")
  void shouldTruncateInputExceedingMaxLength() {
    // Given
    String input = "a".repeat(100);
    int maxLength = 50;
    
    // When
    String result = inputSanitizer.sanitize(input, maxLength);
    
    // Then
    assertThat(result).hasSize(maxLength);
    assertThat(result).endsWith("...");
  }
  
  // ==================== IP Address Sanitization Tests ====================
  
  @Test
  @DisplayName("Should return null when IP address is null")
  void shouldReturnNullWhenIpAddressIsNull() {
    // When
    String result = inputSanitizer.sanitizeIpAddress(null);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should return null when IP address is empty after sanitization")
  void shouldReturnNullWhenIpAddressIsEmptyAfterSanitization() {
    // Given - input that becomes empty after removing invalid chars
    String input = "!!!###$$$";
    
    // When
    String result = inputSanitizer.sanitizeIpAddress(input);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should remove invalid characters from IP address")
  void shouldRemoveInvalidCharactersFromIpAddress() {
    // Given
    String input = "192.168.1.1!!!";
    
    // When
    String result = inputSanitizer.sanitizeIpAddress(input);
    
    // Then
    assertThat(result).isEqualTo("192.168.1.1");
  }
  
  @Test
  @DisplayName("Should preserve valid IPv4 address")
  void shouldPreserveValidIPv4Address() {
    // Given
    String ip = "192.168.1.100";
    
    // When
    String result = inputSanitizer.sanitizeIpAddress(ip);
    
    // Then
    assertThat(result).isEqualTo(ip);
  }
  
  // ==================== Error Message Sanitization Tests ====================
  
  @Test
  @DisplayName("Should provide generic message when error contains SQL error pattern")
  void shouldProvideGenericMessageForSqlErrors() {
    // Given - set production profile
    ReflectionTestUtils.setField(inputSanitizer, "activeProfile", "prod");
    String sqlError = "SQL syntax error at line 123";
    
    // When
    String result = inputSanitizer.sanitizeErrorMessage(sqlError);
    
    // Then
    assertThat(result).doesNotContain("SQL", "syntax", "line 123");
    assertThat(result).contains("database error");
  }
  
  @Test
  @DisplayName("Should sanitize file paths in error messages in production")
  void shouldSanitizeFilePathsInProduction() {
    // Given - set production profile
    ReflectionTestUtils.setField(inputSanitizer, "activeProfile", "prod");
    String errorWithPath = "Error at C:\\Windows\\System32\\config.sys";
    
    // When
    String result = inputSanitizer.sanitizeErrorMessage(errorWithPath);
    
    // Then
    assertThat(result).doesNotContain("C:\\Windows", "System32", "config.sys");
    assertThat(result).contains("system error");
  }
  
  @Test
  @DisplayName("Should sanitize stack traces in error messages in production")
  void shouldSanitizeStackTracesInProduction() {
    // Given - set production profile
    ReflectionTestUtils.setField(inputSanitizer, "activeProfile", "prod");
    String errorWithStackTrace = "Exception at com.vallexia.service.SomeService.process()";
    
    // When
    String result = inputSanitizer.sanitizeErrorMessage(errorWithStackTrace);
    
    // Then
    assertThat(result).doesNotContain("com.vallexia", "SomeService", "process()");
    assertThat(result).contains("system error");
  }
  
  @Test
  @DisplayName("Should preserve more details in development mode")
  void shouldPreserveMoreDetailsInDevMode() {
    // Given - set development profile
    ReflectionTestUtils.setField(inputSanitizer, "activeProfile", "dev");
    String errorWithPath = "Error at /home/user/app/config.xml";
    
    // When
    String result = inputSanitizer.sanitizeErrorMessage(errorWithPath);
    
    // Then - should sanitize but keep some information
    assertThat(result).doesNotContain("/home/user/app/config.xml");
    assertThat(result).contains("[PATH]");
  }
  
  @Test
  @DisplayName("Should return generic message when input is null")
  void shouldReturnGenericMessageWhenInputIsNull() {
    // When
    String result = inputSanitizer.sanitizeErrorMessage(null);
    
    // Then
    assertThat(result).isEqualTo("An error occurred while processing your request");
  }
  
  // ==================== Field-Specific Sanitization Tests ====================
  
  @Test
  @DisplayName("Should sanitize description field correctly")
  void shouldSanitizeDescriptionField() {
    // Given
    String description = "Test description with \u0001 control char";
    
    // When
    String result = inputSanitizer.sanitizeDescription(description);
    
    // Then
    assertThat(result).doesNotContain("\u0001");
    assertThat(result.length()).isLessThanOrEqualTo(500);
  }
  
  @Test
  @DisplayName("Should sanitize username field correctly")
  void shouldSanitizeUsernameField() {
    // Given
    String username = "test\u0002user";
    
    // When
    String result = inputSanitizer.sanitizeUsername(username);
    
    // Then
    assertThat(result).doesNotContain("\u0002");
    assertThat(result.length()).isLessThanOrEqualTo(255);
  }
  
  @Test
  @DisplayName("Should sanitize user agent field correctly")
  void shouldSanitizeUserAgentField() {
    // Given
    String userAgent = "Mozilla/5.0\u0003Invalid";
    
    // When
    String result = inputSanitizer.sanitizeUserAgent(userAgent);
    
    // Then
    assertThat(result).doesNotContain("\u0003");
    assertThat(result.length()).isLessThanOrEqualTo(500);
  }
  
  @Test
  @DisplayName("Should sanitize request URI field correctly")
  void shouldSanitizeRequestUriField() {
    // Given
    String requestUri = "/api/v1/test\u0004endpoint";
    
    // When
    String result = inputSanitizer.sanitizeRequestUri(requestUri);
    
    // Then
    assertThat(result).doesNotContain("\u0004");
    assertThat(result.length()).isLessThanOrEqualTo(500);
  }
  
  @Test
  @DisplayName("Should sanitize details field correctly")
  void shouldSanitizeDetailsField() {
    // Given
    String details = "Details with \u0005 control character";
    
    // When
    String result = inputSanitizer.sanitizeDetails(details);
    
    // Then
    assertThat(result).doesNotContain("\u0005");
    assertThat(result.length()).isLessThanOrEqualTo(5000);
  }
}

