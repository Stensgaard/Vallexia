package com.vallexia.common.unit.controller;

import com.vallexia.common.controller.LocaleConfigController;
import com.vallexia.common.dto.*;
import com.vallexia.common.service.LocaleConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocaleConfigController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 2.0
 * @since 2025-11-25
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LocaleConfigController Unit Tests")
class LocaleConfigControllerTest {

  @Mock
  private LocaleConfigService localeConfigService;

  private LocaleConfigController controller;

  private LocaleConfigDto mockConfig;

  @BeforeEach
  void setUp() {
    // Create mock data
    DietaryRestrictionDto dietaryRestriction = DietaryRestrictionDto.builder()
        .code("VEGAN")
        .name("Vegan")
        .build();
    
    AllergyDto allergy = AllergyDto.builder()
        .code("PEANUTS")
        .name("Peanuts")
        .build();
    
    CuisineTypeDto cuisineType = CuisineTypeDto.builder()
        .code("ITALIAN")
        .name("Italian")
        .build();
    
    GoalTypeDto goalType = GoalTypeDto.builder()
        .code("WEIGHT_LOSS")
        .name("Weight Loss")
        .build();
    
    SubscriptionStatusDto subscriptionStatus = SubscriptionStatusDto.builder()
        .code("ACTIVE")
        .name("Active")
        .build();
    
    MealCategoryDto mealCategory = MealCategoryDto.builder()
        .code("BREAKFAST")
        .name("Breakfast")
        .build();

    mockConfig = LocaleConfigDto.builder()
        .dietaryRestrictions(List.of(dietaryRestriction))
        .allergies(List.of(allergy))
        .cuisineTypes(List.of(cuisineType))
        .goalTypes(List.of(goalType))
        .subscriptionStatuses(List.of(subscriptionStatus))
        .mealCategories(List.of(mealCategory))
        .locales(Collections.emptyList())
        .countries(Collections.emptyList())
        .currencies(Collections.emptyList())
        .timezones(Collections.emptyList())
        .formattingRules(Collections.emptyList())
        .dateFormats(Collections.emptyList())
        .measurementSystems(Collections.emptyList())
        .weightUnits(Collections.emptyList())
        .volumeUnits(Collections.emptyList())
        .countUnits(Collections.emptyList())
        .firstDayOfWeek(Collections.emptyList())
        .build();

    // Mock service methods BEFORE creating controller (since constructor calls buildLocaleConfigSnapshot)
    when(localeConfigService.buildLocaleConfigSnapshot()).thenReturn(mockConfig);
    when(localeConfigService.getDietaryRestrictions()).thenReturn(List.of(dietaryRestriction));
    when(localeConfigService.getAllergies()).thenReturn(List.of(allergy));
    when(localeConfigService.getCuisineTypes()).thenReturn(List.of(cuisineType));
    when(localeConfigService.getGoalTypes()).thenReturn(List.of(goalType));
    when(localeConfigService.getSubscriptionStatuses()).thenReturn(List.of(subscriptionStatus));
    when(localeConfigService.getMealCategories()).thenReturn(List.of(mealCategory));

    // Create controller after mocks are set up
    controller = new LocaleConfigController(localeConfigService);
  }

  // ==================== getLocaleConfig() Endpoint Tests ====================

  @SuppressWarnings("null")
  @Test
  @DisplayName("Should return all metadata sections")
  void shouldReturnAllMetadataSections() {
    // When
    ResponseEntity<LocaleConfigDto> response = controller.getLocaleConfig();

    // Then
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    LocaleConfigDto config = response.getBody();
    assertThat(config).isNotNull();
    assertThat(config.getDietaryRestrictions()).isNotEmpty();
    assertThat(config.getAllergies()).isNotEmpty();
    assertThat(config.getCuisineTypes()).isNotEmpty();
    assertThat(config.getGoalTypes()).isNotEmpty();
    assertThat(config.getSubscriptionStatuses()).isNotEmpty();
    assertThat(config.getMealCategories()).isNotEmpty();
  }

  @Test
  @DisplayName("Should reuse cached snapshot between calls")
  void shouldReuseCachedSnapshot() {
    // When
    LocaleConfigDto first = controller.getLocaleConfig().getBody();
    LocaleConfigDto second = controller.getLocaleConfig().getBody();

    // Then
    assertThat(first).isSameAs(second);
    // Verify service was only called once during controller construction
    verify(localeConfigService, times(1)).buildLocaleConfigSnapshot();
  }

  @Test
  @DisplayName("Should expose cache-control headers")
  void shouldExposeCacheHeaders() {
    // When
    ResponseEntity<LocaleConfigDto> response = controller.getLocaleConfig();

    // Then
    String cacheControl = response.getHeaders().getCacheControl();
    assertThat(cacheControl).isNotBlank();
    assertThat(cacheControl).contains("public");
    assertThat(cacheControl).contains("max-age=21600");
  }

  // ==================== Individual Endpoint Tests ====================

  @Test
  @DisplayName("Should return dietary restrictions")
  void shouldReturnDietaryRestrictions() {
    // When
    var response = controller.getDietaryRestrictions();

    // Then
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  @DisplayName("Should return allergies")
  void shouldReturnAllergies() {
    // When
    var response = controller.getAllergies();

    // Then
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  @DisplayName("Should return cuisine types")
  void shouldReturnCuisineTypes() {
    // When
    var response = controller.getCuisineTypes();

    // Then
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  @DisplayName("Should return goal types")
  void shouldReturnGoalTypes() {
    // When
    var response = controller.getGoalTypes();

    // Then
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  @DisplayName("Should return subscription statuses")
  void shouldReturnSubscriptionStatuses() {
    // When
    var response = controller.getSubscriptionStatuses();

    // Then
    assertThat(response.getBody()).isNotEmpty();
  }
}
