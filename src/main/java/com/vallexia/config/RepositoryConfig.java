package com.vallexia.config;

import com.vallexia.audit.repository.AuditLogRepository;
import com.vallexia.recipe.repository.IngredientNutritionRepository;
import com.vallexia.recipe.repository.RecipeRepository;
import com.vallexia.user.repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration class for Spring Data JPA repository setup.
 * 
 * <p>This class explicitly configures JPA repository scanning for user, audit, and recipe packages.
 * Redis repositories are enabled via auto-configuration and will be used for entity-based
 * Redis storage in the future (e.g., user sessions, shopping carts).
 * 
 * <p>Using {@code basePackageClasses} instead of package strings provides
 * type-safety and better IDE refactoring support.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = {
        UserRepository.class,
        AuditLogRepository.class,
        RecipeRepository.class,
        IngredientNutritionRepository.class
    })
public class RepositoryConfig {
    // Configuration only - no additional methods needed
    // Redis repositories are enabled via auto-configuration for future entity-based storage
}
