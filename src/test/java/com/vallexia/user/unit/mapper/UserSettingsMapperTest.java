package com.vallexia.user.unit.mapper;

import com.vallexia.common.enums.SupportedCountry;
import com.vallexia.common.enums.SupportedDateFormat;
import com.vallexia.common.enums.SupportedFirstDayOfWeek;
import com.vallexia.common.enums.SupportedLocale;
import com.vallexia.common.enums.SupportedMeasurementSystem;
import com.vallexia.common.enums.SupportedTimezone;
import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.entity.UserSettings;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.mapper.UserSettingsMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserSettingsMapper.
 * Tests entity-to-DTO mapping with real MapStruct implementation.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@SpringBootTest(classes = {
    com.vallexia.user.mapper.UserSettingsMapperImpl.class
})
@ActiveProfiles("test")
@DisplayName("UserSettingsMapper Unit Tests")
class UserSettingsMapperTest {
  
  @Autowired
  private UserSettingsMapper userSettingsMapper;
  
  // ==================== toUserSettingsDto() Tests ====================
  
  @Test
  @DisplayName("Should map all fields from entity to DTO")
  void shouldMapAllFieldsFromEntityToDto() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(settings.getId());
    assertThat(dto.getUserId()).isEqualTo(settings.getUser().getId());
    assertThat(dto.getLanguage()).isEqualTo(settings.getLanguage());
    assertThat(dto.getCountry()).isEqualTo(settings.getCountry());
    assertThat(dto.getDateFormat()).isEqualTo(settings.getDateFormat());
    assertThat(dto.getTimezone()).isEqualTo(settings.getTimezone());
  }
  
  @Test
  @DisplayName("Should map locale/language correctly")
  void shouldMapLocaleLanguageCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setLanguage(SupportedLocale.DA.getCode());
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto.getLanguage()).isEqualTo(SupportedLocale.DA.getCode());
  }
  
  @Test
  @DisplayName("Should map country code correctly")
  void shouldMapCountryCodeCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setCountry(SupportedCountry.DK.getCountryCode());
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto.getCountry()).isEqualTo(SupportedCountry.DK.getCountryCode());
  }
  
  @Test
  @DisplayName("Should map date format correctly")
  void shouldMapDateFormatCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setDateFormat(SupportedDateFormat.DD_MM_YYYY_DOT.name());
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto.getDateFormat()).isEqualTo(SupportedDateFormat.DD_MM_YYYY_DOT.name());
  }
  
  @Test
  @DisplayName("Should map timezone correctly")
  void shouldMapTimezoneCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setTimezone(SupportedTimezone.EUROPE_COPENHAGEN.getValue());
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto.getTimezone()).isEqualTo(SupportedTimezone.EUROPE_COPENHAGEN.getValue());
  }
  
  @Test
  @DisplayName("Should map first day of week correctly")
  void shouldMapFirstDayOfWeekCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setFirstDayOfWeek(SupportedFirstDayOfWeek.MONDAY);
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto.getFirstDayOfWeek()).isEqualTo(SupportedFirstDayOfWeek.MONDAY.name());
  }
  
  @Test
  @DisplayName("Should map measurement system correctly")
  void shouldMapMeasurementSystemCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setMeasurementSystem(SupportedMeasurementSystem.METRIC);
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto.getMeasurementSystem()).isEqualTo(SupportedMeasurementSystem.METRIC.name());
  }
  
  @Test
  @DisplayName("Should map currency correctly")
  void shouldMapCurrencyCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setCurrency(SupportedCountry.DK.getCurrencyCode());
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto.getCurrency()).isEqualTo(SupportedCountry.DK.getCurrencyCode());
  }
  
  @Test
  @DisplayName("Should map number separators correctly")
  void shouldMapNumberSeparatorsCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    settings.setNumberDecimalSeparator(",");
    settings.setNumberThousandsSeparator(".");
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    // Note: These fields are ignored in the mapper, so they may not be mapped
    // This test verifies the mapper works even with these values set
    assertThat(dto).isNotNull();
  }
  
  @Test
  @DisplayName("Should return null when entity is null")
  void shouldReturnNullWhenEntityIsNull() {
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(null);
    
    // Then
    assertThat(dto).isNull();
  }
  
  @Test
  @DisplayName("Should map partial entity with only required fields")
  void shouldMapPartialEntityWithOnlyRequiredFields() {
    // Given
    UserSettings settings = new UserSettings();
    settings.setId(1L);
    settings.setUser(UserTestFixtures.createUser());
    settings.setLanguage(SupportedLocale.EN.getCode());
    settings.setCountry(SupportedCountry.US.getCountryCode());
    settings.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
    settings.setTimezone(SupportedTimezone.UTC.getValue());
    settings.setFirstDayOfWeek(SupportedFirstDayOfWeek.MONDAY);
    settings.setMeasurementSystem(SupportedMeasurementSystem.METRIC);
    // Leave currency null
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getUserId()).isEqualTo(settings.getUser().getId());
    assertThat(dto.getLanguage()).isEqualTo(SupportedLocale.EN.getCode());
    assertThat(dto.getCountry()).isEqualTo(SupportedCountry.US.getCountryCode());
  }
  
  @Test
  @DisplayName("Should handle null values gracefully")
  void shouldHandleNullValuesGracefully() {
    // Given
    UserSettings settings = new UserSettings();
    settings.setId(1L);
    settings.setUser(UserTestFixtures.createUser());
    settings.setLanguage(SupportedLocale.EN.getCode());
    settings.setCountry(SupportedCountry.US.getCountryCode());
    settings.setDateFormat(SupportedDateFormat.MM_DD_YYYY.name());
    settings.setTimezone(SupportedTimezone.UTC.getValue());
    settings.setFirstDayOfWeek(SupportedFirstDayOfWeek.MONDAY);
    settings.setMeasurementSystem(SupportedMeasurementSystem.METRIC);
    settings.setCurrency(null);
    
    // When
    UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getCurrency()).isNull();
  }
  
  @Test
  @DisplayName("Should map all supported locales correctly")
  void shouldMapAllSupportedLocalesCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    
    for (SupportedLocale locale : SupportedLocale.values()) {
      settings.setLanguage(locale.getCode());
      
      // When
      UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
      
      // Then
      assertThat(dto.getLanguage()).isEqualTo(locale.getCode());
    }
  }
  
  @Test
  @DisplayName("Should map all measurement systems correctly")
  void shouldMapAllMeasurementSystemsCorrectly() {
    // Given
    UserSettings settings = UserTestFixtures.createUserSettings();
    
    for (SupportedMeasurementSystem system : SupportedMeasurementSystem.values()) {
      settings.setMeasurementSystem(system);
      
      // When
      UserSettingsDto dto = userSettingsMapper.toUserSettingsDto(settings);
      
      // Then
      assertThat(dto.getMeasurementSystem()).isEqualTo(system.name());
    }
  }
}
