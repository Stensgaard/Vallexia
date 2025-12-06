package com.vallexia.user.unit.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedCurrency;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.entity.User;
import com.vallexia.user.entity.UserSettings;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.mapper.UserSettingsMapper;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.repository.UserSettingsRepository;
import com.vallexia.user.service.UserSettingsService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserSettingsService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserSettingsService Unit Tests")
class UserSettingsServiceTest {

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsMapper userSettingsMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserSettingsService userSettingsService;

    @Test
    @DisplayName("Country-specific defaults are derived from SupportedCountry metadata")
    void shouldDeriveDefaultsFromCountryMetadata() {
        UserSettings settings = userSettingsService.getDefaultSettingsForCountry("DK");

        assertThat(settings.getCountry()).isEqualTo(SupportedCountry.DK.getCountryCode());
        assertThat(settings.getLanguage()).isEqualTo(SupportedLocale.DA.getCode());
        assertThat(settings.getDateFormat()).isEqualTo(SupportedDateFormat.DD_MM_YYYY_DOT.name());
        assertThat(settings.getTimezone()).isEqualTo(SupportedTimezone.EUROPE_COPENHAGEN.getValue());
        assertThat(settings.getFirstDayOfWeek()).isEqualTo(SupportedCountry.DK.getFirstDayOfWeek());
        assertThat(settings.getMeasurementSystem()).isEqualTo(SupportedMeasurementSystem.METRIC);
        assertThat(settings.getNumberDecimalSeparator()).isEqualTo(SupportedCountry.DK.getDecimalSeparator());
        assertThat(settings.getNumberThousandsSeparator()).isEqualTo(SupportedCountry.DK.getThousandsSeparator());
        assertThat(settings.getCurrency()).isEqualTo(SupportedCountry.DK.getCurrencyCode());
    }

    @Test
    @DisplayName("Unknown country codes fall back to US defaults")
    void shouldFallbackToUsDefaultsWhenCountryUnknown() {
        UserSettings settings = userSettingsService.getDefaultSettingsForCountry("ZZ");

        assertThat(settings.getCountry()).isEqualTo(SupportedCountry.US.getCountryCode());
        assertThat(settings.getLanguage()).isEqualTo(SupportedLocale.EN.getCode());
        assertThat(settings.getDateFormat()).isEqualTo(SupportedDateFormat.MM_DD_YYYY.name());
        assertThat(settings.getTimezone()).isEqualTo(SupportedTimezone.AMERICA_NEW_YORK.getValue());
        assertThat(settings.getFirstDayOfWeek()).isEqualTo(SupportedCountry.US.getFirstDayOfWeek());
        assertThat(settings.getMeasurementSystem()).isEqualTo(SupportedMeasurementSystem.IMPERIAL);
    }

    @Test
    @DisplayName("Updating settings rejects unsupported measurement system codes")
    void shouldRejectUnsupportedMeasurementSystemDuringUpdate() {
        Long userId = 42L;
        User user = UserTestFixtures.createUser(userId);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userSettingsRepository.findByUserId(userId)).thenReturn(java.util.Optional.empty());

        UserSettingsDto dto = new UserSettingsDto();
        dto.setLanguage(SupportedLocale.EN.getCode());
        dto.setCountry(SupportedCountry.US.getCountryCode());
        dto.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
        dto.setTimezone(SupportedTimezone.AMERICA_NEW_YORK.getValue());
        dto.setFirstDayOfWeek(SupportedFirstDayOfWeek.SUNDAY.name());
        dto.setMeasurementSystem("GALACTIC");

        assertThatThrownBy(() -> userSettingsService.updateUserSettings(userId, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid measurement system value");
    }

    @Test
    @DisplayName("Currency override works when provided in DTO")
    void shouldUseCurrencyOverrideWhenProvided() {
        Long userId = 42L;
        User user = UserTestFixtures.createUser(userId);
        UserSettings existingSettings = UserTestFixtures.createUserSettings(user);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userSettingsRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingSettings));
        when(userSettingsRepository.save(existingSettings)).thenReturn(existingSettings);

        UserSettingsDto dto = new UserSettingsDto();
        dto.setLanguage(SupportedLocale.EN.getCode());
        dto.setCountry(SupportedCountry.US.getCountryCode());
        dto.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
        dto.setTimezone(SupportedTimezone.AMERICA_NEW_YORK.getValue());
        dto.setFirstDayOfWeek(SupportedFirstDayOfWeek.SUNDAY.name());
        dto.setMeasurementSystem(SupportedMeasurementSystem.IMPERIAL.name());
        dto.setCurrency(SupportedCurrency.DKK.getCode()); // Override to DKK even though country is US

        userSettingsService.updateUserSettings(userId, dto);

        assertThat(existingSettings.getCurrency()).isEqualTo(SupportedCurrency.DKK.getCode());
    }

    @Test
    @DisplayName("Currency defaults to country when not provided in DTO")
    void shouldUseCountryDefaultCurrencyWhenNotProvided() {
        Long userId = 42L;
        User user = UserTestFixtures.createUser(userId);
        UserSettings existingSettings = UserTestFixtures.createUserSettings(user);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userSettingsRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingSettings));
        when(userSettingsRepository.save(existingSettings)).thenReturn(existingSettings);

        UserSettingsDto dto = new UserSettingsDto();
        dto.setLanguage(SupportedLocale.DA.getCode());
        dto.setCountry(SupportedCountry.DK.getCountryCode());
        dto.setDateFormat(SupportedDateFormat.DD_MM_YYYY_DOT.name());
        dto.setTimezone(SupportedTimezone.EUROPE_COPENHAGEN.getValue());
        dto.setFirstDayOfWeek(SupportedFirstDayOfWeek.MONDAY.name());
        dto.setMeasurementSystem(SupportedMeasurementSystem.METRIC.name());
        dto.setCurrency(null); // Not provided, should use country default

        userSettingsService.updateUserSettings(userId, dto);

        assertThat(existingSettings.getCurrency()).isEqualTo(SupportedCountry.DK.getCurrencyCode());
        assertThat(existingSettings.getCurrency()).isEqualTo(SupportedCurrency.DKK.getCode());
    }

    @Test
    @DisplayName("Currency defaults to country when empty string is provided")
    void shouldUseCountryDefaultCurrencyWhenEmptyStringProvided() {
        Long userId = 42L;
        User user = UserTestFixtures.createUser(userId);
        UserSettings existingSettings = UserTestFixtures.createUserSettings(user);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userSettingsRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(existingSettings));
        when(userSettingsRepository.save(existingSettings)).thenReturn(existingSettings);

        UserSettingsDto dto = new UserSettingsDto();
        dto.setLanguage(SupportedLocale.EN.getCode());
        dto.setCountry(SupportedCountry.US.getCountryCode());
        dto.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
        dto.setTimezone(SupportedTimezone.AMERICA_NEW_YORK.getValue());
        dto.setFirstDayOfWeek(SupportedFirstDayOfWeek.SUNDAY.name());
        dto.setMeasurementSystem(SupportedMeasurementSystem.IMPERIAL.name());
        dto.setCurrency(""); // Empty string, should use country default

        userSettingsService.updateUserSettings(userId, dto);

        assertThat(existingSettings.getCurrency()).isEqualTo(SupportedCountry.US.getCurrencyCode());
        assertThat(existingSettings.getCurrency()).isEqualTo(SupportedCurrency.USD.getCode());
    }
    
    // ==================== getUserSettings() Tests ====================
    
    @Test
    @DisplayName("Should retrieve user settings successfully")
    void shouldRetrieveUserSettingsSuccessfully() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettings settings = UserTestFixtures.createUserSettings(user);
        UserSettingsDto expectedDto = UserTestFixtures.createUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(settings));
        when(userSettingsMapper.toUserSettingsDto(settings))
            .thenReturn(expectedDto);
        
        // When
        UserSettingsDto result = userSettingsService.getUserSettings(UserTestFixtures.TEST_USER_ID);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expectedDto.getId());
        assertThat(result.getUserId()).isEqualTo(expectedDto.getUserId());
        
        verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsRepository).findByUserId(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsMapper).toUserSettingsDto(settings);
    }
    
    @Test
    @DisplayName("Should return default settings when none exist")
    void shouldReturnDefaultSettingsWhenNoneExist() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettingsDto expectedDto = UserTestFixtures.createUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.empty());
        
        ArgumentCaptor<UserSettings> settingsCaptor = ArgumentCaptor.forClass(UserSettings.class);
        when(userSettingsMapper.toUserSettingsDto(settingsCaptor.capture()))
            .thenReturn(expectedDto);
        
        // When
        UserSettingsDto result = userSettingsService.getUserSettings(UserTestFixtures.TEST_USER_ID);
        
        // Then
        assertThat(result).isNotNull();
        UserSettings captured = settingsCaptor.getValue();
        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getCountry()).isEqualTo(SupportedCountry.US.getCountryCode());
        
        verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsRepository).findByUserId(UserTestFixtures.TEST_USER_ID);
    }
    
    @Test
    @DisplayName("Should throw UserNotFoundException when user doesn't exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Given
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userSettingsService.getUserSettings(UserTestFixtures.TEST_USER_ID))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
        
        verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsRepository, never()).findByUserId(any());
        verify(userSettingsMapper, never()).toUserSettingsDto(any());
    }
    
    @Test
    @DisplayName("Should map entity to DTO correctly")
    void shouldMapEntityToDtoCorrectly() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettings settings = UserTestFixtures.createUserSettings(user);
        UserSettingsDto expectedDto = UserTestFixtures.createUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(settings));
        when(userSettingsMapper.toUserSettingsDto(settings))
            .thenReturn(expectedDto);
        
        // When
        UserSettingsDto result = userSettingsService.getUserSettings(UserTestFixtures.TEST_USER_ID);
        
        // Then
        assertThat(result).isEqualTo(expectedDto);
        verify(userSettingsMapper).toUserSettingsDto(settings);
    }
    
    @Test
    @DisplayName("Should create default settings based on country")
    void shouldCreateDefaultSettingsBasedOnCountry() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettingsDto expectedDto = UserTestFixtures.createUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.empty());
        
        ArgumentCaptor<UserSettings> settingsCaptor = ArgumentCaptor.forClass(UserSettings.class);
        when(userSettingsMapper.toUserSettingsDto(settingsCaptor.capture()))
            .thenReturn(expectedDto);
        
        // When
        userSettingsService.getUserSettings(UserTestFixtures.TEST_USER_ID);
        
        // Then
        UserSettings captured = settingsCaptor.getValue();
        assertThat(captured.getCountry()).isEqualTo(SupportedCountry.US.getCountryCode());
        assertThat(captured.getLanguage()).isEqualTo(SupportedLocale.EN.getCode());
        assertThat(captured.getMeasurementSystem()).isEqualTo(SupportedMeasurementSystem.IMPERIAL);
    }
    
    // ==================== updateUserSettings() Additional Tests ====================
    
    @Test
    @DisplayName("Should update settings successfully with valid data")
    void shouldUpdateSettingsSuccessfullyWithValidData() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettings existingSettings = UserTestFixtures.createUserSettings(user);
        UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
        UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(existingSettings));
        when(userSettingsRepository.save(existingSettings))
            .thenReturn(existingSettings);
        when(userSettingsMapper.toUserSettingsDto(existingSettings))
            .thenReturn(expectedDto);
        
        // When
        UserSettingsDto result = userSettingsService.updateUserSettings(UserTestFixtures.TEST_USER_ID, updateDto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLanguage()).isEqualTo(updateDto.getLanguage());
        assertThat(result.getCountry()).isEqualTo(updateDto.getCountry());
        
        verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsRepository).findByUserId(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsRepository).save(existingSettings);
        verify(auditService).logEvent(eq(EventType.PROFILE_UPDATE), eq(UserTestFixtures.TEST_USER_ID), any(String.class));
    }
    
    @Test
    @DisplayName("Should create settings if they don't exist")
    void shouldCreateSettingsIfTheyDontExist() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
        UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.empty());
        
        ArgumentCaptor<UserSettings> settingsCaptor = ArgumentCaptor.forClass(UserSettings.class);
        when(userSettingsRepository.save(settingsCaptor.capture()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(userSettingsMapper.toUserSettingsDto(any(UserSettings.class)))
            .thenReturn(expectedDto);
        
        // When
        UserSettingsDto result = userSettingsService.updateUserSettings(UserTestFixtures.TEST_USER_ID, updateDto);
        
        // Then
        assertThat(result).isNotNull();
        UserSettings captured = settingsCaptor.getValue();
        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getLanguage()).isEqualTo(updateDto.getLanguage());
        
        verify(userSettingsRepository).save(any(UserSettings.class));
        verify(auditService).logEvent(any(), any(), any());
    }
    
    @Test
    @DisplayName("Should throw UserNotFoundException when user doesn't exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistOnUpdate() {
        // Given
        UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userSettingsService.updateUserSettings(UserTestFixtures.TEST_USER_ID, updateDto))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
        
        verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsRepository, never()).findByUserId(any());
        verify(userSettingsRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should update all settings fields")
    void shouldUpdateAllSettingsFields() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettings existingSettings = UserTestFixtures.createUserSettings(user);
        UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
        UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(existingSettings));
        when(userSettingsRepository.save(existingSettings))
            .thenReturn(existingSettings);
        when(userSettingsMapper.toUserSettingsDto(existingSettings))
            .thenReturn(expectedDto);
        
        // When
        userSettingsService.updateUserSettings(UserTestFixtures.TEST_USER_ID, updateDto);
        
        // Then
        assertThat(existingSettings.getLanguage()).isEqualTo(updateDto.getLanguage());
        assertThat(existingSettings.getCountry()).isEqualTo(updateDto.getCountry());
        assertThat(existingSettings.getDateFormat()).isEqualTo(updateDto.getDateFormat());
        assertThat(existingSettings.getTimezone()).isEqualTo(updateDto.getTimezone());
        assertThat(existingSettings.getMeasurementSystem().name()).isEqualTo(updateDto.getMeasurementSystem());
        
        verify(userSettingsRepository).save(existingSettings);
    }
    
    @Test
    @DisplayName("Should log audit event via AuditService")
    void shouldLogAuditEventViaAuditService() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettings existingSettings = UserTestFixtures.createUserSettings(user);
        UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
        UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(existingSettings));
        when(userSettingsRepository.save(existingSettings))
            .thenReturn(existingSettings);
        when(userSettingsMapper.toUserSettingsDto(existingSettings))
            .thenReturn(expectedDto);
        
        // When
        userSettingsService.updateUserSettings(UserTestFixtures.TEST_USER_ID, updateDto);
        
        // Then
        verify(auditService, times(1)).logEvent(
            eq(EventType.PROFILE_UPDATE),
            eq(UserTestFixtures.TEST_USER_ID),
            any(String.class)
        );
    }
    
    @Test
    @DisplayName("Should handle validation errors for unsupported values")
    void shouldHandleValidationErrorsForUnsupportedValues() {
        // Given
        User user = UserTestFixtures.createUser();
        UserSettingsDto updateDto = new UserSettingsDto();
        updateDto.setLanguage(SupportedLocale.EN.getCode());
        updateDto.setCountry(SupportedCountry.US.getCountryCode());
        updateDto.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
        updateDto.setTimezone(SupportedTimezone.AMERICA_NEW_YORK.getValue());
        updateDto.setFirstDayOfWeek(SupportedFirstDayOfWeek.SUNDAY.name());
        updateDto.setMeasurementSystem("INVALID_SYSTEM");
        
        when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userSettingsService.updateUserSettings(UserTestFixtures.TEST_USER_ID, updateDto))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid measurement system value");
        
        verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
        verify(userSettingsRepository, never()).save(any());
    }
}
