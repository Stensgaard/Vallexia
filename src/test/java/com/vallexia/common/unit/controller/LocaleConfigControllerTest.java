package com.vallexia.common.unit.controller;

import com.vallexia.common.controller.LocaleConfigController;
import com.vallexia.common.dto.LocaleConfigDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LocaleConfigController.
 * Tests REST endpoints with mocked dependencies.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-25
 */
@DisplayName("LocaleConfigController Unit Tests")
class LocaleConfigControllerTest {

  private LocaleConfigController controller;

  @BeforeEach
  void setUp() {
    controller = new LocaleConfigController();
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
    assertThat(config.getDifficultyLevels()).isNotEmpty();
    assertThat(config.getGoalTypes()).isNotEmpty();
    assertThat(config.getSubscriptionStatuses()).isNotEmpty();
    assertThat(config.getMealTypes()).isNotEmpty();
  }

  @Test
  @DisplayName("Should reuse cached snapshot between calls")
  void shouldReuseCachedSnapshot() {
    // When
    LocaleConfigDto first = controller.getLocaleConfig().getBody();
    LocaleConfigDto second = controller.getLocaleConfig().getBody();

    // Then
    assertThat(first).isSameAs(second);
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
  @DisplayName("Should return difficulty levels")
  void shouldReturnDifficultyLevels() {
    // When
    var response = controller.getDifficultyLevels();

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

  @Test
  @DisplayName("Should return meal types")
  void shouldReturnMealTypes() {
    // When
    var response = controller.getMealTypes();

    // Then
    assertThat(response.getBody()).isNotEmpty();
  }
}
