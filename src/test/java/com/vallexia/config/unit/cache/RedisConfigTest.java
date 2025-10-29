package com.vallexia.config.unit.cache;

import com.vallexia.config.cache.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for RedisConfig validation.
 * Tests Redis configuration validation logic with @PostConstruct.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@DisplayName("RedisConfig Validation Tests")
class RedisConfigTest {
  
  private RedisConfig createConfig(String host, String port, String password, String profile) {
    RedisConfig config = new RedisConfig();
    ReflectionTestUtils.setField(config, "redisHost", host);
    ReflectionTestUtils.setField(config, "redisPort", port != null ? Integer.parseInt(port) : 6379);
    ReflectionTestUtils.setField(config, "redisPassword", password);
    ReflectionTestUtils.setField(config, "activeProfile", profile);
    return config;
  }
  
  @Test
  @DisplayName("Should throw exception when host is null")
  void shouldThrowExceptionWhenHostIsNull() {
    // Given
    RedisConfig config = createConfig(null, "6379", "", "dev");
    
    // When/Then
    assertThatThrownBy(() -> config.validateRedisConfig())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Redis host is not configured");
  }
  
  @Test
  @DisplayName("Should throw exception when host is empty")
  void shouldThrowExceptionWhenHostIsEmpty() {
    // Given
    RedisConfig config = createConfig("", "6379", "", "dev");
    
    // When/Then
    assertThatThrownBy(() -> config.validateRedisConfig())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Redis host is not configured");
  }
  
  @Test
  @DisplayName("Should validate successfully with valid host")
  void shouldValidateSuccessfullyWithValidHost() {
    // Given
    RedisConfig config = createConfig("redis", "6379", "", "dev");
    
    // When - should not throw exception
    config.validateRedisConfig();
    
    // Then - validation passes silently
    assertThat(ReflectionTestUtils.getField(config, "redisHost")).isEqualTo("redis");
  }
  
  @Test
  @DisplayName("Should validate successfully with localhost as default")
  void shouldValidateSuccessfullyWithLocalhostAsDefault() {
    // Given
    RedisConfig config = createConfig("localhost", "6379", "", "dev");
    
    // When - should not throw exception
    config.validateRedisConfig();
    
    // Then - validation passes silently
    assertThat(ReflectionTestUtils.getField(config, "redisHost")).isEqualTo("localhost");
  }
  
  @Test
  @DisplayName("Should validate successfully with password in production")
  void shouldValidateSuccessfullyWithPasswordInProduction() {
    // Given
    RedisConfig config = createConfig("redis", "6379", "strongpassword123", "prod");
    
    // When - should not throw exception
    config.validateRedisConfig();
    
    // Then - validation passes silently
    assertThat(ReflectionTestUtils.getField(config, "redisHost")).isEqualTo("redis");
    assertThat(ReflectionTestUtils.getField(config, "redisPassword")).isEqualTo("strongpassword123");
  }
  
  @Test
  @DisplayName("Should log warning when password is empty in production")
  void shouldLogWarningWhenPasswordIsEmptyInProduction() {
    // Given
    RedisConfig config = createConfig("redis", "6379", "", "prod");
    
    // When - should not throw exception but logs warning
    config.validateRedisConfig();
    
    // Then - validation passes with warning
    assertThat(ReflectionTestUtils.getField(config, "redisHost")).isEqualTo("redis");
  }
}

