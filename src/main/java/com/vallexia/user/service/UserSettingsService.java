package com.vallexia.user.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.entity.User;
import com.vallexia.user.entity.UserSettings;
import com.vallexia.user.mapper.UserSettingsMapper;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.repository.UserSettingsRepository;
import com.vallexia.user.exception.UserNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user settings operations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-15
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
     * Get user's locale code, falling back to default if unavailable.
     * 
     * @param userId the user ID (can be null)
     * @return locale code (defaults to English if user is null or settings unavailable)
     */
    @Transactional(readOnly = true)
    public String getUserLocale(Long userId) {
        if (userId == null) {
            return SupportedLocale.EN.getCode();
        }
        try {
            String locale = getUserSettings(userId).getLanguage();
            return SupportedLocale.fromCode(locale).isPresent() 
                ? locale 
                : SupportedLocale.EN.getCode();
        } catch (Exception e) {
            log.debug("Could not fetch user locale for user ID {}: {}", userId, e.getMessage());
            return SupportedLocale.EN.getCode();
        }
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
        settings.setDateFormat(resolveDateFormat(userSettingsDto.getDateFormat()).name());
        settings.setTimezone(userSettingsDto.getTimezone());
        settings.setFirstDayOfWeek(resolveFirstDayOfWeek(userSettingsDto.getFirstDayOfWeek()));
        settings.setMeasurementSystem(resolveMeasurementSystem(userSettingsDto.getMeasurementSystem()));
        
        // Auto-populate separators from country
        SupportedCountry countryMeta = resolveCountryOrDefault(userSettingsDto.getCountry());
        settings.setNumberDecimalSeparator(countryMeta.getDecimalSeparator());
        settings.setNumberThousandsSeparator(countryMeta.getThousandsSeparator());
        
        // Currency: use override if provided, otherwise use country default
        if (userSettingsDto.getCurrency() != null && !userSettingsDto.getCurrency().isEmpty()) {
            settings.setCurrency(userSettingsDto.getCurrency());
        } else {
            settings.setCurrency(countryMeta.getCurrencyCode());
        }
        
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
        return getDefaultSettingsForCountry(SupportedCountry.US.getCountryCode());
    }
    
    /**
     * Get default user settings for a specific country.
     * Derives all settings from the country code.
     * 
     * @param countryCode country code (ISO 3166-1 alpha-2)
     * @return UserSettings with country-specific defaults
     */
    public UserSettings getDefaultSettingsForCountry(String countryCode) {
        SupportedCountry country = resolveCountryOrDefault(countryCode);
        UserSettings settings = new UserSettings();

        settings.setLanguage(country.getLocale().getCode());
        settings.setCountry(country.getCountryCode());
        settings.setDateFormat(country.getDefaultDateFormat().name());
        settings.setTimezone(country.getDefaultTimezone().getValue());
        settings.setFirstDayOfWeek(country.getFirstDayOfWeek());
        settings.setMeasurementSystem(country.getMeasurementSystem());
        settings.setNumberDecimalSeparator(country.getDecimalSeparator());
        settings.setNumberThousandsSeparator(country.getThousandsSeparator());
        settings.setCurrency(country.getCurrencyCode());

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
        return createDefaultSettings(user, SupportedCountry.US.getCountryCode());
    }
    
    /**
     * Create default user settings for a new user with country-specific defaults.
     * 
     * @param user the user to create settings for
     * @param countryCode country code to derive defaults from
     * @return created UserSettings
     */
    @Transactional
    public UserSettings createDefaultSettings(User user, String countryCode) {
        UserSettings userSettings = getDefaultSettingsForCountry(countryCode);
        userSettings.setUser(user);
        UserSettings savedSettings = userSettingsRepository.save(userSettings);
        user.setUserSettings(savedSettings);
        return savedSettings;
    }

    private SupportedFirstDayOfWeek resolveFirstDayOfWeek(String code) {
        return SupportedFirstDayOfWeek.fromCode(code)
                .orElseThrow(() -> new ValidationException(
                        String.format("Invalid first day of week value: %s", code)
                ));
    }

    private SupportedCountry resolveCountryOrDefault(String countryCode) {
        return SupportedCountry.fromCountry(countryCode)
                .orElse(SupportedCountry.US);
    }

    private SupportedDateFormat resolveDateFormat(String code) {
        return SupportedDateFormat.fromCode(code)
                .orElseThrow(() -> new ValidationException(
                        String.format("Invalid date format value: %s", code)
                ));
    }

    private SupportedMeasurementSystem resolveMeasurementSystem(String code) {
        return SupportedMeasurementSystem.fromCode(code)
                .orElseThrow(() -> new ValidationException(
                        String.format("Invalid measurement system value: %s", code)
                ));
    }
}
