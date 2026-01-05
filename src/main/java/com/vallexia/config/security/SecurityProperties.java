package com.vallexia.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for general security settings.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-29
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
  
  /**
   * Comma-separated list of trusted proxy IP addresses.
   * Used for extracting real client IP from X-Forwarded-For headers.
   * Only IPs from trusted proxies will be used for client IP extraction.
   * Example: "172.17.0.1,10.0.0.1"
   */
  private String trustedProxies = "";
}
