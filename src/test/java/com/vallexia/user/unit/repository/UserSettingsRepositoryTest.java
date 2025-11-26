package com.vallexia.user.unit.repository;

import com.vallexia.user.entity.User;
import com.vallexia.user.entity.UserSettings;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserSettingsRepository.
 * Tests repository query methods with mocked implementations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserSettingsRepository Unit Tests")
class UserSettingsRepositoryTest {
  
  @Mock
  private UserSettingsRepository userSettingsRepository;
  
  // ==================== findByUserId() Tests ====================
  
  @Test
  @DisplayName("Should find settings by user ID")
  void shouldFindSettingsByUserId() {
    // Given
    User user = UserTestFixtures.createUser();
    UserSettings settings = UserTestFixtures.createUserSettings(user);
    
    when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(settings));
    
    // When
    Optional<UserSettings> found = userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUser().getId()).isEqualTo(UserTestFixtures.TEST_USER_ID);
    assertThat(found.get().getId()).isEqualTo(settings.getId());
    verify(userSettingsRepository).findByUserId(UserTestFixtures.TEST_USER_ID);
  }
  
  @Test
  @DisplayName("Should return empty Optional when settings not found")
  void shouldReturnEmptyOptionalWhenSettingsNotFound() {
    // Given
    when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When
    Optional<UserSettings> found = userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(found).isEmpty();
    verify(userSettingsRepository).findByUserId(UserTestFixtures.TEST_USER_ID);
  }
  
  @Test
  @DisplayName("Should return empty Optional when user ID not found")
  void shouldReturnEmptyOptionalWhenUserIdNotFound() {
    // Given
    when(userSettingsRepository.findByUserId(999L))
        .thenReturn(Optional.empty());
    
    // When
    Optional<UserSettings> found = userSettingsRepository.findByUserId(999L);
    
    // Then
    assertThat(found).isEmpty();
    verify(userSettingsRepository).findByUserId(999L);
  }
  
  // ==================== save() Tests ====================
  
  @Test
  @DisplayName("Should save settings successfully")
  void shouldSaveSettingsSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    UserSettings settings = UserTestFixtures.createUserSettings(user);
    
    when(userSettingsRepository.save(settings))
        .thenReturn(settings);
    
    // When
    UserSettings saved = userSettingsRepository.save(settings);
    
    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getUser()).isEqualTo(user);
    verify(userSettingsRepository).save(settings);
  }
  
  @Test
  @DisplayName("Should update existing settings")
  void shouldUpdateExistingSettings() {
    // Given
    User user = UserTestFixtures.createUser();
    UserSettings settings = UserTestFixtures.createUserSettings(user);
    settings.setId(1L);
    
    when(userSettingsRepository.save(settings))
        .thenReturn(settings);
    
    // When
    UserSettings saved = userSettingsRepository.save(settings);
    
    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(1L);
    verify(userSettingsRepository).save(settings);
  }
  
  // ==================== delete() Tests ====================
  
  @Test
  @DisplayName("Should delete settings successfully")
  void shouldDeleteSettingsSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    UserSettings settings = UserTestFixtures.createUserSettings(user);
    
    doNothing().when(userSettingsRepository).delete(settings);
    
    // When
    userSettingsRepository.delete(settings);
    
    // Then
    verify(userSettingsRepository).delete(settings);
  }
  
  @Test
  @DisplayName("Should handle case when user has no settings")
  void shouldHandleCaseWhenUserHasNoSettings() {
    // Given
    when(userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When
    Optional<UserSettings> found = userSettingsRepository.findByUserId(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(found).isEmpty();
    verify(userSettingsRepository).findByUserId(UserTestFixtures.TEST_USER_ID);
  }
}
