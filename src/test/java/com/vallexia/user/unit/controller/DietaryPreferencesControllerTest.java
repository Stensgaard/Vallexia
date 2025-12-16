package com.vallexia.user.unit.controller;

import com.vallexia.security.AuthenticationHelper;
import com.vallexia.user.controller.DietaryPreferencesController;
import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.service.DietaryPreferencesService;
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
 * Unit tests for DietaryPreferencesController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DietaryPreferencesController Unit Tests")
class DietaryPreferencesControllerTest {
  
  @Mock
  private DietaryPreferencesService dietaryPreferencesService;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @InjectMocks
  private DietaryPreferencesController dietaryPreferencesController;
  
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
  
  // ==================== getCurrentUserDietaryPreferences() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve preferences for authenticated user successfully")
  void shouldRetrievePreferencesForAuthenticatedUserSuccessfully() {
    // Given
    Long userId = 1L;
    DietaryPreferencesDto expectedDto = UserTestFixtures.createDietaryPreferencesDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(dietaryPreferencesService.getDietaryPreferences(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<DietaryPreferencesDto> response = dietaryPreferencesController.getCurrentUserDietaryPreferences(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DietaryPreferencesDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(expectedDto.getId());
    assertThat(body.getUserId()).isEqualTo(userId);
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(dietaryPreferencesService).getDietaryPreferences(userId);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return 200 OK with correct preferences data")
  void shouldReturnOkWithCorrectPreferencesData() {
    // Given
    Long userId = 1L;
    DietaryPreferencesDto expectedDto = UserTestFixtures.createDietaryPreferencesDto();
    expectedDto.setUserId(userId);
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(dietaryPreferencesService.getDietaryPreferences(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<DietaryPreferencesDto> response = dietaryPreferencesController.getCurrentUserDietaryPreferences(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DietaryPreferencesDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getRestrictions()).isEqualTo(expectedDto.getRestrictions());
    assertThat(body.getAllergies()).isEqualTo(expectedDto.getAllergies());
  }
  
  @Test
  @DisplayName("Should handle authentication properly via AuthenticationHelper")
  void shouldHandleAuthenticationProperlyViaAuthenticationHelper() {
    // Given
    Long userId = 1L;
    DietaryPreferencesDto expectedDto = UserTestFixtures.createDietaryPreferencesDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(dietaryPreferencesService.getDietaryPreferences(userId))
        .thenReturn(expectedDto);
    
    // When
    dietaryPreferencesController.getCurrentUserDietaryPreferences(mockAuthentication);
    
    // Then
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(dietaryPreferencesService).getDietaryPreferences(userId);
  }
  
  // ==================== updateCurrentUserDietaryPreferences() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should update preferences successfully with valid data")
  void shouldUpdatePreferencesSuccessfullyWithValidData() {
    // Given
    Long userId = 1L;
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(dietaryPreferencesService.updateDietaryPreferences(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<DietaryPreferencesDto> response = dietaryPreferencesController.updateCurrentUserDietaryPreferences(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DietaryPreferencesDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getRestrictions()).isEqualTo(updateDto.getRestrictions());
    assertThat(body.getAllergies()).isEqualTo(updateDto.getAllergies());
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(dietaryPreferencesService).updateDietaryPreferences(userId, updateDto);
  }
  
  @Test
  @DisplayName("Should return 200 OK with updated preferences")
  void shouldReturnOkWithUpdatedPreferences() {
    // Given
    Long userId = 1L;
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(dietaryPreferencesService.updateDietaryPreferences(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<DietaryPreferencesDto> response = dietaryPreferencesController.updateCurrentUserDietaryPreferences(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    DietaryPreferencesDto body = response.getBody();
    assertThat(body).isNotNull();
  }
  
  @Test
  @DisplayName("Should handle authentication properly when updating preferences")
  void shouldHandleAuthenticationProperlyWhenUpdatingPreferences() {
    // Given
    Long userId = 1L;
    DietaryPreferencesDto updateDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    DietaryPreferencesDto expectedDto = UserTestFixtures.createUpdatedDietaryPreferencesDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(dietaryPreferencesService.updateDietaryPreferences(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    dietaryPreferencesController.updateCurrentUserDietaryPreferences(updateDto, mockAuthentication);
    
    // Then
    verify(authenticationHelper, times(1)).getCurrentUserId(mockAuthentication);
    verify(dietaryPreferencesService, times(1)).updateDietaryPreferences(eq(userId), eq(updateDto));
  }
}
