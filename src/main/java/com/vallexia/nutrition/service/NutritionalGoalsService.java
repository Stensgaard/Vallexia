package com.vallexia.nutrition.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.nutrition.dto.NutritionalGoalsDto;
import com.vallexia.nutrition.entity.NutritionalGoals;
import com.vallexia.nutrition.enums.GoalType;
import com.vallexia.nutrition.mapper.NutritionalGoalsMapper;
import com.vallexia.nutrition.repository.NutritionalGoalsRepository;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for managing nutritional goals operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-27
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
    private static final String USER_NOT_FOUND_MSG =
        "User not found with id: %d. This may be due to account deletion or invalid user ID.";
    
    // Macro percentage validation thresholds
    private static final BigDecimal MACRO_PERCENTAGE_MIN = BigDecimal.valueOf(95);
    private static final BigDecimal MACRO_PERCENTAGE_MAX = BigDecimal.valueOf(105);
    
    private final NutritionalGoalsRepository nutritionalGoalsRepository;
    private final UserRepository userRepository;
    private final NutritionalGoalsMapper nutritionalGoalsMapper;
    private final MacroCalculator macroCalculator;
    private final AuditService auditService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param nutritionalGoalsRepository the nutritional goals repository
     * @param userRepository the user repository
     * @param nutritionalGoalsMapper the nutritional goals mapper
     * @param macroCalculator the macro calculator
     * @param auditService the audit service
     */
    public NutritionalGoalsService(NutritionalGoalsRepository nutritionalGoalsRepository,
                                  UserRepository userRepository,
                                  NutritionalGoalsMapper nutritionalGoalsMapper,
                                  MacroCalculator macroCalculator,
                                  AuditService auditService) {
        this.nutritionalGoalsRepository = nutritionalGoalsRepository;
        this.userRepository = userRepository;
        this.nutritionalGoalsMapper = nutritionalGoalsMapper;
        this.macroCalculator = macroCalculator;
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
                    String.format(USER_NOT_FOUND_MSG, userId)
                ));
        
        NutritionalGoals goals = nutritionalGoalsRepository.findByUser(user)
                .orElseGet(() -> {
                    NutritionalGoals newGoals = new NutritionalGoals();
                    newGoals.setUser(user);
                    setDefaultNutritionalGoals(newGoals);
                    return newGoals;
                });
        
        NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(goals);
        
        // Calculate macro calories using MacroCalculator
        calculateAndSetMacroCalories(dto, goals);
        
        return dto;
    }
    
    /**
     * Update nutritional goals for user.
     * 
     * @param userId user ID
     * @param nutritionalGoalsDto updated goals data
     * @return updated NutritionalGoalsDto
     * @throws UserNotFoundException if user not found
     * @throws com.vallexia.nutrition.exception.InvalidNutritionalDataException if nutritional data is invalid or out of range
     * @throws ValidationException if macro percentages don't add up to ~100%
     */
    public NutritionalGoalsDto updateNutritionalGoals(Long userId, NutritionalGoalsDto nutritionalGoalsDto) {
        log.info("Updating nutritional goals for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format(USER_NOT_FOUND_MSG, userId)
                ));
        
        NutritionalGoals goals = nutritionalGoalsRepository.findByUser(user)
                .orElseGet(() -> {
                    NutritionalGoals newGoals = new NutritionalGoals();
                    newGoals.setUser(user);
                    setDefaultNutritionalGoals(newGoals);
                    return newGoals;
                });
        
        // Create temporary goals object for validation before setting values on actual entity
        NutritionalGoals tempGoals = new NutritionalGoals();
        tempGoals.setUser(user);
        tempGoals.setDailyCalories(nutritionalGoalsDto.getDailyCalories());
        tempGoals.setDailyProtein(nutritionalGoalsDto.getDailyProtein());
        tempGoals.setDailyCarbs(nutritionalGoalsDto.getDailyCarbs());
        tempGoals.setDailyFats(nutritionalGoalsDto.getDailyFats());
        tempGoals.setDailyFiber(nutritionalGoalsDto.getDailyFiber());
        tempGoals.setDailySodium(nutritionalGoalsDto.getDailySodium());
        tempGoals.setDailySugar(nutritionalGoalsDto.getDailySugar());
        
        // Validate nutritional data before setting values on actual entity
        com.vallexia.nutrition.validator.NutritionalDataValidator.validateNutritionalGoals(tempGoals);
        
        // Update goals (validation passed)
        goals.setDailyCalories(nutritionalGoalsDto.getDailyCalories());
        goals.setDailyProtein(nutritionalGoalsDto.getDailyProtein());
        goals.setDailyCarbs(nutritionalGoalsDto.getDailyCarbs());
        goals.setDailyFats(nutritionalGoalsDto.getDailyFats());
        goals.setDailyFiber(nutritionalGoalsDto.getDailyFiber());
        goals.setDailySodium(nutritionalGoalsDto.getDailySodium());
        goals.setDailySugar(nutritionalGoalsDto.getDailySugar());
        
        // Convert String goalType to GoalType enum
        if (nutritionalGoalsDto.getGoalType() != null && !nutritionalGoalsDto.getGoalType().isEmpty()) {
            goals.setGoalType(GoalType.valueOf(nutritionalGoalsDto.getGoalType().toUpperCase()));
        }
        
        try {
            // Calculate percentages using MacroCalculator
            macroCalculator.calculateMacroPercentages(goals);
            
            // Validate macro percentages add up to approximately 100%
            validateMacroPercentages(goals);
        } catch (Exception e) {
            log.error("Error calculating or validating macro percentages for user ID {}: {}", userId, e.getMessage(), e);
            throw e; // Re-throw to let exception handler process it
        }
        
        NutritionalGoals updatedGoals = nutritionalGoalsRepository.save(goals);
        
        // Audit log
        auditService.logEvent(
            EventType.PROFILE_UPDATE,
            userId,
            String.format("Nutritional goals updated for user ID: %d", userId)
        );
        
        log.info("Nutritional goals updated successfully for user ID: {}", userId);
        
        NutritionalGoalsDto dto = nutritionalGoalsMapper.toNutritionalGoalsDto(updatedGoals);
        
        // Calculate macro calories using MacroCalculator
        calculateAndSetMacroCalories(dto, updatedGoals);
        
        return dto;
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
                    String.format(USER_NOT_FOUND_MSG, userId)
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
    
    /**
     * Calculate macro calories and set them in the DTO.
     * Uses MacroCalculator to ensure consistency with backend calculations.
     * 
     * @param dto the DTO to set macro calories on
     * @param goals the entity containing macro values in grams
     */
    private void calculateAndSetMacroCalories(NutritionalGoalsDto dto, NutritionalGoals goals) {
        dto.setProteinCalories(macroCalculator.calculateProteinCalories(goals.getDailyProtein()));
        dto.setCarbCalories(macroCalculator.calculateCarbCalories(goals.getDailyCarbs()));
        dto.setFatCalories(macroCalculator.calculateFatCalories(goals.getDailyFats()));
    }
}
