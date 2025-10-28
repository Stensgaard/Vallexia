package com.vallexia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.vallexia.config.security.AccountSecurityProperties;
import com.vallexia.config.security.JwtProperties;
import com.vallexia.config.web.CorsProperties;
import com.vallexia.config.audit.AuditProperties;

/**
 * Main Spring Boot application class for Vallexia Smart Meal Planning App.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    CorsProperties.class,
    JwtProperties.class,
    AccountSecurityProperties.class,
    AuditProperties.class
})
public class VallexiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(VallexiaApplication.class, args);
    }
}
