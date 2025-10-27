package com.vallexia.user.service;

import com.vallexia.audit.entity.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.entity.DietaryPreferences;
import com.vallexia.user.entity.User;
import com.vallexia.user.mapper.DietaryPreferencesMapper;
import com.vallexia.user.repository.DietaryPreferencesRepository;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing dietary preferences operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class DietaryPreferencesService {
    
    private final DietaryPreferencesRepository dietaryPreferencesRepository;
    private final UserRepository userRepository;
    private final DietaryPreferencesMapper dietaryPreferencesMapper;
    private final AuditService auditService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param dietaryPreferencesRepository the dietary preferences repository
     * @param userRepository the user repository
     * @param dietaryPreferencesMapper the dietary preferences mapper
     * @param auditService the audit service
     */
    public DietaryPreferencesService(DietaryPreferencesRepository dietaryPreferencesRepository,
                                    UserRepository userRepository,
                                    DietaryPreferencesMapper dietaryPreferencesMapper,
                                    AuditService auditService) {
        this.dietaryPreferencesRepository = dietaryPreferencesRepository;
        this.userRepository = userRepository;
        this.dietaryPreferencesMapper = dietaryPreferencesMapper;
        this.auditService = auditService;
    }
    
    /**
     * Get dietary preferences for user.
     * 
     * @param userId user ID
     * @return DietaryPreferencesDto
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public DietaryPreferencesDto getDietaryPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        DietaryPreferences preferences = dietaryPreferencesRepository.findByUser(user)
                .orElseGet(() -> {
                    DietaryPreferences newPreferences = new DietaryPreferences();
                    newPreferences.setUser(user);
                    return newPreferences;
                });
        
        return dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    }
    
    /**
     * Update dietary preferences for user.
     * 
     * @param userId user ID
     * @param dietaryPreferencesDto updated preferences data
     * @return updated DietaryPreferencesDto
     * @throws UserNotFoundException if user not found
     */
    public DietaryPreferencesDto updateDietaryPreferences(Long userId, DietaryPreferencesDto dietaryPreferencesDto) {
        log.info("Updating dietary preferences for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("User not found with id: %d. This may be due to account deletion or invalid user ID.", userId)
                ));
        
        DietaryPreferences preferences = dietaryPreferencesRepository.findByUser(user)
                .orElseGet(() -> {
                    DietaryPreferences newPreferences = new DietaryPreferences();
                    newPreferences.setUser(user);
                    return newPreferences;
                });
        
        // Update preferences
        preferences.setRestrictions(dietaryPreferencesDto.getRestrictions());
        preferences.setAllergies(dietaryPreferencesDto.getAllergies());
        preferences.setPreferredCuisines(dietaryPreferencesDto.getPreferredCuisines());
        preferences.setDislikedIngredients(dietaryPreferencesDto.getDislikedIngredients());
        preferences.setServingSizePreference(dietaryPreferencesDto.getServingSizePreference());
        
        DietaryPreferences updatedPreferences = dietaryPreferencesRepository.save(preferences);
        
        // Audit log
        auditService.logEvent(
            EventType.PROFILE_UPDATE,
            userId,
            String.format("Dietary preferences updated for user ID: %d", userId)
        );
        
        log.info("Dietary preferences updated successfully for user ID: {}", userId);
        
        return dietaryPreferencesMapper.toDietaryPreferencesDto(updatedPreferences);
    }
    
    /**
     * Create default dietary preferences for a new user.
     * 
     * @param user the user to create preferences for
     * @return created DietaryPreferences
     */
    @Transactional
    public DietaryPreferences createDefaultPreferences(User user) {
        DietaryPreferences dietaryPreferences = new DietaryPreferences();
        dietaryPreferences.setUser(user);
        DietaryPreferences savedPreferences = dietaryPreferencesRepository.save(dietaryPreferences);
        user.setDietaryPreferences(savedPreferences);
        return savedPreferences;
    }
    
    /**
     * Delete dietary preferences for user (GDPR compliance).
     * 
     * @param userId user ID
     * @throws UserNotFoundException if user not found
     */
    public void deleteDietaryPreferences(Long userId) {
        log.info("Deleting dietary preferences for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("User not found with id: %d. This may be due to account deletion or invalid user ID.", userId)
                ));
        
        dietaryPreferencesRepository.findByUser(user).ifPresent(preferences -> {
            dietaryPreferencesRepository.delete(preferences);
            
            // Audit log
            auditService.logEvent(
                EventType.PROFILE_UPDATE,
                userId,
                String.format("Dietary preferences deleted for user ID: %d", userId)
            );
        });
        
        log.info("Dietary preferences deleted successfully for user ID: {}", userId);
    }
}
