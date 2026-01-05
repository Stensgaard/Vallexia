package com.vallexia.config.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Google Cloud Translation API integration.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-12-09
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.google.translation")
public class GoogleTranslationProperties {
  
  /**
   * Whether translation is enabled.
   * Default: true
   */
  private boolean enabled = true;
  
  /**
   * Google Cloud project ID.
   * Can be set via environment variable GOOGLE_CLOUD_PROJECT_ID.
   */
  private String projectId;
  
  /**
   * Default source language for translations.
   * Default: "en" (English, which is Spoonacular's default)
   */
  @NotBlank(message = "Default source language must not be blank")
  private String defaultSourceLanguage = "en";
}
