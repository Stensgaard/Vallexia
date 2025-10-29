package com.vallexia.config.cache;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for token blacklisting and caching.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class RedisConfig {
  
  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;
  
  @Value("${spring.data.redis.port}")
  private int redisPort;
  
  @Value("${spring.data.redis.password:}")
  private String redisPassword;
  
  @Value("${spring.profiles.active:dev}")
  private String activeProfile;
  
  @PostConstruct
  public void validateRedisConfig() {
    if (redisHost == null || redisHost.trim().isEmpty()) {
      throw new IllegalStateException(
          "Redis host is not configured. Set REDIS_HOST environment variable "
              + "or spring.data.redis.host property.");
    }
    
    // In production, Redis password should be required for security
    if ("prod".equals(activeProfile) && (redisPassword == null || redisPassword.trim().isEmpty())) {
      log.warn("Redis password is not set in production environment. "
          + "This is a security risk. Set REDIS_PASSWORD environment variable.");
      // Note: Not throwing exception to allow for environments where Redis is secured
      // by other means (e.g., network isolation, VPN)
    }
    
    log.info("Redis configuration validated - Host: {}, Port: {}, Password set: {}", 
        redisHost, redisPort, (redisPassword != null && !redisPassword.isEmpty()));
  }
  
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setHostName(redisHost);
    config.setPort(redisPort);
    if (redisPassword != null && !redisPassword.isEmpty()) {
      config.setPassword(redisPassword);
    }
    return new LettuceConnectionFactory(config);
  }
  
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    
    // Use String serializer for keys
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    
    // Use JSON serializer for values
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    
    template.afterPropertiesSet();
    return template;
  }
}
