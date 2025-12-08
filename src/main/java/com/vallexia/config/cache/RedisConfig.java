package com.vallexia.config.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for token blacklisting and caching.
 * 
 * <p>Spring Boot auto-configures {@link RedisConnectionFactory} from
 * {@code spring.data.redis.*} properties. This configuration only provides
 * a custom {@link RedisTemplate} with specific serializers for keys and values.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Configuration
public class RedisConfig {
  
  /**
   * Creates a custom RedisTemplate with specific serializers.
   * 
   * <p>Uses Spring Boot's auto-configured {@link RedisConnectionFactory}.
   * Keys are serialized as strings, values as JSON.
   * 
   * @param connectionFactory auto-configured Redis connection factory
   * @return configured RedisTemplate instance
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    
    // Use String serializer for both keys and values
    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);
    template.setValueSerializer(stringSerializer);
    template.setHashValueSerializer(stringSerializer);
    
    template.afterPropertiesSet();
    return template;
  }
}
