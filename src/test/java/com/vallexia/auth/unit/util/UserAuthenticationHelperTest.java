package com.vallexia.auth.unit.util;

import com.vallexia.auth.exception.AccountDisabledException;
import com.vallexia.auth.exception.AccountLockedException;
import com.vallexia.auth.util.UserAuthenticationHelper;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserAuthenticationHelper.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserAuthenticationHelper Unit Tests")
class UserAuthenticationHelperTest {
  
  @Mock
  private UserRepository userRepository;
  
  @InjectMocks
  private UserAuthenticationHelper userAuthenticationHelper;
  
  private User testUser;
  
  @BeforeEach
  void setUp() {
    testUser = UserTestFixtures.createUser();
    testUser.setEnabled(true);
  }
  
  @Test
  @DisplayName("Should find user by username")
  void shouldFindUserByUsername() {
    // Given
    String username = "testuser";
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
    
    // When
    Optional<User> result = userAuthenticationHelper.findUserByUsernameOrEmail(username);
    
    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testUser);
    verify(userRepository).findByUsername(username);
    verify(userRepository, never()).findByEmail(anyString());
  }
  
  @Test
  @DisplayName("Should fall back to email when username not found")
  void shouldFallBackToEmailWhenUsernameNotFound() {
    // Given
    String email = "test@example.com";
    when(userRepository.findByUsername(email)).thenReturn(Optional.empty());
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
    
    // When
    Optional<User> result = userAuthenticationHelper.findUserByUsernameOrEmail(email);
    
    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testUser);
    verify(userRepository).findByUsername(email);
    verify(userRepository).findByEmail(email);
  }
  
  @Test
  @DisplayName("Should return empty when user not found")
  void shouldReturnEmptyWhenUserNotFound() {
    // Given
    String usernameOrEmail = "nonexistent";
    when(userRepository.findByUsername(usernameOrEmail)).thenReturn(Optional.empty());
    when(userRepository.findByEmail(usernameOrEmail)).thenReturn(Optional.empty());
    
    // When
    Optional<User> result = userAuthenticationHelper.findUserByUsernameOrEmail(usernameOrEmail);
    
    // Then
    assertThat(result).isEmpty();
  }
  
  @Test
  @DisplayName("Should validate account status successfully for enabled and unlocked account")
  void shouldValidateAccountStatusSuccessfully() {
    // Given
    testUser.setEnabled(true);
    testUser.setAccountLockedUntil(null);
    
    // When/Then - should not throw
    userAuthenticationHelper.validateAccountStatus(testUser);
  }
  
  @Test
  @DisplayName("Should throw AccountDisabledException for disabled account")
  void shouldThrowAccountDisabledExceptionForDisabledAccount() {
    // Given
    testUser.setEnabled(false);
    
    // When/Then
    assertThatThrownBy(() -> userAuthenticationHelper.validateAccountStatus(testUser))
        .isInstanceOf(AccountDisabledException.class)
        .hasMessage("Account is disabled");
  }
  
  @Test
  @DisplayName("Should throw AccountLockedException for locked account")
  void shouldThrowAccountLockedExceptionForLockedAccount() {
    // Given
    testUser.setEnabled(true);
    testUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
    
    // When/Then
    assertThatThrownBy(() -> userAuthenticationHelper.validateAccountStatus(testUser))
        .isInstanceOf(AccountLockedException.class)
        .hasMessageContaining("Account is temporarily locked");
  }
}
