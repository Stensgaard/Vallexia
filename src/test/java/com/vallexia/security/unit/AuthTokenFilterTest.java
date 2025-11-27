package com.vallexia.security.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vallexia.auth.service.TokenBlacklistService;
import com.vallexia.exception.ErrorCode;
import com.vallexia.exception.ErrorResponseDto;
import com.vallexia.exception.ErrorResponseMapper;
import com.vallexia.security.AuthTokenFilter;
import com.vallexia.security.UserPrincipal;
import com.vallexia.security.util.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthTokenFilter.
 * Tests JWT token parsing, validation, authentication, and error handling.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenFilter Unit Tests")
class AuthTokenFilterTest {
  
  private AuthTokenFilter authTokenFilter;
  
  @Mock
  private JwtUtils jwtUtils;
  
  @Mock
  private TokenBlacklistService tokenBlacklistService;
  
  @Mock
  private ErrorResponseMapper errorResponseMapper;
  
  @Mock
  private ObjectMapper objectMapper;
  
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  
  @Mock
  private FilterChain filterChain;
  
  private static final String VALID_TOKEN = "valid.jwt.token";
  private static final String TEST_USERNAME = "testuser";
  private static final Long TEST_USER_ID = 123L;
  private static final List<String> TEST_ROLES = Arrays.asList("ROLE_USER");
  
  @BeforeEach
  void setUp() {
    authTokenFilter = new AuthTokenFilter(jwtUtils, tokenBlacklistService, errorResponseMapper, objectMapper);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
  }
  
  // ==================== Token Parsing Tests ====================
  
  @Test
  @DisplayName("Should extract JWT from Authorization header with Bearer prefix")
  void shouldExtractJwtFromAuthorizationHeader() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(false);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).validateJwtToken(VALID_TOKEN);
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should return null when Authorization header is missing")
  void shouldReturnNullWhenAuthorizationHeaderMissing() throws Exception {
    // Given - no Authorization header
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
  
  @Test
  @DisplayName("Should return null when Authorization header doesn't start with Bearer")
  void shouldReturnNullWhenHeaderDoesntStartWithBearer() throws Exception {
    // Given
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should handle empty Authorization header")
  void shouldHandleEmptyAuthorizationHeader() throws Exception {
    // Given
    request.addHeader("Authorization", "");
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should handle Authorization header with only Bearer (no token)")
  void shouldHandleAuthorizationHeaderWithOnlyBearer() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer ");
    // parseJwt will return empty string, which will be validated
    when(jwtUtils.validateJwtToken("")).thenReturn(false);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).validateJwtToken("");
    verify(filterChain).doFilter(request, response);
  }
  
  // ==================== Token Validation Tests ====================
  
  @Test
  @DisplayName("Should authenticate user with valid token")
  void shouldAuthenticateUserWithValidToken() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenReturn(TEST_ROLES);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).validateJwtToken(VALID_TOKEN);
    verify(tokenBlacklistService).isTokenBlacklisted(VALID_TOKEN);
    verify(jwtUtils).getUsernameFromJwtToken(VALID_TOKEN);
    verify(jwtUtils).getUserIdFromJwtToken(VALID_TOKEN);
    verify(jwtUtils).getRolesFromJwtToken(VALID_TOKEN);
    
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    assertThat(userPrincipal.getId()).isEqualTo(TEST_USER_ID);
    assertThat(userPrincipal.getUsername()).isEqualTo(TEST_USERNAME);
    
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should not authenticate when token is null")
  void shouldNotAuthenticateWhenTokenIsNull() throws Exception {
    // Given - no Authorization header
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils, never()).validateJwtToken(anyString());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should not authenticate when token is invalid")
  void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer invalid.token");
    when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).validateJwtToken("invalid.token");
    verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should not authenticate when token validation throws JwtException")
  void shouldNotAuthenticateWhenTokenValidationThrowsJwtException() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenThrow(new JwtException("Invalid token"));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).validateJwtToken(VALID_TOKEN);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should check token blacklist before authentication")
  void shouldCheckTokenBlacklistBeforeAuthentication() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(tokenBlacklistService).isTokenBlacklisted(VALID_TOKEN);
    verify(jwtUtils, never()).getUsernameFromJwtToken(anyString());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  // ==================== Blacklist Tests ====================
  
  @Test
  @DisplayName("Should reject blacklisted token")
  void shouldRejectBlacklistedToken() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(tokenBlacklistService).isTokenBlacklisted(VALID_TOKEN);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should send error response for blacklisted token")
  void shouldSendErrorResponseForBlacklistedToken() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(errorResponseMapper).generateRequestId();
    verify(errorResponseMapper).toAuthenticationErrorResponse(
        ErrorCode.INVALID_TOKEN, "/api/v1/users/profile", requestId);
    verify(objectMapper).writeValue(any(OutputStream.class), eq(errorDto));
  }
  
  @Test
  @DisplayName("Should not authenticate when token is blacklisted")
  void shouldNotAuthenticateWhenTokenIsBlacklisted() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(jwtUtils, never()).getUsernameFromJwtToken(anyString());
  }
  
  // ==================== Claims Extraction Tests ====================
  
  @Test
  @DisplayName("Should extract username, userId, and roles from valid token")
  void shouldExtractClaimsFromValidToken() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenReturn(TEST_ROLES);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).getUsernameFromJwtToken(VALID_TOKEN);
    verify(jwtUtils).getUserIdFromJwtToken(VALID_TOKEN);
    verify(jwtUtils).getRolesFromJwtToken(VALID_TOKEN);
  }
  
  @Test
  @DisplayName("Should reject token when userId is null")
  void shouldRejectTokenWhenUserIdIsNull() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenThrow(new IllegalStateException("User ID claim is missing or invalid in token"));
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should reject token when roles is null")
  void shouldRejectTokenWhenRolesIsNull() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenThrow(new IllegalStateException("Roles claim is missing or invalid in token"));
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should reject token when roles is empty")
  void shouldRejectTokenWhenRolesIsEmpty() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenThrow(new IllegalStateException("Roles claim is empty in token"));
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should reject token when username is null")
  void shouldRejectTokenWhenUsernameIsNull() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(null);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should reject token when username is empty")
  void shouldRejectTokenWhenUsernameIsEmpty() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn("");
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should reject token when username extraction throws IllegalArgumentException")
  void shouldRejectTokenWhenUsernameExtractionThrowsException() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN))
        .thenThrow(new IllegalArgumentException("Token cannot be null or empty"));
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  // ==================== UserPrincipal Creation Tests ====================
  
  @Test
  @DisplayName("Should create UserPrincipal with correct values from token claims")
  void shouldCreateUserPrincipalWithCorrectValues() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenReturn(TEST_ROLES);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
    
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    assertThat(userPrincipal.getId()).isEqualTo(TEST_USER_ID);
    assertThat(userPrincipal.getUsername()).isEqualTo(TEST_USERNAME);
    assertThat(userPrincipal.getEmail()).isNull();
    assertThat(userPrincipal.getAuthorities()).hasSize(1);
    assertThat(userPrincipal.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
  }
  
  @Test
  @DisplayName("Should set all account status flags to true (by design)")
  void shouldSetAllAccountStatusFlagsToTrue() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenReturn(TEST_ROLES);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    assertThat(userPrincipal.isEnabled()).isTrue();
    assertThat(userPrincipal.isAccountNonExpired()).isTrue();
    assertThat(userPrincipal.isAccountNonLocked()).isTrue();
    assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
  }
  
  @Test
  @DisplayName("Should create authorities from roles list")
  void shouldCreateAuthoritiesFromRolesList() throws Exception {
    // Given
    List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenReturn(roles);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    assertThat(userPrincipal.getAuthorities()).hasSize(2);
    assertThat(userPrincipal.getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .toList())
        .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }
  
  // ==================== Security Context Tests ====================
  
  @Test
  @DisplayName("Should set authentication in SecurityContext for valid token")
  void shouldSetAuthenticationInSecurityContextForValidToken() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenReturn(TEST_ROLES);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.isAuthenticated()).isTrue();
    assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
  }
  
  @Test
  @DisplayName("Should not set authentication for invalid token")
  void shouldNotSetAuthenticationForInvalidToken() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer invalid.token");
    when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
  
  @Test
  @DisplayName("Should not set authentication when token is blacklisted")
  void shouldNotSetAuthenticationWhenTokenIsBlacklisted() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), anyString(), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
  
  // ==================== Error Response Tests ====================
  
  @Test
  @DisplayName("Should send error response with correct status code (401)")
  void shouldSendErrorResponseWithCorrectStatusCode() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
  }
  
  @Test
  @DisplayName("Should send error response with correct content type (JSON)")
  void shouldSendErrorResponseWithCorrectContentType() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
  }
  
  @Test
  @DisplayName("Should send error response with INVALID_TOKEN error code")
  void shouldSendErrorResponseWithInvalidTokenErrorCode() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(errorResponseMapper).toAuthenticationErrorResponse(
        ErrorCode.INVALID_TOKEN, "/api/v1/users/profile", requestId);
  }
  
  @Test
  @DisplayName("Should include request ID in error response")
  void shouldIncludeRequestIdInErrorResponse() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id-123";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(errorResponseMapper).generateRequestId();
    verify(errorResponseMapper).toAuthenticationErrorResponse(
        ErrorCode.INVALID_TOKEN, "/api/v1/users/profile", requestId);
  }
  
  @Test
  @DisplayName("Should include request path in error response")
  void shouldIncludeRequestPathInErrorResponse() throws Exception {
    // Given
    String path = "/api/v1/recipes/123";
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI(path);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, path);
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq(path), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(errorResponseMapper).toAuthenticationErrorResponse(
        ErrorCode.INVALID_TOKEN, path, requestId);
  }
  
  // ==================== Exception Handling Tests ====================
  
  @Test
  @DisplayName("Should catch JwtException and not set authentication")
  void shouldCatchJwtExceptionAndNotSetAuthentication() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenThrow(new JwtException("JWT parsing failed"));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should catch IllegalArgumentException from username extraction and send error response")
  void shouldCatchIllegalArgumentExceptionFromUsernameExtraction() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN))
        .thenThrow(new IllegalArgumentException("Token cannot be null or empty"));
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should propagate unexpected exceptions as ServletException")
  void shouldPropagateUnexpectedExceptionsAsServletException() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenThrow(new RuntimeException("Unexpected error"));
    
    // When & Then
    assertThatThrownBy(() -> authTokenFilter.doFilter(request, response, filterChain))
        .isInstanceOf(ServletException.class)
        .hasMessageContaining("Authentication processing failed")
        .hasCauseInstanceOf(RuntimeException.class);
  }
  
  @Test
  @DisplayName("Should continue filter chain after handling errors")
  void shouldContinueFilterChainAfterHandlingErrors() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer invalid.token");
    when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(filterChain).doFilter(request, response);
  }
  
  // ==================== Edge Cases ====================
  
  @Test
  @DisplayName("Should handle token with missing claims gracefully")
  void shouldHandleTokenWithMissingClaimsGracefully() throws Exception {
    // Given
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    request.setRequestURI("/api/v1/users/profile");
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenThrow(new IllegalStateException("User ID claim is missing or invalid in token"));
    
    String requestId = "test-request-id";
    ErrorResponseDto errorDto = createErrorResponseDto(requestId, "/api/v1/users/profile");
    when(errorResponseMapper.generateRequestId()).thenReturn(requestId);
    when(errorResponseMapper.toAuthenticationErrorResponse(
        eq(ErrorCode.INVALID_TOKEN), eq("/api/v1/users/profile"), eq(requestId)))
        .thenReturn(errorDto);
    doNothing().when(objectMapper).writeValue(any(OutputStream.class), any(ErrorResponseDto.class));
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
  
  @Test
  @DisplayName("Should handle malformed Authorization header")
  void shouldHandleMalformedAuthorizationHeader() throws Exception {
    // Given
    request.addHeader("Authorization", "BearerTokenWithoutSpace");
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils, never()).validateJwtToken(anyString());
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should handle very long token strings")
  void shouldHandleVeryLongTokenStrings() throws Exception {
    // Given
    String longToken = "a".repeat(10000);
    request.addHeader("Authorization", "Bearer " + longToken);
    when(jwtUtils.validateJwtToken(longToken)).thenReturn(false);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).validateJwtToken(longToken);
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should handle special characters in token")
  void shouldHandleSpecialCharactersInToken() throws Exception {
    // Given
    String specialToken = "token.with-special_chars@123";
    request.addHeader("Authorization", "Bearer " + specialToken);
    when(jwtUtils.validateJwtToken(specialToken)).thenReturn(false);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    verify(jwtUtils).validateJwtToken(specialToken);
    verify(filterChain).doFilter(request, response);
  }
  
  @Test
  @DisplayName("Should handle multiple roles correctly")
  void shouldHandleMultipleRolesCorrectly() throws Exception {
    // Given
    List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR");
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(jwtUtils.validateJwtToken(VALID_TOKEN)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
    when(jwtUtils.getUsernameFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(jwtUtils.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
    when(jwtUtils.getRolesFromJwtToken(VALID_TOKEN)).thenReturn(roles);
    
    // When
    authTokenFilter.doFilter(request, response, filterChain);
    
    // Then
    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    assertThat(userPrincipal.getAuthorities()).hasSize(3);
    assertThat(userPrincipal.getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .toList())
        .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR");
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
        .code(ErrorCode.INVALID_TOKEN.getCode())
        .message(ErrorCode.INVALID_TOKEN.getDefaultMessage())
        .httpStatus(ErrorCode.INVALID_TOKEN.getHttpStatus().value())
        .path(path)
        .requestId(requestId)
        .details(null)
        .timestamp(java.time.LocalDateTime.now())
        .build();
  }
}
