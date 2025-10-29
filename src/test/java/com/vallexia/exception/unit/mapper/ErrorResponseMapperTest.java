package com.vallexia.exception.unit.mapper;

import com.vallexia.exception.*;
import com.vallexia.security.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ErrorResponseMapper.
 * Tests mapping logic for various exception types and error response building.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ErrorResponseMapper Unit Tests")
class ErrorResponseMapperTest {
  
  private ErrorResponseMapper errorResponseMapper;
  
  @Mock
  private InputSanitizer inputSanitizer;
  
  @BeforeEach
  void setUp() {
    errorResponseMapper = new ErrorResponseMapper(inputSanitizer);
  }
  
  // ==================== toErrorResponse() Tests ====================
  
  @Test
  @DisplayName("Should map VallexiaException to ErrorResponseDto with all fields")
  void shouldMapVallexiaExceptionToErrorResponseDtoWithAllFields() {
    // Given
    VallexiaException exception = new ValidationException("Test validation error");
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    when(inputSanitizer.sanitizeErrorMessage("Test validation error"))
        .thenReturn("Test validation error");
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toErrorResponse(
        exception, 
        webRequest, 
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getCode()).isEqualTo("VAL_001");
    assertThat(errorResponse.getMessage()).isEqualTo("Test validation error");
    assertThat(errorResponse.getHttpStatus()).isEqualTo(400);
    assertThat(errorResponse.getRequestId()).isEqualTo(requestId);
    assertThat(errorResponse.getPath()).isNotNull();
    assertThat(errorResponse.getTimestamp()).isNotNull();
  }
  
  @Test
  @DisplayName("Should sanitize error message using InputSanitizer")
  void shouldSanitizeErrorMessageUsingInputSanitizer() {
    // Given
    VallexiaException exception = new ValidationException("Sensitive error info");
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    when(inputSanitizer.sanitizeErrorMessage("Sensitive error info"))
        .thenReturn("Sanitized error message");
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toErrorResponse(
        exception, 
        webRequest, 
        requestId
    );
    
    // Then
    assertThat(errorResponse.getMessage()).isEqualTo("Sanitized error message");
  }
  
  @Test
  @DisplayName("Should map exception with null details")
  void shouldMapExceptionWithNullDetails() {
    // Given
    VallexiaException exception = new ValidationException("Error message");
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    when(inputSanitizer.sanitizeErrorMessage("Error message"))
        .thenReturn("Error message");
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toErrorResponse(
        exception, 
        webRequest, 
        requestId
    );
    
    // Then
    assertThat(errorResponse.getDetails()).isNull();
  }
  
  // ==================== toGenericErrorResponse() Tests ====================
  
  @Test
  @DisplayName("Should map generic exception to ErrorResponseDto")
  void shouldMapGenericExceptionToErrorResponseDto() {
    // Given
    Exception exception = new RuntimeException("Unexpected error");
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toGenericErrorResponse(
        exception, 
        webRequest, 
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getCode()).isEqualTo("SYS_001");
    assertThat(errorResponse.getMessage()).isEqualTo("An unexpected error occurred");
    assertThat(errorResponse.getHttpStatus()).isEqualTo(500);
    assertThat(errorResponse.getRequestId()).isEqualTo(requestId);
    assertThat(errorResponse.getDetails()).isNull();
  }
  
  @Test
  @DisplayName("Should handle null exception in generic error response")
  void shouldHandleNullExceptionInGenericErrorResponse() {
    // Given
    Exception exception = null;
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toGenericErrorResponse(
        exception, 
        webRequest, 
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getCode()).isEqualTo("SYS_001");
    assertThat(errorResponse.getHttpStatus()).isEqualTo(500);
  }
  
  // ==================== toValidationErrorResponse() Tests ====================
  
  @Test
  @DisplayName("Should map validation errors with field details")
  void shouldMapValidationErrorsWithFieldDetails() {
    // Given
    Map<String, String> validationErrors = new HashMap<>();
    validationErrors.put("email", "Email is required");
    validationErrors.put("password", "Password must be at least 8 characters");
    
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.VALIDATION_ERROR,
        validationErrors,
        webRequest,
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getCode()).isEqualTo("VAL_001");
    assertThat(errorResponse.getMessage()).isEqualTo("Validation failed");
    assertThat(errorResponse.getHttpStatus()).isEqualTo(400);
    assertThat(errorResponse.getDetails()).isEqualTo(validationErrors);
    assertThat(errorResponse.getDetails().get("email")).isEqualTo("Email is required");
    assertThat(errorResponse.getDetails().get("password")).isEqualTo("Password must be at least 8 characters");
  }
  
  @Test
  @DisplayName("Should handle null validation errors")
  void shouldHandleNullValidationErrors() {
    // Given
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.ACCESS_DENIED,
        null,
        webRequest,
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getCode()).isEqualTo("AUTH_005");
    assertThat(errorResponse.getHttpStatus()).isEqualTo(403);
    assertThat(errorResponse.getDetails()).isNull();
  }
  
  @Test
  @DisplayName("Should handle empty validation errors map")
  void shouldHandleEmptyValidationErrorsMap() {
    // Given
    Map<String, String> validationErrors = new HashMap<>();
    ServletWebRequest webRequest = createWebRequest();
    String requestId = errorResponseMapper.generateRequestId();
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toValidationErrorResponse(
        ErrorCode.VALIDATION_ERROR,
        validationErrors,
        webRequest,
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getDetails()).isEmpty();
  }
  
  // ==================== toAuthenticationErrorResponse() Tests ====================
  
  @Test
  @DisplayName("Should build authentication error response with path")
  void shouldBuildAuthenticationErrorResponseWithPath() {
    // Given
    String path = "/api/v1/auth/login";
    String requestId = errorResponseMapper.generateRequestId();
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toAuthenticationErrorResponse(
        ErrorCode.AUTHENTICATION_ERROR,
        path,
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getCode()).isEqualTo("AUTH_001");
    assertThat(errorResponse.getMessage()).isEqualTo("Authentication failed");
    assertThat(errorResponse.getHttpStatus()).isEqualTo(401);
    assertThat(errorResponse.getPath()).isEqualTo(path);
    assertThat(errorResponse.getRequestId()).isEqualTo(requestId);
    assertThat(errorResponse.getDetails()).isNull();
    assertThat(errorResponse.getTimestamp()).isNotNull();
  }
  
  @Test
  @DisplayName("Should build error response for different error codes")
  void shouldBuildErrorResponseForDifferentErrorCodes() {
    // Given
    String path = "/api/v1/users/profile";
    String requestId = errorResponseMapper.generateRequestId();
    
    // When
    ErrorResponseDto errorResponse = errorResponseMapper.toAuthenticationErrorResponse(
        ErrorCode.ACCESS_DENIED,
        path,
        requestId
    );
    
    // Then
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getCode()).isEqualTo("AUTH_005");
    assertThat(errorResponse.getMessage()).isEqualTo("You do not have permission to access this resource");
    assertThat(errorResponse.getHttpStatus()).isEqualTo(403);
  }
  
  // ==================== generateRequestId() Tests ====================
  
  @Test
  @DisplayName("Should generate unique request IDs")
  void shouldGenerateUniqueRequestIds() {
    // When
    String requestId1 = errorResponseMapper.generateRequestId();
    String requestId2 = errorResponseMapper.generateRequestId();
    
    // Then
    assertThat(requestId1).isNotNull();
    assertThat(requestId2).isNotNull();
    assertThat(requestId1).isNotEqualTo(requestId2);
  }
  
  @Test
  @DisplayName("Should generate UUID format request IDs")
  void shouldGenerateUUIDFormatRequestIds() {
    // When
    String requestId = errorResponseMapper.generateRequestId();
    
    // Then
    assertThat(requestId).matches(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    );
  }
  
  // ==================== Helper Methods ====================
  
  private ServletWebRequest createWebRequest() {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setRequestURI("/api/v1/test");
    return new ServletWebRequest(mockRequest);
  }
}






