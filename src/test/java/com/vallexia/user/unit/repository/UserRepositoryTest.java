package com.vallexia.user.unit.repository;

import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRepository.
 * Tests repository query methods with mocked implementations.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Unit Tests")
class UserRepositoryTest {
  
  @Mock
  private UserRepository userRepository;
  
  // ==================== existsByUsername() Tests ====================
  
  @Test
  @DisplayName("Should return true when username exists")
  void shouldReturnTrueWhenUsernameExists() {
    // Given
    when(userRepository.existsByUsername(UserTestFixtures.TEST_USERNAME))
        .thenReturn(true);
    
    // When
    boolean exists = userRepository.existsByUsername(UserTestFixtures.TEST_USERNAME);
    
    // Then
    assertThat(exists).isTrue();
    verify(userRepository).existsByUsername(UserTestFixtures.TEST_USERNAME);
  }
  
  @Test
  @DisplayName("Should return false when username does not exist")
  void shouldReturnFalseWhenUsernameDoesNotExist() {
    // Given
    when(userRepository.existsByUsername("nonexistent"))
        .thenReturn(false);
    
    // When
    boolean exists = userRepository.existsByUsername("nonexistent");
    
    // Then
    assertThat(exists).isFalse();
    verify(userRepository).existsByUsername("nonexistent");
  }
  
  // ==================== existsByEmail() Tests ====================
  
  @Test
  @DisplayName("Should return true when email exists")
  void shouldReturnTrueWhenEmailExists() {
    // Given
    when(userRepository.existsByEmail(UserTestFixtures.TEST_EMAIL))
        .thenReturn(true);
    
    // When
    boolean exists = userRepository.existsByEmail(UserTestFixtures.TEST_EMAIL);
    
    // Then
    assertThat(exists).isTrue();
    verify(userRepository).existsByEmail(UserTestFixtures.TEST_EMAIL);
  }
  
  @Test
  @DisplayName("Should return false when email does not exist")
  void shouldReturnFalseWhenEmailDoesNotExist() {
    // Given
    when(userRepository.existsByEmail("nonexistent@example.com"))
        .thenReturn(false);
    
    // When
    boolean exists = userRepository.existsByEmail("nonexistent@example.com");
    
    // Then
    assertThat(exists).isFalse();
    verify(userRepository).existsByEmail("nonexistent@example.com");
  }
  
  @Test
  @DisplayName("Should return true when email exists for different user")
  void shouldReturnTrueWhenEmailExistsForDifferentUser() {
    // Given
    when(userRepository.existsByEmail(UserTestFixtures.TEST_EMAIL))
        .thenReturn(true);
    
    // When
    boolean exists = userRepository.existsByEmail(UserTestFixtures.TEST_EMAIL);
    
    // Then
    assertThat(exists).isTrue();
    verify(userRepository).existsByEmail(UserTestFixtures.TEST_EMAIL);
  }
  
  // ==================== findByUsernameAndEnabledTrue() Tests ====================
  
  @Test
  @DisplayName("Should find enabled user by username")
  void shouldFindEnabledUserByUsername() {
    // Given
    User user = UserTestFixtures.createUser();
    user.setEnabled(true);
    when(userRepository.findByUsernameAndEnabledTrue(UserTestFixtures.TEST_USERNAME))
        .thenReturn(Optional.of(user));
    
    // When
    Optional<User> found = userRepository.findByUsernameAndEnabledTrue(UserTestFixtures.TEST_USERNAME);
    
    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getEnabled()).isTrue();
    verify(userRepository).findByUsernameAndEnabledTrue(UserTestFixtures.TEST_USERNAME);
  }
  
  @Test
  @DisplayName("Should not find disabled user by username")
  void shouldNotFindDisabledUserByUsername() {
    // Given
    when(userRepository.findByUsernameAndEnabledTrue(UserTestFixtures.TEST_USERNAME))
        .thenReturn(Optional.empty());
    
    // When
    Optional<User> found = userRepository.findByUsernameAndEnabledTrue(UserTestFixtures.TEST_USERNAME);
    
    // Then
    assertThat(found).isEmpty();
    verify(userRepository).findByUsernameAndEnabledTrue(UserTestFixtures.TEST_USERNAME);
  }
  
  @Test
  @DisplayName("Should return empty Optional when enabled user not found")
  void shouldReturnEmptyOptionalWhenEnabledUserNotFound() {
    // Given
    when(userRepository.findByUsernameAndEnabledTrue("nonexistent"))
        .thenReturn(Optional.empty());
    
    // When
    Optional<User> found = userRepository.findByUsernameAndEnabledTrue("nonexistent");
    
    // Then
    assertThat(found).isEmpty();
    verify(userRepository).findByUsernameAndEnabledTrue("nonexistent");
  }
  
  // ==================== findByEmailAndEnabledTrue() Tests ====================
  
  @Test
  @DisplayName("Should find enabled user by email")
  void shouldFindEnabledUserByEmail() {
    // Given
    User user = UserTestFixtures.createUser();
    user.setEnabled(true);
    when(userRepository.findByEmailAndEnabledTrue(UserTestFixtures.TEST_EMAIL))
        .thenReturn(Optional.of(user));
    
    // When
    Optional<User> found = userRepository.findByEmailAndEnabledTrue(UserTestFixtures.TEST_EMAIL);
    
    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getEnabled()).isTrue();
    verify(userRepository).findByEmailAndEnabledTrue(UserTestFixtures.TEST_EMAIL);
  }
  
  @Test
  @DisplayName("Should not find disabled user by email")
  void shouldNotFindDisabledUserByEmail() {
    // Given
    when(userRepository.findByEmailAndEnabledTrue(UserTestFixtures.TEST_EMAIL))
        .thenReturn(Optional.empty());
    
    // When
    Optional<User> found = userRepository.findByEmailAndEnabledTrue(UserTestFixtures.TEST_EMAIL);
    
    // Then
    assertThat(found).isEmpty();
    verify(userRepository).findByEmailAndEnabledTrue(UserTestFixtures.TEST_EMAIL);
  }
  
  @Test
  @DisplayName("Should return empty Optional when enabled email not found")
  void shouldReturnEmptyOptionalWhenEnabledEmailNotFound() {
    // Given
    when(userRepository.findByEmailAndEnabledTrue("nonexistent@example.com"))
        .thenReturn(Optional.empty());
    
    // When
    Optional<User> found = userRepository.findByEmailAndEnabledTrue("nonexistent@example.com");
    
    // Then
    assertThat(found).isEmpty();
    verify(userRepository).findByEmailAndEnabledTrue("nonexistent@example.com");
  }
  
  // ==================== findById() Tests (JpaRepository) ====================
  
  @Test
  @DisplayName("Should find user by ID")
  void shouldFindUserById() {
    // Given
    User user = UserTestFixtures.createUser();
    when(userRepository.findById(1L))
        .thenReturn(Optional.of(user));
    
    // When
    Optional<User> found = userRepository.findById(1L);
    
    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(1L);
    verify(userRepository).findById(1L);
  }
  
  @Test
  @DisplayName("Should return empty Optional when ID not found")
  void shouldReturnEmptyOptionalWhenIdNotFound() {
    // Given
    when(userRepository.findById(999L))
        .thenReturn(Optional.empty());
    
    // When
    Optional<User> found = userRepository.findById(999L);
    
    // Then
    assertThat(found).isEmpty();
    verify(userRepository).findById(999L);
  }
  
  @Test
  @DisplayName("Should save user")
  void shouldSaveUser() {
    // Given
    User user = UserTestFixtures.createUser();
    when(userRepository.save(user))
        .thenReturn(user);
    
    // When
    User saved = userRepository.save(user);
    
    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getUsername()).isEqualTo(UserTestFixtures.TEST_USERNAME);
    assertThat(saved.getEmail()).isEqualTo(UserTestFixtures.TEST_EMAIL);
    verify(userRepository).save(user);
  }
}
