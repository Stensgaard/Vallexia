package com.vallexia.auth.unit.service;

import com.vallexia.auth.fixtures.AuthTestFixtures;
import com.vallexia.auth.service.JwtTokenService;
import com.vallexia.security.util.JwtUtils;
import com.vallexia.user.entity.User;
import com.vallexia.user.entity.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtTokenService.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JwtTokenService Unit Tests")
class JwtTokenServiceTest {
  
  @Mock
  private JwtUtils jwtUtils;
  
  @InjectMocks
  private JwtTokenService jwtTokenService;
  
  private User testUser;
  
  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setRoles(Set.of(Role.USER));
  }
  
  @Test
  @DisplayName("Should generate tokens successfully")
  void shouldGenerateTokensSuccessfully() {
    // Given
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    Date expirationDate = new Date(System.currentTimeMillis() + 3600000);
    
    when(jwtUtils.generateAccessToken(anyString(), anyLong(), anyList())).thenReturn(accessToken);
    when(jwtUtils.generateRefreshToken(anyString(), anyLong(), anyList())).thenReturn(refreshToken);
    when(jwtUtils.getExpirationDateFromToken(accessToken)).thenReturn(expirationDate);
    
    // When
    JwtTokenService.JwtTokenData result = jwtTokenService.generateTokens(testUser);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.accessToken()).isEqualTo(accessToken);
    assertThat(result.refreshToken()).isEqualTo(refreshToken);
    assertThat(result.expiresAt()).isNotNull();
    
    verify(jwtUtils).generateAccessToken(eq(testUser.getUsername()), eq(testUser.getId()), anyList());
    verify(jwtUtils).generateRefreshToken(eq(testUser.getUsername()), eq(testUser.getId()), anyList());
    verify(jwtUtils).getExpirationDateFromToken(accessToken);
  }
  
  @Test
  @DisplayName("Should parse JWT from request with Bearer token")
  void shouldParseJwtFromRequestWithBearerToken() {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Bearer " + AuthTestFixtures.TEST_ACCESS_TOKEN);
    
    // When
    String result = jwtTokenService.parseJwtFromRequest(request);
    
    // Then
    assertThat(result).isEqualTo(AuthTestFixtures.TEST_ACCESS_TOKEN);
  }
  
  @Test
  @DisplayName("Should return null when Authorization header is missing")
  void shouldReturnNullWhenAuthorizationHeaderMissing() {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn(null);
    
    // When
    String result = jwtTokenService.parseJwtFromRequest(request);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should return null when Authorization header doesn't start with Bearer")
  void shouldReturnNullWhenHeaderDoesntStartWithBearer() {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Basic token");
    
    // When
    String result = jwtTokenService.parseJwtFromRequest(request);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should return null when token is empty after Bearer prefix")
  void shouldReturnNullWhenTokenIsEmptyAfterBearer() {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Bearer ");
    
    // When
    String result = jwtTokenService.parseJwtFromRequest(request);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should validate token successfully")
  void shouldValidateTokenSuccessfully() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    
    // When
    boolean result = jwtTokenService.isValidToken(token);
    
    // Then
    assertThat(result).isTrue();
    verify(jwtUtils).validateJwtToken(token);
  }
  
  @Test
  @DisplayName("Should return false for null token")
  void shouldReturnFalseForNullToken() {
    // When
    boolean result = jwtTokenService.isValidToken(null);
    
    // Then
    assertThat(result).isFalse();
    verify(jwtUtils, never()).validateJwtToken(anyString());
  }
  
  @Test
  @DisplayName("Should return false for empty token")
  void shouldReturnFalseForEmptyToken() {
    // When
    boolean result = jwtTokenService.isValidToken("");
    
    // Then
    assertThat(result).isFalse();
    verify(jwtUtils, never()).validateJwtToken(anyString());
  }
  
  @Test
  @DisplayName("Should check if token is expired")
  void shouldCheckIfTokenIsExpired() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    when(jwtUtils.isTokenExpired(token)).thenReturn(true);
    
    // When
    boolean result = jwtTokenService.isTokenExpired(token);
    
    // Then
    assertThat(result).isTrue();
    verify(jwtUtils).isTokenExpired(token);
  }
  
  @Test
  @DisplayName("Should get token expiration time")
  void shouldGetTokenExpirationTime() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    Date expirationDate = new Date(1000L);
    when(jwtUtils.getExpirationDateFromToken(token)).thenReturn(expirationDate);
    
    // When
    long result = jwtTokenService.getTokenExpirationTime(token);
    
    // Then
    assertThat(result).isEqualTo(1000L);
    verify(jwtUtils).getExpirationDateFromToken(token);
  }
  
  @Test
  @DisplayName("Should return 0 when expiration date is null")
  void shouldReturnZeroWhenExpirationDateIsNull() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    when(jwtUtils.getExpirationDateFromToken(token)).thenReturn(null);
    
    // When
    long result = jwtTokenService.getTokenExpirationTime(token);
    
    // Then
    assertThat(result).isEqualTo(0L);
  }
  
  @Test
  @DisplayName("Should get username from token")
  void shouldGetUsernameFromToken() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String username = "testuser";
    when(jwtUtils.getUsernameFromJwtToken(token)).thenReturn(username);
    
    // When
    String result = jwtTokenService.getUsernameFromToken(token);
    
    // Then
    assertThat(result).isEqualTo(username);
    verify(jwtUtils).getUsernameFromJwtToken(token);
  }
}
