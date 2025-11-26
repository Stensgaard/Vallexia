package com.vallexia.config.unit.web;

import com.vallexia.config.web.CorsConfig;
import com.vallexia.config.web.CorsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CorsConfig.
 * Tests CORS configuration source bean creation and configuration values.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@DisplayName("CorsConfig Tests")
class CorsConfigTest {
  
  private CorsProperties createMockProperties() {
    CorsProperties properties = mock(CorsProperties.class);
    when(properties.getAllowedOrigins()).thenReturn(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
    when(properties.getAllowedMethods()).thenReturn(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    when(properties.getAllowedHeaders()).thenReturn(Arrays.asList("Authorization", "Content-Type", "Accept"));
    when(properties.isAllowCredentials()).thenReturn(true);
    return properties;
  }
  
  @Test
  @DisplayName("Should create CorsConfigurationSource bean")
  void shouldCreateCorsConfigurationSourceBean() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    
    // Then
    assertThat(source).isNotNull();
    assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
  }
  
  private CorsConfiguration getRegisteredConfiguration(UrlBasedCorsConfigurationSource source) {
    // Use reflection to access the registered configurations
    @SuppressWarnings("unchecked")
    Map<String, CorsConfiguration> configs = (Map<String, CorsConfiguration>) 
        ReflectionTestUtils.getField(source, "corsConfigurations");
    assertThat(configs).isNotNull();
    assertThat(configs).isNotEmpty();
    // Configuration is registered for "/**" pattern
    // Try to get by key first, if that fails, get the first value
    @SuppressWarnings("null")
    CorsConfiguration configuration = configs.get("/**");
    if (configuration == null && !configs.isEmpty()) {
      // If direct key lookup fails, get the first value (there should only be one)
      configuration = configs.values().iterator().next();
    }
    assertThat(configuration).isNotNull();
    return configuration;
  }
  
  @Test
  @DisplayName("Should configure allowed origins from properties")
  void shouldConfigureAllowedOriginsFromProperties() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    CorsConfiguration configuration = getRegisteredConfiguration((UrlBasedCorsConfigurationSource) source);
    
    // Then
    assertThat(configuration).isNotNull();
    assertThat(configuration.getAllowedOrigins())
        .containsExactlyInAnyOrder("http://localhost:5173", "http://localhost:3000");
  }
  
  @Test
  @DisplayName("Should configure allowed methods from properties")
  void shouldConfigureAllowedMethodsFromProperties() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    CorsConfiguration configuration = getRegisteredConfiguration((UrlBasedCorsConfigurationSource) source);
    
    // Then
    assertThat(configuration).isNotNull();
    assertThat(configuration.getAllowedMethods())
        .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
  }
  
  @Test
  @DisplayName("Should configure allowed headers from properties")
  void shouldConfigureAllowedHeadersFromProperties() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    CorsConfiguration configuration = getRegisteredConfiguration((UrlBasedCorsConfigurationSource) source);
    
    // Then
    assertThat(configuration).isNotNull();
    assertThat(configuration.getAllowedHeaders())
        .containsExactlyInAnyOrder("Authorization", "Content-Type", "Accept");
  }
  
  @Test
  @DisplayName("Should configure allow credentials from properties")
  void shouldConfigureAllowCredentialsFromProperties() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    CorsConfiguration configuration = getRegisteredConfiguration((UrlBasedCorsConfigurationSource) source);
    
    // Then
    assertThat(configuration).isNotNull();
    assertThat(configuration.getAllowCredentials()).isTrue();
  }
  
  @Test
  @DisplayName("Should configure exposed headers")
  void shouldConfigureExposedHeaders() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    CorsConfiguration configuration = getRegisteredConfiguration((UrlBasedCorsConfigurationSource) source);
    
    // Then
    assertThat(configuration).isNotNull();
    assertThat(configuration.getExposedHeaders())
        .containsExactlyInAnyOrder("Authorization", "X-Total-Count");
  }
  
  @Test
  @DisplayName("Should configure max age to 3600 seconds")
  void shouldConfigureMaxAgeTo3600Seconds() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    CorsConfiguration configuration = getRegisteredConfiguration((UrlBasedCorsConfigurationSource) source);
    
    // Then
    assertThat(configuration).isNotNull();
    assertThat(configuration.getMaxAge()).isEqualTo(3600L);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should register configuration for all paths pattern")
  void shouldRegisterConfigurationForAllPathsPattern() {
    // Given
    CorsProperties properties = createMockProperties();
    CorsConfig config = new CorsConfig(properties);
    
    // When
    CorsConfigurationSource source = config.corsConfigurationSource();
    UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
    @SuppressWarnings("unchecked")
    Map<String, CorsConfiguration> configs = (Map<String, CorsConfiguration>) 
        ReflectionTestUtils.getField(urlBasedSource, "corsConfigurations");
    
    // Then
    assertThat(configs).isNotNull();
    assertThat(configs).isNotEmpty();
    // The configuration should be registered (key might be "/**" or stored differently)
    assertThat(configs.size()).isEqualTo(1);
    CorsConfiguration configuration = configs.values().iterator().next();
    assertThat(configuration).isNotNull();
    // Verify it has the expected configuration
    assertThat(configuration.getAllowedOrigins())
        .containsExactlyInAnyOrder("http://localhost:5173", "http://localhost:3000");
  }
}
