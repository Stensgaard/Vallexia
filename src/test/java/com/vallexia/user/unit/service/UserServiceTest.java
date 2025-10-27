package com.vallexia.user.unit.service;

import com.vallexia.audit.entity.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.exception.ValidationException;
import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.entity.User;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.mapper.UserMapper;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.service.UserService;
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
 * Unit tests for UserService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService Unit Tests")
class UserServiceTest {
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private UserMapper userMapper;
  
  @Mock
  private AuditService auditService;
  
  @InjectMocks
  private UserService userService;
  
  // ==================== getUserProfile() Tests ====================
  
  @Test
  @DisplayName("Should retrieve user profile successfully")
  void shouldRetrieveUserProfileSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(userMapper.toUserProfileDto(user))
        .thenReturn(expectedDto);
    
    // When
    UserProfileDto result = userService.getUserProfile(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(expectedDto.getId());
    assertThat(result.getUsername()).isEqualTo(expectedDto.getUsername());
    assertThat(result.getEmail()).isEqualTo(expectedDto.getEmail());
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(userMapper).toUserProfileDto(user);
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    // Given
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> userService.getUserProfile(UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(userMapper, never()).toUserProfileDto(any());
  }
  
  @Test
  @DisplayName("Should map entity to DTO correctly")
  void shouldMapEntityToDtoCorrectly() {
    // Given
    User user = UserTestFixtures.createUser();
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(userMapper.toUserProfileDto(user))
        .thenReturn(expectedDto);
    
    // When
    UserProfileDto result = userService.getUserProfile(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isEqualTo(expectedDto);
    verify(userMapper).toUserProfileDto(user);
  }
  
  // ==================== updateUserProfile() Tests ====================
  
  @Test
  @DisplayName("Should update profile successfully with valid data")
  void shouldUpdateProfileSuccessfullyWithValidData() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    User updatedUser = UserTestFixtures.createUser();
    updatedUser.setEmail(updateDto.getEmail());
    updatedUser.setFirstName(updateDto.getFirstName());
    updatedUser.setLastName(updateDto.getLastName());
    updatedUser.setProfilePictureUrl(updateDto.getProfilePictureUrl());
    updatedUser.setHouseholdSize(updateDto.getHouseholdSize());
    updatedUser.setMealsPerDay(updateDto.getMealsPerDay());
    
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(updateDto.getEmail()))
        .thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(updatedUser);
    when(userMapper.toUserProfileDto(updatedUser))
        .thenReturn(expectedDto);
    
    // When
    UserProfileDto result = userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("updated@example.com");
    assertThat(result.getFirstName()).isEqualTo("Updated");
    assertThat(result.getLastName()).isEqualTo("Name");
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(userRepository).existsByEmail(updateDto.getEmail());
    verify(userRepository).save(any(User.class));
    verify(auditService).logEvent(eq(EventType.PROFILE_UPDATE), eq(UserTestFixtures.TEST_USER_ID), any(String.class));
    verify(userMapper).toUserProfileDto(updatedUser);
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistOnUpdate() {
    // Given
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(userRepository, never()).existsByEmail(any());
    verify(userRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should throw ValidationException when email is already in use by another user")
  void shouldThrowValidationExceptionWhenEmailIsAlreadyInUse() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    String newEmail = "updated@example.com";
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(newEmail))
        .thenReturn(true); // Email already in use
    
    // When & Then
    assertThatThrownBy(() -> userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Email is already in use");
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(userRepository).existsByEmail(newEmail);
    verify(userRepository, never()).save(any());
    verify(auditService, never()).logEvent(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should not update username (immutable field)")
  void shouldNotUpdateUsername() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    updateDto.setUsername("newusername"); // Try to update username
    
    User updatedUser = UserTestFixtures.createUser();
    updatedUser.setEmail(updateDto.getEmail());
    updatedUser.setFirstName(updateDto.getFirstName());
    updatedUser.setLastName(updateDto.getLastName());
    
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(updateDto.getEmail()))
        .thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(updatedUser);
    when(userMapper.toUserProfileDto(updatedUser))
        .thenReturn(expectedDto);
    
    // When
    UserProfileDto result = userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getUsername()).isEqualTo(UserTestFixtures.TEST_USERNAME); // Should remain unchanged
    assertThat(result.getUsername()).isEqualTo(UserTestFixtures.TEST_USERNAME);
  }
  
  @Test
  @DisplayName("Should update all mutable fields")
  void shouldUpdateAllMutableFields() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    User updatedUser = UserTestFixtures.createUser();
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(updateDto.getEmail()))
        .thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(updatedUser);
    when(userMapper.toUserProfileDto(any(User.class)))
        .thenReturn(expectedDto);
    
    // When
    userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo(updateDto.getEmail());
    assertThat(savedUser.getFirstName()).isEqualTo(updateDto.getFirstName());
    assertThat(savedUser.getLastName()).isEqualTo(updateDto.getLastName());
    assertThat(savedUser.getProfilePictureUrl()).isEqualTo(updateDto.getProfilePictureUrl());
    assertThat(savedUser.getHouseholdSize()).isEqualTo(updateDto.getHouseholdSize());
    assertThat(savedUser.getMealsPerDay()).isEqualTo(updateDto.getMealsPerDay());
  }
  
  @Test
  @DisplayName("Should save updated entity to repository")
  void shouldSaveUpdatedEntityToRepository() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    User updatedUser = UserTestFixtures.createUser();
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(updateDto.getEmail()))
        .thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(updatedUser);
    when(userMapper.toUserProfileDto(updatedUser))
        .thenReturn(expectedDto);
    
    // When
    userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    verify(userRepository, times(1)).save(any(User.class));
  }
  
  @Test
  @DisplayName("Should log audit event via AuditService")
  void shouldLogAuditEventViaAuditService() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    UserProfileDto updateDto = UserTestFixtures.createUpdatedUserProfileDto();
    User updatedUser = UserTestFixtures.createUser();
    UserProfileDto expectedDto = UserTestFixtures.createUpdatedUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(updateDto.getEmail()))
        .thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(updatedUser);
    when(userMapper.toUserProfileDto(updatedUser))
        .thenReturn(expectedDto);
    
    // When
    userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    verify(auditService, times(1)).logEvent(
        eq(EventType.PROFILE_UPDATE),
        eq(UserTestFixtures.TEST_USER_ID),
        any(String.class)
    );
  }
  
  @Test
  @DisplayName("Should handle email change validation correctly (same email should pass)")
  void shouldHandleEmailChangeValidationCorrectly() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    existingUser.setEmail("test@example.com");
    
    UserProfileDto updateDto = UserTestFixtures.createUserProfileDto();
    updateDto.setEmail("test@example.com"); // Same email
    
    User updatedUser = UserTestFixtures.createUser();
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(existingUser.getEmail()))
        .thenReturn(true); // Email exists (for the same user)
    when(userRepository.save(any(User.class)))
        .thenReturn(updatedUser);
    when(userMapper.toUserProfileDto(updatedUser))
        .thenReturn(expectedDto);
    
    // When
    UserProfileDto result = userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    verify(userRepository).save(any(User.class));
    verify(auditService).logEvent(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should throw ValidationException when new email conflicts with another user")
  void shouldThrowValidationExceptionWhenNewEmailConflictsWithAnotherUser() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    existingUser.setEmail("old@example.com");
    
    UserProfileDto updateDto = UserTestFixtures.createUserProfileDto();
    updateDto.setEmail("new@example.com"); // Different email
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail("new@example.com"))
        .thenReturn(true); // Email already in use by another user
    
    // When & Then
    assertThatThrownBy(() -> userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Email is already in use");
    
    verify(userRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should update profile with partial data")
  void shouldUpdateProfileWithPartialData() {
    // Given
    User existingUser = UserTestFixtures.createUser();
    UserProfileDto updateDto = new UserProfileDto();
    updateDto.setEmail("partial@example.com");
    updateDto.setFirstName("Partial");
    // Other fields remain null
    
    User updatedUser = UserTestFixtures.createUser();
    updatedUser.setEmail("partial@example.com");
    updatedUser.setFirstName("Partial");
    
    UserProfileDto expectedDto = UserTestFixtures.createUserProfileDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail(updateDto.getEmail()))
        .thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(updatedUser);
    when(userMapper.toUserProfileDto(updatedUser))
        .thenReturn(expectedDto);
    
    // When
    UserProfileDto result = userService.updateUserProfile(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    verify(userRepository).save(any(User.class));
  }
}

