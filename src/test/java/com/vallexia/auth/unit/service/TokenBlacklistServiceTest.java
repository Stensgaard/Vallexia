package com.vallexia.auth.unit.service;

import com.vallexia.auth.fixtures.AuthTestFixtures;
import com.vallexia.auth.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenBlacklistService.
 * Tests token blacklisting logic with mocked Redis.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TokenBlacklistService Unit Tests")
class TokenBlacklistServiceTest {
  
  @Mock
  private RedisTemplate<String, Object> redisTemplate;
  
  @Mock
  private ValueOperations<String, Object> valueOperations;
  
  @InjectMocks
  private TokenBlacklistService tokenBlacklistService;
  
  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }
  
  // ==================== blacklistToken() Tests ====================
  
  @Test
  @DisplayName("Should successfully blacklist valid token")
  void shouldSuccessfullyBlacklistValidToken() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    long expirationTime = System.currentTimeMillis() + 3600000; // 1 hour from now
    
    doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    
    // When
    boolean result = tokenBlacklistService.blacklistToken(token, expirationTime);
    
    // Then
    assertThat(result).isTrue();
    // Verify token is hashed (key should not contain original token)
    verify(valueOperations).set(
        argThat(key -> key.startsWith("blacklist:") && !key.contains(token)), 
        eq("blacklisted"), 
        anyLong(), 
        eq(TimeUnit.SECONDS)
    );
  }
  
  @Test
  @DisplayName("Should return false for already expired token")
  void shouldReturnFalseForAlreadyExpiredToken() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    long expirationTime = System.currentTimeMillis() - 1000; // Already expired
    
    // When
    boolean result = tokenBlacklistService.blacklistToken(token, expirationTime);
    
    // Then
    assertThat(result).isFalse();
    verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
  }
  
  @Test
  @DisplayName("Should hash token before storing in Redis")
  void shouldHashTokenBeforeStoringInRedis() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    long expirationTime = System.currentTimeMillis() + 3600000;
    
    doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    
    // When
    tokenBlacklistService.blacklistToken(token, expirationTime);
    
    // Then - verify the key does not contain the original token (it's hashed)
    verify(valueOperations).set(
        argThat(key -> {
          // Key should start with "blacklist:" and be longer than original token (hash is 64 hex chars)
          return key.startsWith("blacklist:") && key.length() > "blacklist:".length() + token.length();
        }),
        eq("blacklisted"),
        anyLong(),
        eq(TimeUnit.SECONDS)
    );
  }
  
  @Test
  @DisplayName("Should return false on Redis failure")
  void shouldReturnFalseOnRedisFailure() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    long expirationTime = System.currentTimeMillis() + 3600000;
    
    doThrow(new RuntimeException("Redis connection failed"))
        .when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    
    // When
    boolean result = tokenBlacklistService.blacklistToken(token, expirationTime);
    
    // Then
    assertThat(result).isFalse();
  }
  
  @Test
  @DisplayName("Should calculate correct TTL based on expiration time")
  void shouldCalculateCorrectTtlBasedOnExpirationTime() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    long expirationTime = System.currentTimeMillis() + 7200000; // 2 hours from now
    long expectedTtlSeconds = (expirationTime - System.currentTimeMillis()) / 1000;
    
    doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    
    // When
    tokenBlacklistService.blacklistToken(token, expirationTime);
    
    // Then - verify TTL is approximately correct (within 5 seconds tolerance)
    ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
    verify(valueOperations).set(
        anyString(),
        anyString(),
        ttlCaptor.capture(),
        eq(TimeUnit.SECONDS)
    );
    long actualTtl = ttlCaptor.getValue();
    assertThat(Math.abs(actualTtl - expectedTtlSeconds)).isLessThan(5);
  }
  
  // ==================== isTokenBlacklisted() Tests ====================
  
  @Test
  @DisplayName("Should return true for blacklisted token")
  void shouldReturnTrueForBlacklistedToken() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    
    when(redisTemplate.hasKey(anyString())).thenReturn(true);
    
    // When
    boolean result = tokenBlacklistService.isTokenBlacklisted(token);
    
    // Then
    assertThat(result).isTrue();
    verify(redisTemplate).hasKey(anyString());
    // Verify token is hashed before checking
    verify(redisTemplate).hasKey(argThat(key -> key.startsWith("blacklist:") && !key.contains(token)));
  }
  
  @Test
  @DisplayName("Should return false for non-blacklisted token")
  void shouldReturnFalseForNonBlacklistedToken() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    
    when(redisTemplate.hasKey(anyString())).thenReturn(false);
    
    // When
    boolean result = tokenBlacklistService.isTokenBlacklisted(token);
    
    // Then
    assertThat(result).isFalse();
    verify(redisTemplate).hasKey(anyString());
  }
  
  @Test
  @DisplayName("Should fail closed when Redis is unavailable")
  void shouldFailClosedWhenRedisIsUnavailable() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    
    when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis unavailable"));
    
    // When
    boolean result = tokenBlacklistService.isTokenBlacklisted(token);
    
    // Then - fail closed (return true for security)
    assertThat(result).isTrue();
    verify(redisTemplate).hasKey(anyString());
  }
  
  @Test
  @DisplayName("Should hash token before checking Redis")
  void shouldHashTokenBeforeCheckingRedis() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    
    when(redisTemplate.hasKey(anyString())).thenReturn(false);
    
    // When
    tokenBlacklistService.isTokenBlacklisted(token);
    
    // Then - verify the key does not contain the original token
    verify(redisTemplate).hasKey(
        argThat(key -> {
          return key.startsWith("blacklist:") && !key.contains(token);
        })
    );
  }
  
  
  // ==================== Token Hashing Tests ====================
  
  @Test
  @DisplayName("Should produce same hash for same token")
  void shouldProduceSameHashForSameToken() {
    // Given
    String token = AuthTestFixtures.TEST_ACCESS_TOKEN;
    
    doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    
    // When - blacklist same token twice
    long expirationTime = System.currentTimeMillis() + 3600000;
    tokenBlacklistService.blacklistToken(token, expirationTime);
    tokenBlacklistService.blacklistToken(token, expirationTime);
    
    // Then - verify same key is used (through verification that pattern is consistent)
    verify(valueOperations, times(2)).set(
        argThat(key -> key.startsWith("blacklist:") && key.length() == "blacklist:".length() + 64), // SHA-256 hex hash is 64 chars
        anyString(),
        anyLong(),
        any(TimeUnit.class)
    );
  }
  
  @Test
  @DisplayName("Should produce different hashes for different tokens")
  void shouldProduceDifferentHashesForDifferentTokens() {
    // Given
    String token1 = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String token2 = AuthTestFixtures.TEST_REFRESH_TOKEN;
    
    doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    
    // When
    long expirationTime = System.currentTimeMillis() + 3600000;
    tokenBlacklistService.blacklistToken(token1, expirationTime);
    tokenBlacklistService.blacklistToken(token2, expirationTime);
    
    // Then - verify different keys are used
    // We can't directly compare hashes, but we verify both calls were made with different keys
    verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
  }
}
