package com.vallexia.security.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.ErrorResponseDto;
import com.vallexia.exception.ErrorResponseMapper;
import com.vallexia.security.AuthEntryPointJwt;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthEntryPointJwt.
 * Tests unauthorized access handling, error response generation, and JSON serialization.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthEntryPointJwt Unit Tests")
class AuthEntryPointJwtTest {
  
  private AuthEntryPointJwt authEntryPointJwt;
  
  @Mock
  private ErrorResponseMapper errorResponseMapper;
  
  @Mock
  private ObjectMapper objectMapper;
  
  @Mock
  private HttpServletRequest request;
  
  private MockHttpServletResponse response;
  
  @Mock
  private AuthenticationException authException;
  
  @BeforeEach
  void setUp() {
    authEntryPointJwt = new AuthEntryPointJwt(errorResponseMapper, objectMapper);
    response = new MockHttpServletResponse();
  }
  
  // ==================== Response Status and Content Type Tests ====================
  
  @Test
  @DisplayName("Should set HTTP status to 401 (UNAUTHORIZED)")
  void shouldSetHttpStatusTo401() throws IOException, ServletException {
    // Given
    String requestId = "test-request-id";
    String path = "/api/v1/users/profile";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authEntryPointJwt.commence(request, response, authException);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
  }
  
  @Test
  @DisplayName("Should set content type to APPLICATION_JSON")
  void shouldSetContentTypeToApplicationJson() throws IOException, ServletException {
    // Given
    String requestId = "test-request-id";
    String path = "/api/v1/users/profile";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authEntryPointJwt.commence(request, response, authException);
    
    // Then
    assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
  }
  
  // ==================== ErrorResponseMapper Integration Tests ====================
  
  @Test
  @DisplayName("Should generate request ID using ErrorResponseMapper")
  void shouldGenerateRequestIdUsingErrorResponseMapper() throws IOException, ServletException {
    // Given
    String requestId = "generated-uuid-12345";
    String path = "/api/v1/users/profile";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authEntryPointJwt.commence(request, response, authException);
    
    // Then
    verify(errorResponseMapper).generateRequestId();
  }
  
  @Test
  @DisplayName("Should extract request URI from request")
  void shouldExtractRequestUriFromRequest() throws IOException, ServletException {
    // Given
    String requestId = "test-request-id";
    String path = "/api/v1/recipes/123";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authEntryPointJwt.commence(request, response, authException);
    
    // Then
    verify(request).getRequestURI();
    verify(errorResponseMapper).toAuthenticationErrorResponse(
        ErrorCode.AUTHENTICATION_ERROR, path, requestId);
  }
  
  @Test
  @DisplayName("Should call toAuthenticationErrorResponse with correct parameters")
  void shouldCallToAuthenticationErrorResponseWithCorrectParameters() throws IOException, ServletException {
    // Given
    String requestId = "test-request-id";
    String path = "/api/v1/users/settings";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authEntryPointJwt.commence(request, response, authException);
    
    // Then
    verify(errorResponseMapper).toAuthenticationErrorResponse(
        ErrorCode.AUTHENTICATION_ERROR, path, requestId);
  }
  
  // ==================== JSON Serialization Tests ====================
  
  @Test
  @DisplayName("Should write error response to response output stream")
  void shouldWriteErrorResponseToResponseOutputStream() throws IOException, ServletException {
    // Given
    String requestId = "test-request-id";
    String path = "/api/v1/users/profile";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authEntryPointJwt.commence(request, response, authException);
    
    // Then
    verify(objectMapper).writeValue(eq(response.getOutputStream()), eq(errorDto));
  }
  
  @Test
  @DisplayName("Should write correct error response JSON to output stream")
  void shouldWriteCorrectErrorResponseJsonToOutputStream() throws IOException, ServletException {
    // Given
    String requestId = "test-request-id-123";
    String path = "/api/v1/recipes";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    // Use real ObjectMapper for this test to verify JSON serialization
    // Configure with JavaTimeModule to handle LocalDateTime
    ObjectMapper realObjectMapper = new ObjectMapper();
    realObjectMapper.registerModule(new JavaTimeModule());
    authEntryPointJwt = new AuthEntryPointJwt(errorResponseMapper, realObjectMapper);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    
    // When
    authEntryPointJwt.commence(request, response, authException);
    
    // Then
    String jsonResponse = response.getContentAsString();
    assertThat(jsonResponse).isNotEmpty();
    
    // Verify JSON contains expected fields (without deserializing)
    assertThat(jsonResponse).contains("\"code\":\"" + errorDto.getCode() + "\"");
    assertThat(jsonResponse).contains("\"message\":\"" + errorDto.getMessage() + "\"");
    assertThat(jsonResponse).contains("\"httpStatus\":" + errorDto.getHttpStatus());
    assertThat(jsonResponse).contains("\"path\":\"" + errorDto.getPath() + "\"");
    assertThat(jsonResponse).contains("\"requestId\":\"" + errorDto.getRequestId() + "\"");
  }
  
  // ==================== Exception Handling Tests ====================
  
  @Test
  @DisplayName("Should propagate IOException when writing response")
  void shouldPropagateIOExceptionWhenWritingResponse() throws IOException {
    // Given
    String requestId = "test-request-id";
    String path = "/api/v1/users/profile";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    
    IOException ioException = new IOException("Failed to write response");
    doThrow(ioException).when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When & Then
    assertThatThrownBy(() -> authEntryPointJwt.commence(request, response, authException))
        .isInstanceOf(IOException.class)
        .isEqualTo(ioException);
  }
  
  @Test
  @DisplayName("Should propagate IOException when getting output stream")
  void shouldPropagateIOExceptionWhenGettingOutputStream() throws IOException {
    // Given
    String requestId = "test-request-id";
    String path = "/api/v1/users/profile";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    IOException ioException = new IOException("Failed to get output stream");
    
    when(request.getRequestURI()).thenReturn(path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.AUTHENTICATION_ERROR), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    when(mockResponse.getOutputStream()).thenThrow(ioException);
    doNothing().when(mockResponse).setContentType(anyString());
    doNothing().when(mockResponse).setStatus(anyInt());
    
    // When & Then
    assertThatThrownBy(() -> authEntryPointJwt.commence(request, mockResponse, authException))
        .isInstanceOf(IOException.class)
        .isEqualTo(ioException);
  }
  
  // ==================== Helper Methods ====================
  
  /**
   * Creates a test ErrorResponseDto with standard values.
   * 
   * @param requestId the request ID
   * @param path the request path
   * @return ErrorResponseDto instance
   */
  private ErrorResponseDto createErrorResponseDto(String requestId, String path) {
    return ErrorResponseDto.builder()
        .code(ErrorCode.AUTHENTICATION_ERROR.getCode())
        .message(ErrorCode.AUTHENTICATION_ERROR.getDefaultMessage())
        .httpStatus(ErrorCode.AUTHENTICATION_ERROR.getHttpStatus().value())
        .path(path)
        .requestId(requestId)
        .details(null)
        .timestamp(java.time.LocalDateTime.now())
        .build();
  }
}
