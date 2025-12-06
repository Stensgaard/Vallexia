package com.vallexia.user.unit.controller;

import com.vallexia.security.AuthenticationHelper;
import com.vallexia.user.controller.UserSettingsController;
import com.vallexia.user.dto.UserSettingsDto;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.service.UserSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserSettingsController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserSettingsController Unit Tests")
class UserSettingsControllerTest {
  
  @Mock
  private UserSettingsService userSettingsService;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @InjectMocks
  private UserSettingsController userSettingsController;
  
  private Authentication mockAuthentication;
  
  @BeforeEach
  void setUp() {
    mockAuthentication = createMockAuthentication();
  }
  
  private Authentication createMockAuthentication() {
    return new Authentication() {
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
      }
      
      @Override
      public Object getCredentials() {
        return null;
      }
      
      @Override
      public Object getDetails() {
        return null;
      }
      
      @Override
      public Object getPrincipal() {
        return "testuser";
      }
      
      @Override
      public boolean isAuthenticated() {
        return true;
      }
      
      @Override
      public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        
      }
      
      @Override
      public String getName() {
        return "testuser";
      }
    };
  }
  
  // ==================== getCurrentUserSettings() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve settings for authenticated user successfully")
  void shouldRetrieveSettingsForAuthenticatedUserSuccessfully() {
    // Given
    Long userId = 1L;
    UserSettingsDto expectedDto = UserTestFixtures.createUserSettingsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userSettingsService.getUserSettings(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserSettingsDto> response = userSettingsController.getCurrentUserSettings(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserSettingsDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(userId);
    assertThat(body.getUserId()).isEqualTo(userId);
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(userSettingsService).getUserSettings(userId);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return 200 OK with correct settings data")
  void shouldReturnOkWithCorrectSettingsData() {
    // Given
    Long userId = 1L;
    UserSettingsDto expectedDto = UserTestFixtures.createUserSettingsDto();
    expectedDto.setId(userId);
    expectedDto.setUserId(userId);
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userSettingsService.getUserSettings(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserSettingsDto> response = userSettingsController.getCurrentUserSettings(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserSettingsDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getLanguage()).isEqualTo(expectedDto.getLanguage());
    assertThat(body.getCountry()).isEqualTo(expectedDto.getCountry());
  }
  
  @Test
  @DisplayName("Should handle authentication properly via AuthenticationHelper")
  void shouldHandleAuthenticationProperlyViaAuthenticationHelper() {
    // Given
    Long userId = 1L;
    UserSettingsDto expectedDto = UserTestFixtures.createUserSettingsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userSettingsService.getUserSettings(userId))
        .thenReturn(expectedDto);
    
    // When
    userSettingsController.getCurrentUserSettings(mockAuthentication);
    
    // Then
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(userSettingsService).getUserSettings(userId);
  }
  
  // ==================== updateCurrentUserSettings() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should update settings successfully with valid data")
  void shouldUpdateSettingsSuccessfullyWithValidData() {
    // Given
    Long userId = 1L;
    UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
    UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userSettingsService.updateUserSettings(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserSettingsDto> response = userSettingsController.updateCurrentUserSettings(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserSettingsDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getLanguage()).isEqualTo(updateDto.getLanguage());
    assertThat(body.getCountry()).isEqualTo(updateDto.getCountry());
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(userSettingsService).updateUserSettings(userId, updateDto);
  }
  
  @Test
  @DisplayName("Should return 200 OK with updated settings")
  void shouldReturnOkWithUpdatedSettings() {
    // Given
    Long userId = 1L;
    UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
    UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userSettingsService.updateUserSettings(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserSettingsDto> response = userSettingsController.updateCurrentUserSettings(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserSettingsDto body = response.getBody();
    assertThat(body).isNotNull();
  }
  
  @Test
  @DisplayName("Should handle authentication properly when updating settings")
  void shouldHandleAuthenticationProperlyWhenUpdatingSettings() {
    // Given
    Long userId = 1L;
    UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
    UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userSettingsService.updateUserSettings(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    userSettingsController.updateCurrentUserSettings(updateDto, mockAuthentication);
    
    // Then
    verify(authenticationHelper, times(1)).getCurrentUserId(mockAuthentication);
    verify(userSettingsService, times(1)).updateUserSettings(eq(userId), eq(updateDto));
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should update settings with all fields")
  void shouldUpdateSettingsWithAllFields() {
    // Given
    Long userId = 1L;
    UserSettingsDto updateDto = UserTestFixtures.createUpdatedUserSettingsDto();
    UserSettingsDto expectedDto = UserTestFixtures.createUpdatedUserSettingsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(userSettingsService.updateUserSettings(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<UserSettingsDto> response = userSettingsController.updateCurrentUserSettings(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    UserSettingsDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getLanguage()).isEqualTo(updateDto.getLanguage());
    assertThat(body.getCountry()).isEqualTo(updateDto.getCountry());
    assertThat(body.getDateFormat()).isEqualTo(updateDto.getDateFormat());
    assertThat(body.getTimezone()).isEqualTo(updateDto.getTimezone());
    assertThat(body.getMeasurementSystem()).isEqualTo(updateDto.getMeasurementSystem());
  }
}
