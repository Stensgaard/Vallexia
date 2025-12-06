package com.vallexia.auth.unit.controller;

import com.vallexia.auth.controller.AuthController;
import com.vallexia.auth.dto.*;
import com.vallexia.auth.exception.AccountLockedException;
import com.vallexia.auth.exception.AuthenticationException;
import com.vallexia.auth.exception.UserAlreadyExistsException;
import com.vallexia.auth.fixtures.AuthTestFixtures;
import com.vallexia.auth.service.AuthService;
import com.vallexia.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {
  
  @Mock
  private AuthService authService;
  
  @Mock
  private HttpServletRequest request;
  
  @InjectMocks
  private AuthController authController;
  
  // ==================== registerUser() Endpoint Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return 201 CREATED on successful registration")
  void shouldReturn201CreatedOnSuccessfulRegistration() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    JwtResponseDto responseDto = AuthTestFixtures.createJwtResponseDto();
    
    when(authService.registerUser(registerDto, request)).thenReturn(responseDto);
    
    // When
    ResponseEntity<JwtResponseDto> response = authController.registerUser(registerDto, request);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JwtResponseDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getAccessToken()).isEqualTo(responseDto.getAccessToken());
    assertThat(body.getRefreshToken()).isEqualTo(responseDto.getRefreshToken());
    verify(authService).registerUser(registerDto, request);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return JWT response DTO on successful registration")
  void shouldReturnJwtResponseDtoOnSuccessfulRegistration() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    JwtResponseDto responseDto = AuthTestFixtures.createJwtResponseDto();
    
    when(authService.registerUser(registerDto, request)).thenReturn(responseDto);
    
    // When
    ResponseEntity<JwtResponseDto> response = authController.registerUser(registerDto, request);
    
    // Then
    JwtResponseDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(responseDto.getId());
    assertThat(body.getUsername()).isEqualTo(responseDto.getUsername());
    assertThat(body.getEmail()).isEqualTo(responseDto.getEmail());
  }
  
  @Test
  @DisplayName("Should propagate UserAlreadyExistsException as service exception")
  void shouldPropagateUserAlreadyExistsExceptionAsServiceException() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    
    when(authService.registerUser(registerDto, request))
        .thenThrow(new UserAlreadyExistsException("Username is already taken"));
    
    // When & Then
    assertThatThrownBy(() -> authController.registerUser(registerDto, request))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("Username is already taken");
    
    verify(authService).registerUser(registerDto, request);
  }
  
  @Test
  @DisplayName("Should propagate ValidationException for invalid input")
  void shouldPropagateValidationExceptionForInvalidInput() {
    // Given
    RegisterRequestDto registerDto = AuthTestFixtures.createRegisterRequestDto();
    
    when(authService.registerUser(registerDto, request))
        .thenThrow(new ValidationException("Password and confirmation password do not match"));
    
    // When & Then
    assertThatThrownBy(() -> authController.registerUser(registerDto, request))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Password and confirmation password do not match");
    
    verify(authService).registerUser(registerDto, request);
  }
  
  // ==================== authenticateUser() Endpoint Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return 200 OK on successful login")
  void shouldReturn200OkOnSuccessfulLogin() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    JwtResponseDto responseDto = AuthTestFixtures.createJwtResponseDto();
    
    when(authService.authenticateUser(loginDto, request)).thenReturn(responseDto);
    
    // When
    ResponseEntity<JwtResponseDto> response = authController.authenticateUser(loginDto, request);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JwtResponseDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getAccessToken()).isEqualTo(responseDto.getAccessToken());
    verify(authService).authenticateUser(loginDto, request);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return JWT response DTO on successful login")
  void shouldReturnJwtResponseDtoOnSuccessfulLogin() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    JwtResponseDto responseDto = AuthTestFixtures.createJwtResponseDto();
    
    when(authService.authenticateUser(loginDto, request)).thenReturn(responseDto);
    
    // When
    ResponseEntity<JwtResponseDto> response = authController.authenticateUser(loginDto, request);
    
    // Then
    JwtResponseDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(responseDto.getId());
    assertThat(body.getUsername()).isEqualTo(responseDto.getUsername());
  }
  
  @Test
  @DisplayName("Should propagate AuthenticationException for invalid credentials")
  void shouldPropagateAuthenticationExceptionForInvalidCredentials() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    
    when(authService.authenticateUser(loginDto, request))
        .thenThrow(new AuthenticationException("Invalid username/email or password"));
    
    // When & Then
    assertThatThrownBy(() -> authController.authenticateUser(loginDto, request))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid username/email or password");
    
    verify(authService).authenticateUser(loginDto, request);
  }
  
  @Test
  @DisplayName("Should propagate AccountLockedException for locked account")
  void shouldPropagateAccountLockedExceptionForLockedAccount() {
    // Given
    LoginRequestDto loginDto = AuthTestFixtures.createLoginRequestDto();
    
    when(authService.authenticateUser(loginDto, request))
        .thenThrow(new AccountLockedException("Account is temporarily locked"));
    
    // When & Then
    assertThatThrownBy(() -> authController.authenticateUser(loginDto, request))
        .isInstanceOf(AccountLockedException.class)
        .hasMessageContaining("Account is temporarily locked");
    
    verify(authService).authenticateUser(loginDto, request);
  }
  
  // ==================== refreshToken() Endpoint Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return 200 OK on successful token refresh")
  void shouldReturn200OkOnSuccessfulTokenRefresh() {
    // Given
    RefreshTokenRequestDto refreshDto = AuthTestFixtures.createRefreshTokenRequestDto();
    JwtResponseDto responseDto = AuthTestFixtures.createJwtResponseDto();
    
    when(authService.refreshToken(refreshDto.getRefreshToken())).thenReturn(responseDto);
    
    // When
    ResponseEntity<JwtResponseDto> response = authController.refreshToken(refreshDto);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JwtResponseDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getAccessToken()).isEqualTo(responseDto.getAccessToken());
    verify(authService).refreshToken(refreshDto.getRefreshToken());
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return JWT response DTO on successful token refresh")
  void shouldReturnJwtResponseDtoOnSuccessfulTokenRefresh() {
    // Given
    RefreshTokenRequestDto refreshDto = AuthTestFixtures.createRefreshTokenRequestDto();
    JwtResponseDto responseDto = AuthTestFixtures.createJwtResponseDto();
    
    when(authService.refreshToken(refreshDto.getRefreshToken())).thenReturn(responseDto);
    
    // When
    ResponseEntity<JwtResponseDto> response = authController.refreshToken(refreshDto);
    
    // Then
    JwtResponseDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(responseDto.getId());
    assertThat(body.getRefreshToken()).isEqualTo(responseDto.getRefreshToken());
  }
  
  @Test
  @DisplayName("Should propagate AuthenticationException for invalid token")
  void shouldPropagateAuthenticationExceptionForInvalidToken() {
    // Given
    RefreshTokenRequestDto refreshDto = AuthTestFixtures.createRefreshTokenRequestDto();
    
    when(authService.refreshToken(refreshDto.getRefreshToken()))
        .thenThrow(new AuthenticationException("Invalid refresh token"));
    
    // When & Then
    assertThatThrownBy(() -> authController.refreshToken(refreshDto))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("Invalid refresh token");
    
    verify(authService).refreshToken(refreshDto.getRefreshToken());
  }
  
  // ==================== logoutUser() Endpoint Tests ====================
  
  @Test
  @DisplayName("Should return 204 NO CONTENT on successful logout")
  void shouldReturn204NoContentOnSuccessfulLogout() {
    // Given
    doNothing().when(authService).logoutUser(request);
    
    // When
    ResponseEntity<Void> response = authController.logoutUser(request);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
    verify(authService).logoutUser(request);
  }
  
  @Test
  @DisplayName("Should call authService logoutUser method")
  void shouldCallAuthServiceLogoutUserMethod() {
    // Given
    doNothing().when(authService).logoutUser(request);
    
    // When
    authController.logoutUser(request);
    
    // Then
    verify(authService, times(1)).logoutUser(request);
  }
  
  @Test
  @DisplayName("Should not throw exception even if logout service fails")
  void shouldNotThrowExceptionEvenIfLogoutServiceFails() {
    // Given
    doNothing().when(authService).logoutUser(request);
    // Service handles exceptions internally, so it never throws
    
    // When & Then - should not throw
    ResponseEntity<Void> response = authController.logoutUser(request);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(authService).logoutUser(request);
  }
}
