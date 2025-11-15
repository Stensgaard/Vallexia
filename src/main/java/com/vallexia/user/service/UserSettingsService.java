package com.vallexia.user.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.entity.User;
import com.vallexia.user.entity.UserSettings;
import com.vallexia.user.entity.enums.FirstDayOfWeek;
import com.vallexia.user.mapper.UserSettingsMapper;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.repository.UserSettingsRepository;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.util.LocaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user settings operations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class UserSettingsService {
    
    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;
    private final UserSettingsMapper userSettingsMapper;
    private final AuditService auditService;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param userSettingsRepository the user settings repository
     * @param userRepository the user repository
     * @param userSettingsMapper the user settings mapper
     * @param auditService the audit service
     */
    public UserSettingsService(UserSettingsRepository userSettingsRepository,
                              UserRepository userRepository,
                              UserSettingsMapper userSettingsMapper,
                              AuditService auditService) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
        this.userSettingsMapper = userSettingsMapper;
        this.auditService = auditService;
    }
    
    /**
     * Get user settings for user. Creates and persists default settings if none exist.
     * 
     * @param userId user ID
     * @return UserSettingsDto
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserSettingsDto getUserSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElse(null);
        
        if (settings == null) {
            // Return default settings as DTO without persisting
            // Settings will be persisted when user first updates them
            UserSettings defaultSettings = getDefaultSettings();
            defaultSettings.setUser(user);
            return userSettingsMapper.toUserSettingsDto(defaultSettings);
        }
        
        return userSettingsMapper.toUserSettingsDto(settings);
    }
    
    /**
     * Update user settings for user.
     * 
     * @param userId user ID
     * @param userSettingsDto updated settings data
     * @return updated UserSettingsDto
     * @throws UserNotFoundException if user not found
     */
    public UserSettingsDto updateUserSettings(Long userId, UserSettingsDto userSettingsDto) {
        log.info("Updating user settings for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                    String.format("User not found with id: %d. This may be due to account deletion or invalid user ID.", userId)
                ));
        
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserSettings newSettings = getDefaultSettings();
                    newSettings.setUser(user);
                    return newSettings;
                });
        
        // Update settings
        settings.setLanguage(userSettingsDto.getLanguage());
        settings.setCountry(userSettingsDto.getCountry());
        settings.setDateFormat(userSettingsDto.getDateFormat());
        settings.setTimezone(userSettingsDto.getTimezone());
        settings.setFirstDayOfWeek(userSettingsDto.getFirstDayOfWeek());
        settings.setMeasurementSystem(userSettingsDto.getMeasurementSystem());
        
        // Auto-populate separators and currency from country
        String country = userSettingsDto.getCountry();
        settings.setNumberDecimalSeparator(LocaleUtils.getDecimalSeparator(country));
        settings.setNumberThousandsSeparator(LocaleUtils.getThousandsSeparator(country));
        settings.setCurrency(LocaleUtils.getCurrencyFromCountry(country));
        
        UserSettings updatedSettings = userSettingsRepository.save(settings);
        
        // Audit log
        auditService.logEvent(
            EventType.PROFILE_UPDATE,
            userId,
            String.format("User settings updated for user ID: %d", userId)
        );
        
        log.info("User settings updated successfully for user ID: {}", userId);
        
        return userSettingsMapper.toUserSettingsDto(updatedSettings);
    }
    
    /**
     * Get default user settings object.
     * 
     * @return default UserSettings
     */
    public UserSettings getDefaultSettings() {
        UserSettings settings = new UserSettings();
        settings.setLanguage("en");
        String defaultCountry = "US";
        settings.setCountry(defaultCountry);
        settings.setDateFormat("MM/DD/YYYY");
        settings.setTimezone("UTC");
        settings.setFirstDayOfWeek(FirstDayOfWeek.MONDAY);
        settings.setMeasurementSystem("METRIC");
        // Auto-populate separators and currency from country
        settings.setNumberDecimalSeparator(LocaleUtils.getDecimalSeparator(defaultCountry));
        settings.setNumberThousandsSeparator(LocaleUtils.getThousandsSeparator(defaultCountry));
        settings.setCurrency(LocaleUtils.getCurrencyFromCountry(defaultCountry));
        return settings;
    }
    
    /**
     * Create default user settings for a new user.
     * 
     * @param user the user to create settings for
     * @return created UserSettings
     */
    @Transactional
    public UserSettings createDefaultSettings(User user) {
        UserSettings userSettings = getDefaultSettings();
        userSettings.setUser(user);
        UserSettings savedSettings = userSettingsRepository.save(userSettings);
        user.setUserSettings(savedSettings);
        return savedSettings;
    }
}
