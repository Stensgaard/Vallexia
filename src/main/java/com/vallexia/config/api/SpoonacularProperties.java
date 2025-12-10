package com.vallexia.config.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Spoonacular API integration.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.spoonacular")
public class SpoonacularProperties {
  
  /**
   * Spoonacular API key for authentication.
   * Retrieved from environment variable SPOONACULAR_API_KEY.
   */
  @NotBlank(message = "Spoonacular API key must not be blank")
  private String apiKey;
  
  /**
   * Base URL for Spoonacular API.
   * Default: https://api.spoonacular.com
   */
  private String baseUrl = "https://api.spoonacular.com";
  
  /**
   * Cache TTL in hours.
   * Default: 1 hour (per Spoonacular Terms of Use).
   */
  @Min(value = 1, message = "Cache TTL must be at least 1 hour")
  private int cacheTtlHours = 1;
  
  /**
   * Rate limit per minute for API calls.
   * Default: 60 requests/minute (free tier limit).
   */
  @Min(value = 1, message = "Rate limit per minute must be at least 1")
  private int rateLimitPerMinute = 60;
  
  /**
   * Daily quota points for API calls.
   * Default: 50 points/day (free tier limit).
   */
  @Min(value = 1, message = "Daily quota points must be at least 1")
  private int dailyQuotaPoints = 50;
}
