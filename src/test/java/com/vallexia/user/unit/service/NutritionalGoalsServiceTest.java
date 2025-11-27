package com.vallexia.user.unit.service;

import com.vallexia.audit.entity.enums.EventType;
import com.vallexia.audit.service.AuditService;
import com.vallexia.exception.ValidationException;
import com.vallexia.nutrition.service.MacroCalculator;
import com.vallexia.user.dto.NutritionalGoalsDto;
import com.vallexia.user.entity.NutritionalGoals;
import com.vallexia.user.entity.User;
import com.vallexia.user.entity.enums.GoalType;
import com.vallexia.user.exception.UserNotFoundException;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.mapper.NutritionalGoalsMapper;
import com.vallexia.user.repository.NutritionalGoalsRepository;
import com.vallexia.user.repository.UserRepository;
import com.vallexia.user.service.NutritionalGoalsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NutritionalGoalsService.
 * Tests business logic with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NutritionalGoalsService Unit Tests")
class NutritionalGoalsServiceTest {
  
  @Mock
  private NutritionalGoalsRepository nutritionalGoalsRepository;
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private NutritionalGoalsMapper nutritionalGoalsMapper;
  
  @Mock
  private MacroCalculator macroCalculator;
  
  @Mock
  private AuditService auditService;
  
  @InjectMocks
  private NutritionalGoalsService nutritionalGoalsService;
  
  /**
   * Helper method to set up default macro calorie calculation mocks.
   * Used to avoid repeating mock setup in every test.
   */
  private void setupMacroCalorieMocks(NutritionalGoals goals) {
    when(macroCalculator.calculateProteinCalories(any(BigDecimal.class)))
        .thenAnswer(invocation -> {
          BigDecimal protein = invocation.getArgument(0);
          return protein != null ? protein.multiply(BigDecimal.valueOf(4)) : BigDecimal.ZERO;
        });
    when(macroCalculator.calculateCarbCalories(any(BigDecimal.class)))
        .thenAnswer(invocation -> {
          BigDecimal carbs = invocation.getArgument(0);
          return carbs != null ? carbs.multiply(BigDecimal.valueOf(4)) : BigDecimal.ZERO;
        });
    when(macroCalculator.calculateFatCalories(any(BigDecimal.class)))
        .thenAnswer(invocation -> {
          BigDecimal fats = invocation.getArgument(0);
          return fats != null ? fats.multiply(BigDecimal.valueOf(9)) : BigDecimal.ZERO;
        });
  }
  
  // ==================== getNutritionalGoals() Tests ====================
  
  @Test
  @DisplayName("Should retrieve nutritional goals successfully")
  void shouldRetrieveNutritionalGoalsSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    NutritionalGoalsDto expectedDto = UserTestFixtures.createNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(goals));
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(goals))
        .thenReturn(expectedDto);
    // Set up macro calorie calculation mocks
    setupMacroCalorieMocks(goals);
    
    // When
    NutritionalGoalsDto result = nutritionalGoalsService.getNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(expectedDto.getId());
    assertThat(result.getUserId()).isEqualTo(expectedDto.getUserId());
    // Verify macro calories are calculated (150g * 4 = 600, 250g * 4 = 1000, 67g * 9 = 603)
    assertThat(result.getProteinCalories()).isNotNull();
    assertThat(result.getCarbCalories()).isNotNull();
    assertThat(result.getFatCalories()).isNotNull();
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository).findByUser(user);
    verify(nutritionalGoalsMapper).toNutritionalGoalsDto(goals);
    verify(macroCalculator).calculateProteinCalories(goals.getDailyProtein());
    verify(macroCalculator).calculateCarbCalories(goals.getDailyCarbs());
    verify(macroCalculator).calculateFatCalories(goals.getDailyFats());
  }
  
  @Test
  @DisplayName("Should return default goals when none exist")
  void shouldReturnDefaultGoalsWhenNoneExist() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoalsDto expectedDto = new NutritionalGoalsDto();
    expectedDto.setUserId(UserTestFixtures.TEST_USER_ID);
    expectedDto.setDailyCalories(BigDecimal.valueOf(2000));
    expectedDto.setDailyProtein(BigDecimal.valueOf(150));
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    ArgumentCaptor<NutritionalGoals> goalsCaptor = ArgumentCaptor.forClass(NutritionalGoals.class);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(goalsCaptor.capture()))
        .thenReturn(expectedDto);
    
    // Set up macro calorie calculation mocks for default goals
    setupMacroCalorieMocks(null); // Will use default values
    
    // When
    NutritionalGoalsDto result = nutritionalGoalsService.getNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isNotNull();
    NutritionalGoals captured = goalsCaptor.getValue();
    assertThat(captured.getUser()).isEqualTo(user);
    assertThat(captured.getDailyCalories()).isEqualTo(BigDecimal.valueOf(2000));
    assertThat(captured.getDailyProtein()).isEqualTo(BigDecimal.valueOf(150));
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository).findByUser(user);
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    // Given
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> nutritionalGoalsService.getNutritionalGoals(UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository, never()).findByUser(any());
    verify(nutritionalGoalsMapper, never()).toNutritionalGoalsDto(any());
  }
  
  @Test
  @DisplayName("Should map entity to DTO correctly")
  void shouldMapEntityToDtoCorrectly() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    NutritionalGoalsDto expectedDto = UserTestFixtures.createNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(goals));
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(goals))
        .thenReturn(expectedDto);
    
    // When
    NutritionalGoalsDto result = nutritionalGoalsService.getNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    // Then
    assertThat(result).isEqualTo(expectedDto);
    verify(nutritionalGoalsMapper).toNutritionalGoalsDto(goals);
  }
  
  @Test
  @DisplayName("Should set default values when creating new goals")
  void shouldSetDefaultValuesWhenCreatingNewGoals() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    ArgumentCaptor<NutritionalGoals> goalsCaptor = ArgumentCaptor.forClass(NutritionalGoals.class);
    NutritionalGoalsDto expectedDto = new NutritionalGoalsDto();
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(goalsCaptor.capture()))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsService.getNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    // Then
    NutritionalGoals captured = goalsCaptor.getValue();
    assertThat(captured.getDailyCalories()).isEqualTo(BigDecimal.valueOf(2000));
    assertThat(captured.getDailyProtein()).isEqualTo(BigDecimal.valueOf(150));
    assertThat(captured.getDailyCarbs()).isEqualTo(BigDecimal.valueOf(250));
    assertThat(captured.getDailyFats()).isEqualTo(BigDecimal.valueOf(67));
  }
  
  // ==================== updateNutritionalGoals() Tests ====================
  
  @Test
  @DisplayName("Should update goals successfully with valid data")
  void shouldUpdateGoalsSuccessfullyWithValidData() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20));
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    NutritionalGoalsDto result = nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getDailyCalories()).isEqualTo(updateDto.getDailyCalories());
    assertThat(result.getGoalType()).isEqualTo(updateDto.getGoalType());
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository).findByUser(user);
    verify(macroCalculator).calculateMacroPercentages(existingGoals);
    verify(nutritionalGoalsRepository).save(existingGoals);
    verify(auditService).logEvent(eq(EventType.PROFILE_UPDATE), eq(UserTestFixtures.TEST_USER_ID), any(String.class));
  }
  
  @Test
  @DisplayName("Should create goals if they don't exist")
  void shouldCreateGoalsIfTheyDontExist() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    ArgumentCaptor<NutritionalGoals> goalsCaptor = ArgumentCaptor.forClass(NutritionalGoals.class);
    doNothing().when(macroCalculator).calculateMacroPercentages(goalsCaptor.capture());
    when(nutritionalGoalsRepository.save(goalsCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(any(NutritionalGoals.class)))
        .thenReturn(expectedDto);
    
    // When
    NutritionalGoalsDto result = nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    verify(nutritionalGoalsRepository).save(any(NutritionalGoals.class));
    verify(auditService).logEvent(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistOnUpdate() {
    // Given
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository, never()).findByUser(any());
    verify(nutritionalGoalsRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should update all goal fields (calories, protein, carbs, fats, fiber, sodium, sugar)")
  void shouldUpdateAllGoalFields() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20));
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(existingGoals.getDailyCalories()).isEqualTo(updateDto.getDailyCalories());
    assertThat(existingGoals.getDailyProtein()).isEqualTo(updateDto.getDailyProtein());
    assertThat(existingGoals.getDailyCarbs()).isEqualTo(updateDto.getDailyCarbs());
    assertThat(existingGoals.getDailyFats()).isEqualTo(updateDto.getDailyFats());
    assertThat(existingGoals.getDailyFiber()).isEqualTo(updateDto.getDailyFiber());
    assertThat(existingGoals.getDailySodium()).isEqualTo(updateDto.getDailySodium());
    assertThat(existingGoals.getDailySugar()).isEqualTo(updateDto.getDailySugar());
    
    verify(nutritionalGoalsRepository).save(existingGoals);
  }
  
  @Test
  @DisplayName("Should convert goalType string to enum correctly")
  void shouldConvertGoalTypeStringToEnumCorrectly() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20));
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    updateDto.setGoalType("MUSCLE_GAIN");
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(existingGoals.getGoalType()).isEqualTo(GoalType.MUSCLE_GAIN);
  }
  
  @Test
  @DisplayName("Should validate macro percentages add up to ~100%")
  void shouldValidateMacroPercentagesAddUpTo100Percent() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20)); // Total = 100%
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    NutritionalGoalsDto result = nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    verify(macroCalculator).calculateMacroPercentages(existingGoals);
  }
  
  @Test
  @DisplayName("Should throw ValidationException when macro percentages invalid")
  void shouldThrowValidationExceptionWhenMacroPercentagesInvalid() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(10));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(10));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(10)); // Total = 30% (< 95%)
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    
    // When & Then
    assertThatThrownBy(() -> nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Macro percentages must add up to approximately 100%");
    
    verify(macroCalculator).calculateMacroPercentages(existingGoals);
    verify(nutritionalGoalsRepository, never()).save(any());
  }
  
  @Test
  @DisplayName("Should calculate macro percentages using MacroCalculator")
  void shouldCalculateMacroPercentagesUsingMacroCalculator() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    verify(macroCalculator, times(1)).calculateMacroPercentages(existingGoals);
  }
  
  @Test
  @DisplayName("Should log audit event via AuditService")
  void shouldLogAuditEventViaAuditService() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20));
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    verify(auditService, times(1)).logEvent(
        eq(EventType.PROFILE_UPDATE),
        eq(UserTestFixtures.TEST_USER_ID),
        any(String.class)
    );
  }
  
  @Test
  @DisplayName("Should handle null goalType gracefully")
  void shouldHandleNullGoalTypeGracefully() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20));
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    updateDto.setGoalType(null);
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    NutritionalGoalsDto result = nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(result).isNotNull();
    verify(nutritionalGoalsRepository).save(existingGoals);
  }
  
  @Test
  @DisplayName("Should handle case-insensitive goalType conversion")
  void shouldHandleCaseInsensitiveGoalTypeConversion() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20));
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    updateDto.setGoalType("weight_loss"); // lowercase
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When
    nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    
    // Then
    assertThat(existingGoals.getGoalType()).isEqualTo(GoalType.WEIGHT_LOSS);
  }
  
  // ==================== createDefaultGoals() Tests ====================
  
  @Test
  @DisplayName("Should create default goals for new user")
  void shouldCreateDefaultGoalsForNewUser() {
    // Given
    User user = UserTestFixtures.createUser();
    
    ArgumentCaptor<NutritionalGoals> goalsCaptor = ArgumentCaptor.forClass(NutritionalGoals.class);
    when(nutritionalGoalsRepository.save(goalsCaptor.capture()))
        .thenAnswer(invocation -> {
          NutritionalGoals goals = invocation.getArgument(0);
          goals.setId(1L);
          return goals;
        });
    
    // When
    NutritionalGoals result = nutritionalGoalsService.createDefaultGoals(user);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    NutritionalGoals captured = goalsCaptor.getValue();
    assertThat(captured.getUser()).isEqualTo(user);
    assertThat(user.getNutritionalGoals()).isEqualTo(result);
    
    verify(nutritionalGoalsRepository).save(any(NutritionalGoals.class));
  }
  
  @Test
  @DisplayName("Should set all default nutritional values")
  void shouldSetAllDefaultNutritionalValues() {
    // Given
    User user = UserTestFixtures.createUser();
    
    ArgumentCaptor<NutritionalGoals> goalsCaptor = ArgumentCaptor.forClass(NutritionalGoals.class);
    when(nutritionalGoalsRepository.save(goalsCaptor.capture()))
        .thenAnswer(invocation -> {
          NutritionalGoals goals = invocation.getArgument(0);
          goals.setId(1L);
          return goals;
        });
    
    // When
    nutritionalGoalsService.createDefaultGoals(user);
    
    // Then
    NutritionalGoals captured = goalsCaptor.getValue();
    assertThat(captured.getDailyCalories()).isEqualTo(BigDecimal.valueOf(2000));
    assertThat(captured.getDailyProtein()).isEqualTo(BigDecimal.valueOf(150));
    assertThat(captured.getDailyCarbs()).isEqualTo(BigDecimal.valueOf(250));
    assertThat(captured.getDailyFats()).isEqualTo(BigDecimal.valueOf(67));
    assertThat(captured.getDailyFiber()).isEqualTo(BigDecimal.valueOf(25));
    assertThat(captured.getDailySodium()).isEqualTo(BigDecimal.valueOf(2300));
    assertThat(captured.getDailySugar()).isEqualTo(BigDecimal.valueOf(50));
  }
  
  @Test
  @DisplayName("Should associate goals with user")
  void shouldAssociateGoalsWithUser() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(nutritionalGoalsRepository.save(any(NutritionalGoals.class)))
        .thenAnswer(invocation -> {
          NutritionalGoals goals = invocation.getArgument(0);
          goals.setId(1L);
          return goals;
        });
    
    // When
    NutritionalGoals result = nutritionalGoalsService.createDefaultGoals(user);
    
    // Then
    assertThat(user.getNutritionalGoals()).isEqualTo(result);
    assertThat(result.getUser()).isEqualTo(user);
  }
  
  @Test
  @DisplayName("Should save goals to repository")
  void shouldSaveGoalsToRepository() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(nutritionalGoalsRepository.save(any(NutritionalGoals.class)))
        .thenAnswer(invocation -> {
          NutritionalGoals goals = invocation.getArgument(0);
          goals.setId(1L);
          return goals;
        });
    
    // When
    nutritionalGoalsService.createDefaultGoals(user);
    
    // Then
    verify(nutritionalGoalsRepository, times(1)).save(any(NutritionalGoals.class));
  }
  
  // ==================== deleteNutritionalGoals() Tests ====================
  
  @Test
  @DisplayName("Should delete goals successfully")
  void shouldDeleteGoalsSuccessfully() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(goals));
    doNothing().when(nutritionalGoalsRepository).delete(goals);
    
    // When
    nutritionalGoalsService.deleteNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository).findByUser(user);
    verify(nutritionalGoalsRepository).delete(goals);
    verify(auditService).logEvent(eq(EventType.PROFILE_UPDATE), eq(UserTestFixtures.TEST_USER_ID), any(String.class));
  }
  
  @Test
  @DisplayName("Should throw UserNotFoundException when user doesn't exist")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistOnDelete() {
    // Given
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() -> nutritionalGoalsService.deleteNutritionalGoals(UserTestFixtures.TEST_USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found with id: " + UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository, never()).findByUser(any());
    verify(nutritionalGoalsRepository, never()).delete(any());
  }
  
  @Test
  @DisplayName("Should handle case when goals don't exist (no-op)")
  void shouldHandleCaseWhenGoalsDontExist() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    // When
    nutritionalGoalsService.deleteNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository).findByUser(user);
    verify(nutritionalGoalsRepository, never()).delete(any());
    verify(auditService, never()).logEvent(any(), any(), any());
  }
  
  @Test
  @DisplayName("Should log audit event via AuditService")
  void shouldLogAuditEventViaAuditServiceOnDelete() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals goals = UserTestFixtures.createNutritionalGoals(user);
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(goals));
    doNothing().when(nutritionalGoalsRepository).delete(goals);
    
    // When
    nutritionalGoalsService.deleteNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    // Then
    verify(auditService, times(1)).logEvent(
        eq(EventType.PROFILE_UPDATE),
        eq(UserTestFixtures.TEST_USER_ID),
        any(String.class)
    );
  }
  
  @Test
  @DisplayName("Should not throw exception when goals already deleted")
  void shouldNotThrowExceptionWhenGoalsAlreadyDeleted() {
    // Given
    User user = UserTestFixtures.createUser();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.empty());
    
    // When & Then - should not throw
    nutritionalGoalsService.deleteNutritionalGoals(UserTestFixtures.TEST_USER_ID);
    
    verify(userRepository).findById(UserTestFixtures.TEST_USER_ID);
    verify(nutritionalGoalsRepository).findByUser(user);
    verify(nutritionalGoalsRepository, never()).delete(any());
  }
  
  // ==================== validateMacroPercentages() Tests ====================
  
  @Test
  @DisplayName("Should accept percentages that sum to 100% (within 95-105% range)")
  void shouldAcceptPercentagesThatSumTo100Percent() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(50));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(20)); // Total = 100%
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When & Then - should not throw
    NutritionalGoalsDto result = nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    assertThat(result).isNotNull();
  }
  
  @Test
  @DisplayName("Should reject percentages that sum to less than 95%")
  void shouldRejectPercentagesThatSumToLessThan95Percent() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(30));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(30));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(30)); // Total = 90% (< 95%)
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    
    // When & Then
    assertThatThrownBy(() -> nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Macro percentages must add up to approximately 100%");
  }
  
  @Test
  @DisplayName("Should reject percentages that sum to more than 105%")
  void shouldRejectPercentagesThatSumToMoreThan105Percent() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(BigDecimal.valueOf(40));
    existingGoals.setCarbsPercentage(BigDecimal.valueOf(40));
    existingGoals.setFatsPercentage(BigDecimal.valueOf(30)); // Total = 110% (> 105%)
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    
    // When & Then
    assertThatThrownBy(() -> nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Macro percentages must add up to approximately 100%");
  }
  
  @Test
  @DisplayName("Should handle null percentages gracefully")
  void shouldHandleNullPercentagesGracefully() {
    // Given
    User user = UserTestFixtures.createUser();
    NutritionalGoals existingGoals = UserTestFixtures.createNutritionalGoals(user);
    existingGoals.setProteinPercentage(null);
    existingGoals.setCarbsPercentage(null);
    existingGoals.setFatsPercentage(null);
    
    NutritionalGoalsDto updateDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    NutritionalGoalsDto expectedDto = UserTestFixtures.createUpdatedNutritionalGoalsDto();
    
    when(userRepository.findById(UserTestFixtures.TEST_USER_ID))
        .thenReturn(Optional.of(user));
    when(nutritionalGoalsRepository.findByUser(user))
        .thenReturn(Optional.of(existingGoals));
    doNothing().when(macroCalculator).calculateMacroPercentages(existingGoals);
    when(nutritionalGoalsRepository.save(existingGoals))
        .thenReturn(existingGoals);
    when(nutritionalGoalsMapper.toNutritionalGoalsDto(existingGoals))
        .thenReturn(expectedDto);
    
    // When & Then - should not throw
    NutritionalGoalsDto result = nutritionalGoalsService.updateNutritionalGoals(UserTestFixtures.TEST_USER_ID, updateDto);
    assertThat(result).isNotNull();
  }
}
