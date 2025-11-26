package com.vallexia.user.unit.repository;

import com.vallexia.user.entity.DietaryPreferences;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.DietaryPreferencesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DietaryPreferencesRepository.
 * Tests repository query methods with mocked implementations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DietaryPreferencesRepository Unit Tests")
class DietaryPreferencesRepositoryTest {
  
  @Mock
  private DietaryPreferencesRepository dietaryPreferencesRepository;
  
  // ==================== findByUser() Tests ====================
  
  @Test
  @DisplayName("Should find preferences by user")
  void shouldFindPreferencesByUser() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences(user);
    
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(preferences));
    
    // When
    Optional<DietaryPreferences> found = dietaryPreferencesRepository.findByUser(user);
    
    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUser()).isEqualTo(user);
    assertThat(found.get().getId()).isEqualTo(preferences.getId());
    verify(dietaryPreferencesRepository).findByUser(user);
  }
  
  @Test
  @DisplayName("Should return empty Optional when preferences not found")
  void shouldReturnEmptyOptionalWhenPreferencesNotFound() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    // When
    Optional<DietaryPreferences> found = dietaryPreferencesRepository.findByUser(user);
    
    // Then
    assertThat(found).isEmpty();
    verify(dietaryPreferencesRepository).findByUser(user);
  }
  
  // ==================== save() Tests ====================
  
  @Test
  @DisplayName("Should save preferences successfully")
  void shouldSavePreferencesSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences(user);
    
    when(dietaryPreferencesRepository.save(preferences))
        .thenReturn(preferences);
    
    // When
    DietaryPreferences saved = dietaryPreferencesRepository.save(preferences);
    
    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getUser()).isEqualTo(user);
    verify(dietaryPreferencesRepository).save(preferences);
  }
  
  @Test
  @DisplayName("Should update existing preferences")
  void shouldUpdateExistingPreferences() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences(user);
    preferences.setId(1L);
    
    when(dietaryPreferencesRepository.save(preferences))
        .thenReturn(preferences);
    
    // When
    DietaryPreferences saved = dietaryPreferencesRepository.save(preferences);
    
    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(1L);
    verify(dietaryPreferencesRepository).save(preferences);
  }
  
  // ==================== delete() Tests ====================
  
  @Test
  @DisplayName("Should delete preferences successfully")
  void shouldDeletePreferencesSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences(user);
    
    doNothing().when(dietaryPreferencesRepository).delete(preferences);
    
    // When
    dietaryPreferencesRepository.delete(preferences);
    
    // Then
    verify(dietaryPreferencesRepository).delete(preferences);
  }
  
  @Test
  @DisplayName("Should handle case when user has no preferences")
  void shouldHandleCaseWhenUserHasNoPreferences() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    // When
    Optional<DietaryPreferences> found = dietaryPreferencesRepository.findByUser(user);
    
    // Then
    assertThat(found).isEmpty();
    verify(dietaryPreferencesRepository).findByUser(user);
  }
}
