package com.vallexia.nutrition.unit.controller;

import com.vallexia.nutrition.controller.NutritionalGoalsController;
import com.vallexia.nutrition.dto.MacroBreakdown;
import com.vallexia.nutrition.dto.NutritionalGoalsDto;
import com.vallexia.nutrition.enums.GoalType;
import com.vallexia.nutrition.service.MacroCalculator;
import com.vallexia.nutrition.service.NutritionalGoalsService;
import com.vallexia.security.AuthenticationHelper;
import com.vallexia.user.fixtures.UserTestFixtures;
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

import java.math.BigDecimal;
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
 * @since 2025-11-27
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NutritionalGoalsController Unit Tests")
class NutritionalGoalsControllerTest {
  
  @Mock
  private NutritionalGoalsService nutritionalGoalsService;
  
  @Mock
  private MacroCalculator macroCalculator;
  
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
  
  // ==================== calculateMacrosFromGoalType() Tests ====================
  
  @Test
  @DisplayName("Should calculate macros from goal type successfully")
  void shouldCalculateMacrosFromGoalTypeSuccessfully() {
    // Given
    BigDecimal dailyCalories = BigDecimal.valueOf(2000);
    String goalType = "WEIGHT_LOSS";
    MacroBreakdown expectedBreakdown = new MacroBreakdown(
        BigDecimal.valueOf(200), // 40% of 2000 / 4 = 200g protein
        BigDecimal.valueOf(150), // 30% of 2000 / 4 = 150g carbs
        BigDecimal.valueOf(66.67) // 30% of 2000 / 9 = 66.67g fats
    );
    
    when(macroCalculator.calculateMacrosFromGoalType(dailyCalories, GoalType.WEIGHT_LOSS))
        .thenReturn(expectedBreakdown);
    
    // When
    ResponseEntity<MacroBreakdown> response = nutritionalGoalsController.calculateMacrosFromGoalType(dailyCalories, goalType);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    MacroBreakdown body = response.getBody();
    assertThat(body).isNotNull();
    if (body != null) {
      assertThat(body.getProtein()).isNotNull();
      assertThat(body.getCarbs()).isNotNull();
      assertThat(body.getFats()).isNotNull();
    }
    
    verify(macroCalculator).calculateMacrosFromGoalType(dailyCalories, GoalType.WEIGHT_LOSS);
  }
  
  @Test
  @DisplayName("Should handle case-insensitive goal type")
  void shouldHandleCaseInsensitiveGoalType() {
    // Given
    BigDecimal dailyCalories = BigDecimal.valueOf(2000);
    String goalType = "muscle_gain"; // lowercase
    MacroBreakdown expectedBreakdown = new MacroBreakdown(
        BigDecimal.valueOf(175), // 35% of 2000 / 4 = 175g protein
        BigDecimal.valueOf(200), // 40% of 2000 / 4 = 200g carbs
        BigDecimal.valueOf(55.56) // 25% of 2000 / 9 = 55.56g fats
    );
    
    when(macroCalculator.calculateMacrosFromGoalType(dailyCalories, GoalType.MUSCLE_GAIN))
        .thenReturn(expectedBreakdown);
    
    // When
    ResponseEntity<MacroBreakdown> response = nutritionalGoalsController.calculateMacrosFromGoalType(dailyCalories, goalType);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(macroCalculator).calculateMacrosFromGoalType(dailyCalories, GoalType.MUSCLE_GAIN);
  }
  
  @Test
  @DisplayName("Should return 400 Bad Request for invalid goal type")
  void shouldReturnBadRequestForInvalidGoalType() {
    // Given
    BigDecimal dailyCalories = BigDecimal.valueOf(2000);
    String invalidGoalType = "INVALID_GOAL";
    
    // When
    ResponseEntity<MacroBreakdown> response = nutritionalGoalsController.calculateMacrosFromGoalType(dailyCalories, invalidGoalType);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    verify(macroCalculator, never()).calculateMacrosFromGoalType(any(), any());
  }
}
