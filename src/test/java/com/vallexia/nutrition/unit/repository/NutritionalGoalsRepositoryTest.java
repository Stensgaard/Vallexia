package com.vallexia.nutrition.unit.repository;

import com.vallexia.nutrition.entity.NutritionalGoals;
import com.vallexia.nutrition.repository.NutritionalGoalsRepository;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NutritionalGoalsRepository.
 * Tests repository query methods with mocked implementations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NutritionalGoalsRepository Unit Tests")
class NutritionalGoalsRepositoryTest {
  
  @Mock
  private NutritionalGoalsRepository nutritionalGoalsRepository;
  
  // ==================== findByUser() Tests ====================
  
  @Test
  @DisplayName("Should find goals by user")
  void shouldFindGoalsByUser() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(goals));
    
    // When
    Optional<NutritionalGoals> found = nutritionalGoalsRepository.findByUser(user);
    
    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUser()).isEqualTo(user);
    assertThat(found.get().getId()).isEqualTo(goals.getId());
    verify(nutritionalGoalsRepository).findByUser(user);
  }
  
  @Test
  @DisplayName("Should return empty Optional when goals not found")
  void shouldReturnEmptyOptionalWhenGoalsNotFound() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    // When
    Optional<NutritionalGoals> found = nutritionalGoalsRepository.findByUser(user);
    
    // Then
    assertThat(found).isEmpty();
    verify(nutritionalGoalsRepository).findByUser(user);
  }
  
  // ==================== save() Tests ====================
  
  @Test
  @DisplayName("Should save goals successfully")
  void shouldSaveGoalsSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    
    when(nutritionalGoalsRepository.save(goals))
        .thenReturn(goals);
    
    // When
    NutritionalGoals saved = nutritionalGoalsRepository.save(goals);
    
    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getUser()).isEqualTo(user);
    verify(nutritionalGoalsRepository).save(goals);
  }
  
  @Test
  @DisplayName("Should update existing goals")
  void shouldUpdateExistingGoals() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    goals.setId(1L);
    
    when(nutritionalGoalsRepository.save(goals))
        .thenReturn(goals);
    
    // When
    NutritionalGoals saved = nutritionalGoalsRepository.save(goals);
    
    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(1L);
    verify(nutritionalGoalsRepository).save(goals);
  }
  
  // ==================== delete() Tests ====================
  
  @Test
  @DisplayName("Should delete goals successfully")
  void shouldDeleteGoalsSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    
    doNothing().when(nutritionalGoalsRepository).delete(goals);
    
    // When
    nutritionalGoalsRepository.delete(goals);
    
    // Then
    verify(nutritionalGoalsRepository).delete(goals);
  }
  
  @Test
  @DisplayName("Should handle case when user has no goals")
  void shouldHandleCaseWhenUserHasNoGoals() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    // When
    Optional<NutritionalGoals> found = nutritionalGoalsRepository.findByUser(user);
    
    // Then
    assertThat(found).isEmpty();
    verify(nutritionalGoalsRepository).findByUser(user);
  }
}
