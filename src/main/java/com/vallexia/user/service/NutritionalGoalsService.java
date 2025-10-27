package com.vallexia.user.service;

import com.vallexia.audit.entity.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.user.dto.NutritionalGoalsDto;
import com.vallexia.user.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import com.vallexia.user.mapper.NutritionalGoalsMapper;
import com.vallexia.user.repository.NutritionalGoalsRepository;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.nutrition.service.NutritionalCalculator;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for managing nutritional goals operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class NutritionalGoalsService {
    
    // Default nutritional goals constants
    private static final BigDecimal DEFAULT_DAILY_CALORIES = BigDecimal.valueOf(2000);
    private static final BigDecimal DEFAULT_DAILY_PROTEIN = BigDecimal.valueOf(150);
    private static final BigDecimal DEFAULT_DAILY_CARBS = BigDecimal.valueOf(250);
    private static final BigDecimal DEFAULT_DAILY_FATS = BigDecimal.valueOf(67);
    private static final BigDecimal DEFAULT_DAILY_FIBER = BigDecimal.valueOf(25);
    private static final BigDecimal DEFAULT_DAILY_SODIUM = BigDecimal.valueOf(2300);
    private static final BigDecimal DEFAULT_DAILY_SUGAR = BigDecimal.valueOf(50);
    
    // Macro percentage validation thresholds
    private static final BigDecimal MACRO_PERCENTAGE_MIN = BigDecimal.valueOf(95);
    private static final BigDecimal MACRO_PERCENTAGE_MAX = BigDecimal.valueOf(105);
    
    private final NutritionalGoalsRepository nutritionalGoalsRepository;
    private final UserRepository userRepository;
    private final NutritionalGoalsMapper nutritionalGoalsMapper;
    private final NutritionalCalculator nutritionalCalculator;
    private final AuditService auditService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param nutritionalGoalsRepository the nutritional goals repository
     * @param userRepository the user repository
     * @param nutritionalGoalsMapper the nutritional goals mapper
     * @param nutritionalCalculator the nutritional calculator
     * @param auditService the audit service
     */
    public NutritionalGoalsService(NutritionalGoalsRepository nutritionalGoalsRepository,
                                  UserRepository userRepository,
                                  NutritionalGoalsMapper nutritionalGoalsMapper,
                                  NutritionalCalculator nutritionalCalculator,
                                  AuditService auditService) {
        this.nutritionalGoalsRepository = nutritionalGoalsRepository;
        this.userRepository = userRepository;
        this.nutritionalGoalsMapper = nutritionalGoalsMapper;
        this.nutritionalCalculator = nutritionalCalculator;
        this.auditService = auditService;
    }
    
    /**
     * Get nutritional goals for user.
     * 
     * @param userId user ID
     * @return NutritionalGoalsDto
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public NutritionalGoalsDto getNutritionalGoals(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("User not found with id: %d. This may be due to account deletion or invalid user ID.", userId)
                ));
        
        NutritionalGoals goals = nutritionalGoalsRepository.findByUser(user)
                .orElseGet(() -> {
                    NutritionalGoals newGoals = new NutritionalGoals();
                    newGoals.setUser(user);
                    setDefaultNutritionalGoals(newGoals);
                    return newGoals;
                });
        
        return nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
    }
    
    /**
     * Update nutritional goals for user.
     * 
     * @param userId user ID
     * @param nutritionalGoalsDto updated goals data
     * @return updated NutritionalGoalsDto
     * @throws UserNotFoundException if user not found
     * @throws ValidationException if macro percentages don't add up to ~100%
     */
    public NutritionalGoalsDto updateNutritionalGoals(Long userId, NutritionalGoalsDto nutritionalGoalsDto) {
        log.info("Updating nutritional goals for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("User not found with id: %d. This may be due to account deletion or invalid user ID.", userId)
                ));
        
        NutritionalGoals goals = nutritionalGoalsRepository.findByUser(user)
                .orElseGet(() -> {
                    NutritionalGoals newGoals = new NutritionalGoals();
                    newGoals.setUser(user);
                    setDefaultNutritionalGoals(newGoals);
                    return newGoals;
                });
        
        // Update goals
        goals.setDailyCalories(nutritionalGoalsDto.getDailyCalories());
        goals.setDailyProtein(nutritionalGoalsDto.getDailyProtein());
        goals.setDailyCarbs(nutritionalGoalsDto.getDailyCarbs());
        goals.setDailyFats(nutritionalGoalsDto.getDailyFats());
        goals.setDailyFiber(nutritionalGoalsDto.getDailyFiber());
        goals.setDailySodium(nutritionalGoalsDto.getDailySodium());
        goals.setDailySugar(nutritionalGoalsDto.getDailySugar());
        goals.setGoalType(nutritionalGoalsDto.getGoalType());
        
        // Calculate percentages using NutritionalCalculator
        nutritionalCalculator.calculateMacroPercentages(goals);
        
        // Validate macro percentages add up to approximately 100%
        validateMacroPercentages(goals);
        
        NutritionalGoals updatedGoals = nutritionalGoalsRepository.save(goals);
        
        // Audit log
        auditService.logEvent(
            EventType.PROFILE_UPDATE,
            userId,
            String.format("Nutritional goals updated for user ID: %d", userId)
        );
        
        log.info("Nutritional goals updated successfully for user ID: {}", userId);
        
        return nutritionalGoalsMapper.toNutritionalGoalsDto(updatedGoals);
    }
    
    /**
     * Create default nutritional goals for a new user.
     * 
     * @param user the user to create goals for
     * @return created NutritionalGoals
     */
    @Transactional
    public NutritionalGoals createDefaultGoals(User user) {
        NutritionalGoals nutritionalGoals = new NutritionalGoals();
        nutritionalGoals.setUser(user);
        setDefaultNutritionalGoals(nutritionalGoals);
        
        NutritionalGoals savedGoals = nutritionalGoalsRepository.save(nutritionalGoals);
        user.setNutritionalGoals(savedGoals);
        return savedGoals;
    }
    
    /**
     * Delete nutritional goals for user (GDPR compliance).
     * 
     * @param userId user ID
     * @throws UserNotFoundException if user not found
     */
    public void deleteNutritionalGoals(Long userId) {
        log.info("Deleting nutritional goals for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("User not found with id: %d. This may be due to account deletion or invalid user ID.", userId)
                ));
        
        nutritionalGoalsRepository.findByUser(user).ifPresent(goals -> {
            nutritionalGoalsRepository.delete(goals);
            
            // Audit log
            auditService.logEvent(
                EventType.PROFILE_UPDATE,
                userId,
                String.format("Nutritional goals deleted for user ID: %d", userId)
            );
        });
        
        log.info("Nutritional goals deleted successfully for user ID: {}", userId);
    }
    
    /**
     * Set default nutritional goals values.
     * 
     * @param goals the nutritional goals to set defaults for
     */
    private void setDefaultNutritionalGoals(NutritionalGoals goals) {
        goals.setDailyCalories(DEFAULT_DAILY_CALORIES);
        goals.setDailyProtein(DEFAULT_DAILY_PROTEIN);
        goals.setDailyCarbs(DEFAULT_DAILY_CARBS);
        goals.setDailyFats(DEFAULT_DAILY_FATS);
        goals.setDailyFiber(DEFAULT_DAILY_FIBER);
        goals.setDailySodium(DEFAULT_DAILY_SODIUM);
        goals.setDailySugar(DEFAULT_DAILY_SUGAR);
    }
    
    /**
     * Validate that macro percentages add up to approximately 100% (95-105%).
     * 
     * @param goals the nutritional goals to validate
     * @throws ValidationException if macro percentages are invalid
     */
    private void validateMacroPercentages(NutritionalGoals goals) {
        if (goals.getProteinPercentage() != null && 
            goals.getCarbsPercentage() != null && 
            goals.getFatsPercentage() != null) {
            
            BigDecimal total = goals.getProteinPercentage()
                .add(goals.getCarbsPercentage())
                .add(goals.getFatsPercentage());
            
            if (total.compareTo(MACRO_PERCENTAGE_MIN) < 0 || 
                total.compareTo(MACRO_PERCENTAGE_MAX) > 0) {
                throw new ValidationException(
                    String.format("Macro percentages must add up to approximately 100%% (95-105%%). Current total: %.2f%%", total)
                );
            }
        }
    }
}
