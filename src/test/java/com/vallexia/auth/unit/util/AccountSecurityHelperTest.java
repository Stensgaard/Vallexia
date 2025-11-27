package com.vallexia.auth.unit.util;

import com.vallexia.auth.util.AccountSecurityHelper;
import com.vallexia.config.security.AccountSecurityProperties;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountSecurityHelper.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AccountSecurityHelper Unit Tests")
class AccountSecurityHelperTest {
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private AccountSecurityProperties accountSecurityProperties;
  
  @InjectMocks
  private AccountSecurityHelper accountSecurityHelper;
  
  private User testUser;
  
  @BeforeEach
  void setUp() {
    testUser = UserTestFixtures.createUser();
    testUser.setFailedLoginAttempts(0);
    testUser.setAccountLockedUntil(null);
    
    when(accountSecurityProperties.getMaxFailedAttempts()).thenReturn(5);
    when(accountSecurityProperties.getDurationMinutes()).thenReturn(30);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
  }
  
  @Test
  @DisplayName("Should increment failed login attempts")
  void shouldIncrementFailedLoginAttempts() {
    // Given
    testUser.setFailedLoginAttempts(2);
    
    // When
    accountSecurityHelper.handleFailedLoginAttempt(testUser);
    
    // Then
    assertThat(testUser.getFailedLoginAttempts()).isEqualTo(3);
    verify(userRepository).save(testUser);
  }
  
  @Test
  @DisplayName("Should lock account when max attempts reached")
  void shouldLockAccountWhenMaxAttemptsReached() {
    // Given
    testUser.setFailedLoginAttempts(4); // One less than max
    
    // When
    accountSecurityHelper.handleFailedLoginAttempt(testUser);
    
    // Then
    assertThat(testUser.getFailedLoginAttempts()).isEqualTo(5);
    assertThat(testUser.getAccountLockedUntil()).isNotNull();
    assertThat(testUser.getAccountLockedUntil()).isAfter(LocalDateTime.now());
    verify(userRepository).save(testUser);
  }
  
  @Test
  @DisplayName("Should not lock account when below max attempts")
  void shouldNotLockAccountWhenBelowMaxAttempts() {
    // Given
    testUser.setFailedLoginAttempts(2);
    
    // When
    accountSecurityHelper.handleFailedLoginAttempt(testUser);
    
    // Then
    assertThat(testUser.getFailedLoginAttempts()).isEqualTo(3);
    assertThat(testUser.getAccountLockedUntil()).isNull();
    verify(userRepository).save(testUser);
  }
  
  @Test
  @DisplayName("Should reset failed login attempts when attempts exist")
  void shouldResetFailedLoginAttemptsWhenAttemptsExist() {
    // Given
    testUser.setFailedLoginAttempts(3);
    
    // When
    accountSecurityHelper.resetFailedLoginAttempts(testUser);
    
    // Then
    assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
    verify(userRepository).save(testUser);
  }
  
  @Test
  @DisplayName("Should not save when no failed attempts to reset")
  void shouldNotSaveWhenNoFailedAttemptsToReset() {
    // Given
    testUser.setFailedLoginAttempts(0);
    
    // When
    accountSecurityHelper.resetFailedLoginAttempts(testUser);
    
    // Then
    assertThat(testUser.getFailedLoginAttempts()).isEqualTo(0);
    verify(userRepository, never()).save(any(User.class));
  }
}
