package com.vallexia.config.unit.cache;

import com.vallexia.config.cache.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for RedisConfig.
 * Tests RedisTemplate bean creation with custom serializers.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("RedisConfig Tests")
class RedisConfigTest {
  
  @Test
  @DisplayName("Should create RedisTemplate with correct serializers")
  void shouldCreateRedisTemplateWithCorrectSerializers() {
    // Given
    RedisConfig config = new RedisConfig();
    RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
    
    // When
    RedisTemplate<String, Object> template = config.redisTemplate(connectionFactory);
    
    // Then
    assertThat(template).isNotNull();
    assertThat(template.getConnectionFactory()).isEqualTo(connectionFactory);
    assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
    assertThat(template.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
    assertThat(template.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
    assertThat(template.getHashValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
  }
  
  @Test
  @DisplayName("Should configure RedisTemplate with string keys and JSON values")
  void shouldConfigureRedisTemplateWithStringKeysAndJsonValues() {
    // Given
    RedisConfig config = new RedisConfig();
    RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
    
    // When
    RedisTemplate<String, Object> template = config.redisTemplate(connectionFactory);
    
    // Then
    assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
    assertThat(template.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
  }
}
