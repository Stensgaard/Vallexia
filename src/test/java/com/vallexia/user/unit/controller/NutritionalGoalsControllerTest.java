package com.vallexia.user.unit.controller;

import com.vallexia.security.AuthenticationHelper;
import com.vallexia.user.controller.NutritionalGoalsController;
import com.vallexia.user.dto.NutritionalGoalsDto;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.service.NutritionalGoalsService;
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
 * Unit tests for NutritionalGoalsController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NutritionalGoalsController Unit Tests")
class NutritionalGoalsControllerTest {
  
  @Mock
  private NutritionalGoalsService nutritionalGoalsService;
  
  @Mock
  private AuthenticationHelper authenticationHelper;
  
  @InjectMocks
  private NutritionalGoalsController nutritionalGoalsController;
  
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
  
  // ==================== getCurrentUserNutritionalGoals() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should retrieve goals for authenticated user successfully")
  void shouldRetrieveGoalsForAuthenticatedUserSuccessfully() {
    // Given
    Long userId = 1L;
    NutritionalGoalsDto expectedDto = UserTestFixtures.createNutritionalGoalsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(nutritionalGoalsService.getNutritionalGoals(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<NutritionalGoalsDto> response = nutritionalGoalsController.getCurrentUserNutritionalGoals(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    NutritionalGoalsDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getId()).isEqualTo(expectedDto.getId());
    assertThat(body.getUserId()).isEqualTo(userId);
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(nutritionalGoalsService).getNutritionalGoals(userId);
  }
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return 200 OK with correct goals data")
  void shouldReturnOkWithCorrectGoalsData() {
    // Given
    Long userId = 1L;
    NutritionalGoalsDto expectedDto = UserTestFixtures.createNutritionalGoalsDto();
    expectedDto.setUserId(userId);
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(nutritionalGoalsService.getNutritionalGoals(userId))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<NutritionalGoalsDto> response = nutritionalGoalsController.getCurrentUserNutritionalGoals(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    NutritionalGoalsDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getDailyCalories()).isEqualTo(expectedDto.getDailyCalories());
    assertThat(body.getDailyProtein()).isEqualTo(expectedDto.getDailyProtein());
  }
  
  @Test
  @DisplayName("Should handle authentication properly via AuthenticationHelper")
  void shouldHandleAuthenticationProperlyViaAuthenticationHelper() {
    // Given
    Long userId = 1L;
    NutritionalGoalsDto expectedDto = UserTestFixtures.createNutritionalGoalsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(nutritionalGoalsService.getNutritionalGoals(userId))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsController.getCurrentUserNutritionalGoals(mockAuthentication);
    
    // Then
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(nutritionalGoalsService).getNutritionalGoals(userId);
  }
  
  // ==================== updateCurrentUserNutritionalGoals() Tests ====================
  
  @SuppressWarnings("null")
  @Test
  @DisplayName("Should update goals successfully with valid data")
  void shouldUpdateGoalsSuccessfullyWithValidData() {
    // Given
    Long userId = 1L;
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(nutritionalGoalsService.updateNutritionalGoals(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<NutritionalGoalsDto> response = nutritionalGoalsController.updateCurrentUserNutritionalGoals(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    NutritionalGoalsDto body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getDailyCalories()).isEqualTo(updateDto.getDailyCalories());
    assertThat(body.getGoalType()).isEqualTo(updateDto.getGoalType());
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(nutritionalGoalsService).updateNutritionalGoals(userId, updateDto);
  }
  
  @Test
  @DisplayName("Should return 200 OK with updated goals")
  void shouldReturnOkWithUpdatedGoals() {
    // Given
    Long userId = 1L;
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(nutritionalGoalsService.updateNutritionalGoals(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    ResponseEntity<NutritionalGoalsDto> response = nutritionalGoalsController.updateCurrentUserNutritionalGoals(updateDto, mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    NutritionalGoalsDto body = response.getBody();
    assertThat(body).isNotNull();
  }
  
  @Test
  @DisplayName("Should handle authentication properly when updating goals")
  void shouldHandleAuthenticationProperlyWhenUpdatingGoals() {
    // Given
    Long userId = 1L;
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    when(nutritionalGoalsService.updateNutritionalGoals(userId, updateDto))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsController.updateCurrentUserNutritionalGoals(updateDto, mockAuthentication);
    
    // Then
    verify(authenticationHelper, times(1)).getCurrentUserId(mockAuthentication);
    verify(nutritionalGoalsService, times(1)).updateNutritionalGoals(eq(userId), eq(updateDto));
  }
  
  // ==================== deleteCurrentUserNutritionalGoals() Tests ====================
  
  @Test
  @DisplayName("Should delete goals successfully (204)")
  void shouldDeleteGoalsSuccessfully() {
    // Given
    Long userId = 1L;
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    doNothing().when(nutritionalGoalsService).deleteNutritionalGoals(userId);
    
    // When
    ResponseEntity<Void> response = nutritionalGoalsController.deleteCurrentUserNutritionalGoals(mockAuthentication);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
    
    verify(authenticationHelper).getCurrentUserId(mockAuthentication);
    verify(nutritionalGoalsService).deleteNutritionalGoals(userId);
  }
  
  @Test
  @DisplayName("Should handle authentication properly when deleting goals")
  void shouldHandleAuthenticationProperlyWhenDeletingGoals() {
    // Given
    Long userId = 1L;
    
    when(authenticationHelper.getCurrentUserId(mockAuthentication))
        .thenReturn(userId);
    doNothing().when(nutritionalGoalsService).deleteNutritionalGoals(userId);
    
    // When
    nutritionalGoalsController.deleteCurrentUserNutritionalGoals(mockAuthentication);
    
    // Then
    verify(authenticationHelper, times(1)).getCurrentUserId(mockAuthentication);
    verify(nutritionalGoalsService, times(1)).deleteNutritionalGoals(userId);
  }
}
