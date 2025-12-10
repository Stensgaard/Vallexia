package com.vallexia;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.vallexia.config.security.AccountSecurityProperties;
import com.vallexia.config.security.JwtProperties;
import com.vallexia.config.security.RateLimitingProperties;
import com.vallexia.config.security.SecurityProperties;
import com.vallexia.config.web.CorsProperties;
import com.vallexia.config.api.SpoonacularProperties;
import com.vallexia.config.audit.AuditProperties;

/**
 * Main Spring Boot application class for Vallexia Smart Meal Planning App.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-26
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    CorsProperties.class,
    JwtProperties.class,
    AccountSecurityProperties.class,
    SecurityProperties.class,
    AuditProperties.class,
    SpoonacularProperties.class,
    RateLimitingProperties.class
})
public class VallexiaApplication {
    public static void main(String[] args) {
        // Load .env file only in development (not in production)
        // Production should use system environment variables set by container orchestration
        String activeProfile = System.getProperty("spring.profiles.active");
        if (activeProfile == null || activeProfile.isEmpty()) {
            activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        
        // Only load .env if not in production profile
        boolean isProduction = activeProfile != null && activeProfile.contains("prod");
        
        if (!isProduction) {
            try {
                Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
                
                // Set system properties from .env file so Spring Boot can use them
                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // Only set if not already set as system property (system properties take precedence)
                    if (System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                });
                
                log.info("Loaded environment variables from .env file (development mode)");
            } catch (Exception e) {
                log.warn("Failed to load .env file: {}. Continuing with system environment variables.", e.getMessage());
            }
        } else {
            log.debug("Skipping .env file loading in production profile. Using system environment variables.");
        }
        
        SpringApplication.run(VallexiaApplication.class, args);
    }
}
