package com.vallexia.user.unit.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.entity.DietaryPreferences;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.mapper.DietaryPreferencesMapper;
import com.vallexia.user.repository.DietaryPreferencesRepository;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.service.DietaryPreferencesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DietaryPreferencesService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DietaryPreferencesService Unit Tests")
class DietaryPreferencesServiceTest {
  
  @Mock
  private DietaryPreferencesRepository dietaryPreferencesRepository;
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private DietaryPreferencesMapper dietaryPreferencesMapper;
  
  @Mock
  private AuditService auditService;
  
  @InjectMocks
  private DietaryPreferencesService dietaryPreferencesService;
  
  // ==================== getDietaryPreferences() Tests ====================
  
  @Test
  @DisplayName("Should retrieve dietary preferences successfully")
  void shouldRetrieveDietaryPreferencesSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences(user);
    DietaryPreferencesDto expectedDto = UserTestFixtures.createDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(preferences));
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(preferences))
        .thenReturn(expectedDto);
    
    // When
    DietaryPreferencesDto result = dietaryPreferencesService.getDietaryPreferences(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(expectedDto.getId());
    assertThat(result.getUserId()).isEqualTo(expectedDto.getUserId());
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(dietaryPreferencesRepository).findByUser(user);
    verify(dietaryPreferencesMapper).toDietaryPreferencesDto(preferences);
  }
  
  @Test
  @DisplayName("Should return default preferences when none exist")
  void shouldReturnDefaultPreferencesWhenNoneExist() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferencesDto expectedDto = new DietaryPreferencesDto();
    expectedDto.setUserId(UserTestFixtures.TEST_USER_ID);
    expectedDto.setRestrictions(new HashSet<>());
    expectedDto.setAllergies(new HashSet<>());
    expectedDto.setPreferredCuisines(new HashSet<>());
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    ArgumentCaptor<DietaryPreferences> preferencesCaptor = ArgumentCaptor.forClass(DietaryPreferences.class);
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(preferencesCaptor.capture()))
        .thenReturn(expectedDto);
    
    // When
    DietaryPreferencesDto result = dietaryPreferencesService.getDietaryPreferences(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    DietaryPreferences captured = preferencesCaptor.getValue();
    assertThat(captured.getUser()).isEqualTo(user);
    assertThat(captured.getRestrictions()).isEmpty();
    assertThat(captured.getAllergies()).isEmpty();
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(dietaryPreferencesRepository).findByUser(user);
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    // Given
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> dietaryPreferencesService.getDietaryPreferences(UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(dietaryPreferencesRepository, never()).findByUser(any());
    verify(dietaryPreferencesMapper, never()).toDietaryPreferencesDto(any());
  }
  
  @Test
  @DisplayName("Should map entity to DTO correctly")
  void shouldMapEntityToDtoCorrectly() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences(user);
    DietaryPreferencesDto expectedDto = UserTestFixtures.createDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(preferences));
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(preferences))
        .thenReturn(expectedDto);
    
    // When
    DietaryPreferencesDto result = dietaryPreferencesService.getDietaryPreferences(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isEqualTo(expectedDto);
    verify(dietaryPreferencesMapper).toDietaryPreferencesDto(preferences);
  }
  
  // ==================== updateDietaryPreferences() Tests ====================
  
  @Test
  @DisplayName("Should update preferences successfully with valid data")
  void shouldUpdatePreferencesSuccessfullyWithValidData() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences existingPreferences = UserTestFixtures.createDietaryPreferences(user);
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(existingPreferences));
    when(dietaryPreferencesRepository.save(existingPreferences))
        .thenReturn(existingPreferences);
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(existingPreferences))
        .thenReturn(expectedDto);
    
    // When
    DietaryPreferencesDto result = dietaryPreferencesService.updateDietaryPreferences(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRestrictions()).isEqualTo(updateDto.getRestrictions());
    assertThat(result.getAllergies()).isEqualTo(updateDto.getAllergies());
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(dietaryPreferencesRepository).findByUser(user);
    verify(dietaryPreferencesRepository).save(existingPreferences);
    verify(auditService).logEvent(eq(EventType.PROFILE_UPDATE), eq(UserTestFixtures.TEST_USER_ID), any(String.class));
    verify(dietaryPreferencesMapper).toDietaryPreferencesDto(existingPreferences);
  }
  
  @Test
  @DisplayName("Should create preferences if they don't exist")
  void shouldCreatePreferencesIfTheyDontExist() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    ArgumentCaptor<DietaryPreferences> preferencesCaptor = ArgumentCaptor.forClass(DietaryPreferences.class);
    when(dietaryPreferencesRepository.save(preferencesCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(any(DietaryPreferences.class)))
        .thenReturn(expectedDto);
    
    // When
    DietaryPreferencesDto result = dietaryPreferencesService.updateDietaryPreferences(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    DietaryPreferences captured = preferencesCaptor.getValue();
    assertThat(captured.getUser()).isEqualTo(user);
    assertThat(captured.getRestrictions()).isEqualTo(updateDto.getRestrictions());
    
    verify(dietaryPreferencesRepository).save(any(DietaryPreferences.class));
    verify(auditService).logEvent(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistOnUpdate() {
    // Given
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> dietaryPreferencesService.updateDietaryPreferences(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(dietaryPreferencesRepository, never()).findByUser(any());
    verify(dietaryPreferencesRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should update all preference fields (restrictions, allergies, cuisines)")
  void shouldUpdateAllPreferenceFields() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences existingPreferences = UserTestFixtures.createDietaryPreferences(user);
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(existingPreferences));
    when(dietaryPreferencesRepository.save(existingPreferences))
        .thenReturn(existingPreferences);
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(existingPreferences))
        .thenReturn(expectedDto);
    
    // When
    dietaryPreferencesService.updateDietaryPreferences(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(existingPreferences.getRestrictions()).isEqualTo(updateDto.getRestrictions());
    assertThat(existingPreferences.getAllergies()).isEqualTo(updateDto.getAllergies());
    assertThat(existingPreferences.getPreferredCuisines()).isEqualTo(updateDto.getPreferredCuisines());
    
    verify(dietaryPreferencesRepository).save(existingPreferences);
  }
  
  @Test
  @DisplayName("Should save updated entity to repository")
  void shouldSaveUpdatedEntityToRepository() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences existingPreferences = UserTestFixtures.createDietaryPreferences(user);
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(existingPreferences));
    when(dietaryPreferencesRepository.save(existingPreferences))
        .thenReturn(existingPreferences);
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(existingPreferences))
        .thenReturn(expectedDto);
    
    // When
    dietaryPreferencesService.updateDietaryPreferences(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    verify(dietaryPreferencesRepository, times(1)).save(existingPreferences);
  }
  
  @Test
  @DisplayName("Should log audit event via AuditService")
  void shouldLogAuditEventViaAuditService() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences existingPreferences = UserTestFixtures.createDietaryPreferences(user);
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(existingPreferences));
    when(dietaryPreferencesRepository.save(existingPreferences))
        .thenReturn(existingPreferences);
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(existingPreferences))
        .thenReturn(expectedDto);
    
    // When
    dietaryPreferencesService.updateDietaryPreferences(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    verify(auditService, times(1)).logEvent(
        eq(EventType.PROFILE_UPDATE),
        eq(UserTestFixtures.TEST_USER_ID),
        any(String.class)
    );
  }
  
  @Test
  @DisplayName("Should handle empty/null collections")
  void shouldHandleEmptyNullCollections() {
    // Given
    User user = UserTestFixtures.createUser();
    DietaryPreferences existingPreferences = UserTestFixtures.createDietaryPreferences(user);
    DietaryPreferencesDto updateDto = new DietaryPreferencesDto();
    updateDto.setRestrictions(new HashSet<>());
    updateDto.setAllergies(null);
    updateDto.setPreferredCuisines(new HashSet<>());
    
    DietaryPreferencesDto expectedDto = new DietaryPreferencesDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(dietaryPreferencesRepository.findByUser(user))
        .thenReturn(Optional.of(existingPreferences));
    when(dietaryPreferencesRepository.save(existingPreferences))
        .thenReturn(existingPreferences);
    when(dietaryPreferencesMapper.toDietaryPreferencesDto(existingPreferences))
        .thenReturn(expectedDto);
    
    // When
    DietaryPreferencesDto result = dietaryPreferencesService.updateDietaryPreferences(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    verify(dietaryPreferencesRepository).save(existingPreferences);
  }
  
  // ==================== createDefaultPreferences() Tests ====================
  
  @Test
  @DisplayName("Should create default preferences for new user")
  void shouldCreateDefaultPreferencesForNewUser() {
    // Given
    User user = UserTestFixtures.createUser();
    
    ArgumentCaptor<DietaryPreferences> preferencesCaptor = ArgumentCaptor.forClass(DietaryPreferences.class);
    when(dietaryPreferencesRepository.save(preferencesCaptor.capture()))
        .thenAnswer(invocation -> {
          DietaryPreferences prefs = invocation.getArgument(0);
          prefs.setId(1L);
          return prefs;
        });
    
    // When
    DietaryPreferences result = dietaryPreferencesService.createDefaultPreferences(user);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    DietaryPreferences captured = preferencesCaptor.getValue();
    assertThat(captured.getUser()).isEqualTo(user);
    assertThat(user.getDietaryPreferences()).isEqualTo(result);
    
    verify(dietaryPreferencesRepository).save(any(DietaryPreferences.class));
  }
  
  @Test
  @DisplayName("Should associate preferences with user")
  void shouldAssociatePreferencesWithUser() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(dietaryPreferencesRepository.save(any(DietaryPreferences.class)))
        .thenAnswer(invocation -> {
          DietaryPreferences prefs = invocation.getArgument(0);
          prefs.setId(1L);
          return prefs;
        });
    
    // When
    DietaryPreferences result = dietaryPreferencesService.createDefaultPreferences(user);
    
    // Then
    assertThat(user.getDietaryPreferences()).isEqualTo(result);
    assertThat(result.getUser()).isEqualTo(user);
  }
  
  @Test
  @DisplayName("Should save preferences to repository")
  void shouldSavePreferencesToRepository() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(dietaryPreferencesRepository.save(any(DietaryPreferences.class)))
        .thenAnswer(invocation -> {
          DietaryPreferences prefs = invocation.getArgument(0);
          prefs.setId(1L);
          return prefs;
        });
    
    // When
    dietaryPreferencesService.createDefaultPreferences(user);
    
    // Then
    verify(dietaryPreferencesRepository, times(1)).save(any(DietaryPreferences.class));
  }
}
