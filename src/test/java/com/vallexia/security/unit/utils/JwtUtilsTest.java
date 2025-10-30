package com.vallexia.security.unit.utils;

import com.vallexia.config.security.JwtProperties;
import com.vallexia.security.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtUtils.
 * Tests JWT token generation, validation, null handling, and role extraction.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@DisplayName("JwtUtils Unit Tests")
class JwtUtilsTest {
  
  private JwtUtils jwtUtils;
  private JwtProperties jwtProperties;
  private static final String TEST_SECRET = "a".repeat(64); // 64 character secret
  private static final long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes
  private static final long REFRESH_TOKEN_EXPIRATION = 86400000L; // 24 hours
  
  @BeforeEach
  void setUp() {
    jwtProperties = new JwtProperties();
    jwtProperties.setSecret(TEST_SECRET);
    jwtProperties.setAccessTokenExpiration(ACCESS_TOKEN_EXPIRATION);
    jwtProperties.setRefreshTokenExpiration(REFRESH_TOKEN_EXPIRATION);
    jwtUtils = new JwtUtils(jwtProperties);
  }
  
  // ==================== Null Checks Tests ====================
  
  @Test
  @DisplayName("Should throw IllegalArgumentException when token is null in getUsernameFromJwtToken")
  void shouldThrowExceptionWhenTokenIsNullInGetUsername() {
    // When/Then
    assertThatThrownBy(() -> jwtUtils.getUsernameFromJwtToken(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Token cannot be null or empty");
  }
  
  @Test
  @DisplayName("Should throw IllegalArgumentException when token is empty in getUsernameFromJwtToken")
  void shouldThrowExceptionWhenTokenIsEmptyInGetUsername() {
    // When/Then
    assertThatThrownBy(() -> jwtUtils.getUsernameFromJwtToken(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Token cannot be null or empty");
  }
  
  @Test
  @DisplayName("Should return null when token is null in getUserIdFromJwtToken")
  void shouldReturnNullWhenTokenIsNullInGetUserId() {
    // When
    Long result = jwtUtils.getUserIdFromJwtToken(null);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should return null when token is empty in getUserIdFromJwtToken")
  void shouldReturnNullWhenTokenIsEmptyInGetUserId() {
    // When
    Long result = jwtUtils.getUserIdFromJwtToken("");
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should return null when token is null in getRolesFromJwtToken")
  void shouldReturnNullWhenTokenIsNullInGetRoles() {
    // When
    List<String> result = jwtUtils.getRolesFromJwtToken(null);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should return false when token is null in validateJwtToken")
  void shouldReturnFalseWhenTokenIsNullInValidate() {
    // When
    boolean result = jwtUtils.validateJwtToken(null);
    
    // Then
    assertThat(result).isFalse();
  }
  
  @Test
  @DisplayName("Should return true when token is empty in isTokenExpired")
  void shouldReturnTrueWhenTokenIsEmptyInIsTokenExpired() {
    // When
    boolean result = jwtUtils.isTokenExpired("");
    
    // Then
    assertThat(result).isTrue();
  }
  
  @Test
  @DisplayName("Should return null when token is null in getExpirationDateFromToken")
  void shouldReturnNullWhenTokenIsNullInGetExpirationDate() {
    // When
    java.util.Date result = jwtUtils.getExpirationDateFromToken(null);
    // Then
    assertThat(result).isNull();
  }
  
  // ==================== Token Generation Tests ====================
  
  @Test
  @DisplayName("Should generate valid access token with username, userId, and roles")
  void shouldGenerateValidAccessToken() {
    // Given
    String username = "testuser";
    Long userId = 123L;
    List<String> roles = Arrays.asList("ROLE_USER");
    
    // When
    String token = jwtUtils.generateAccessToken(username, userId, roles);
    
    // Then
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    assertThat(jwtUtils.getUsernameFromJwtToken(token)).isEqualTo(username);
    assertThat(jwtUtils.getUserIdFromJwtToken(token)).isEqualTo(userId);
    assertThat(jwtUtils.getRolesFromJwtToken(token)).isEqualTo(roles);
  }
  
  @Test
  @DisplayName("Should generate valid refresh token with username, userId, and roles")
  void shouldGenerateValidRefreshToken() {
    // Given
    String username = "testuser";
    Long userId = 123L;
    List<String> roles = Arrays.asList("ROLE_USER");
    
    // When
    String token = jwtUtils.generateRefreshToken(username, userId, roles);
    
    // Then
    assertThat(token).isNotNull().isNotEmpty();
    assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    assertThat(jwtUtils.getUsernameFromJwtToken(token)).isEqualTo(username);
    assertThat(jwtUtils.getUserIdFromJwtToken(token)).isEqualTo(userId);
    assertThat(jwtUtils.getRolesFromJwtToken(token)).isEqualTo(roles);
  }
  
  // ==================== Role Extraction Tests ====================
  
  @Test
  @DisplayName("Should safely extract roles when token contains mixed types in roles claim")
  void shouldSafelyExtractRolesWithMixedTypes() throws Exception {
    // Given - create a token with mixed types in roles (should filter out non-strings)
    SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 123L);
    // Mix of String and non-String in roles
    List<Object> mixedRoles = new ArrayList<>();
    mixedRoles.add("ROLE_USER");
    mixedRoles.add(123); // Non-string
    mixedRoles.add("ROLE_ADMIN");
    claims.put("roles", mixedRoles);
    
    String token = Jwts.builder()
        .claims(claims)
        .subject("testuser")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
    
    // When
    List<String> result = jwtUtils.getRolesFromJwtToken(token);
    
    // Then - should only contain String roles
    assertThat(result).isNotNull();
    assertThat(result).containsExactly("ROLE_USER", "ROLE_ADMIN");
    assertThat(result).doesNotContain("123");
  }
  
  @Test
  @DisplayName("Should return null when roles claim is not a List")
  void shouldReturnNullWhenRolesIsNotList() throws Exception {
    // Given - token with roles as non-List
    SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 123L);
    claims.put("roles", "ROLE_USER"); // String instead of List
    
    String token = Jwts.builder()
        .claims(claims)
        .subject("testuser")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
    
    // When
    List<String> result = jwtUtils.getRolesFromJwtToken(token);
    
    // Then
    assertThat(result).isNull();
  }
  
  @Test
  @DisplayName("Should return null when roles claim is missing")
  void shouldReturnNullWhenRolesClaimMissing() throws Exception {
    // Given - token without roles claim
    SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 123L);
    // No roles claim
    
    String token = Jwts.builder()
        .claims(claims)
        .subject("testuser")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
    
    // When
    List<String> result = jwtUtils.getRolesFromJwtToken(token);
    
    // Then
    assertThat(result).isNull();
  }
  
  // ==================== Validation Tests ====================
  
  @Test
  @DisplayName("Should return false for invalid token in validateJwtToken")
  void shouldReturnFalseForInvalidToken() {
    // When
    boolean result = jwtUtils.validateJwtToken("invalid.token.here");
    
    // Then
    assertThat(result).isFalse();
  }
  
  @Test
  @DisplayName("Should return false for expired token")
  void shouldReturnFalseForExpiredToken() throws Exception {
    // Given - create expired token
    SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 123L);
    claims.put("roles", Arrays.asList("ROLE_USER"));
    
    String token = Jwts.builder()
        .claims(claims)
        .subject("testuser")
        .issuedAt(new Date(System.currentTimeMillis() - 100000))
        .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired
        .signWith(key)
        .compact();
    
    // When
    boolean result = jwtUtils.validateJwtToken(token);
    
    // Then
    assertThat(result).isFalse();
  }
  
  @Test
  @DisplayName("Should correctly identify expired token")
  void shouldCorrectlyIdentifyExpiredToken() throws Exception {
    // Given - create expired token
    SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 123L);
    claims.put("roles", Arrays.asList("ROLE_USER"));
    
    String token = Jwts.builder()
        .claims(claims)
        .subject("testuser")
        .issuedAt(new Date(System.currentTimeMillis() - 100000))
        .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired
        .signWith(key)
        .compact();
    
    // When
    boolean expired = jwtUtils.isTokenExpired(token);
    
    // Then
    assertThat(expired).isTrue();
  }
  
  @Test
  @DisplayName("Should correctly identify non-expired token")
  void shouldCorrectlyIdentifyNonExpiredToken() {
    // Given
    String username = "testuser";
    Long userId = 123L;
    List<String> roles = Arrays.asList("ROLE_USER");
    
    String token = jwtUtils.generateAccessToken(username, userId, roles);
    
    // When
    boolean expired = jwtUtils.isTokenExpired(token);
    
    // Then
    assertThat(expired).isFalse();
  }
  
  // ==================== User ID Extraction Tests ====================
  
  @Test
  @DisplayName("Should extract user ID correctly from token")
  void shouldExtractUserIdCorrectly() {
    // Given
    Long expectedUserId = 456L;
    String token = jwtUtils.generateAccessToken("testuser", expectedUserId, Arrays.asList("ROLE_USER"));
    
    // When
    Long userId = jwtUtils.getUserIdFromJwtToken(token);
    
    // Then
    assertThat(userId).isEqualTo(expectedUserId);
  }
  
  @Test
  @DisplayName("Should return null when userId claim is missing")
  void shouldReturnNullWhenUserIdClaimMissing() throws Exception {
    // Given - token without userId claim
    SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", Arrays.asList("ROLE_USER"));
    // No userId claim
    
    String token = Jwts.builder()
        .claims(claims)
        .subject("testuser")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
    
    // When
    Long userId = jwtUtils.getUserIdFromJwtToken(token);
    
    // Then
    assertThat(userId).isNull();
  }
  
  @Test
  @DisplayName("Should return null when userId claim is not a Number")
  void shouldReturnNullWhenUserIdIsNotNumber() throws Exception {
    // Given - token with userId as String
    SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", "not-a-number");
    claims.put("roles", Arrays.asList("ROLE_USER"));
    
    String token = Jwts.builder()
        .claims(claims)
        .subject("testuser")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
    
    // When
    Long userId = jwtUtils.getUserIdFromJwtToken(token);
    
    // Then
    assertThat(userId).isNull();
  }
}

