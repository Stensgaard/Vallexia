package com.vallexia.user.unit.mapper;

import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.entity.DietaryPreferences;
import com.vallexia.common.enums.SupportedAllergy;
import com.vallexia.common.enums.SupportedCuisineType;
import com.vallexia.common.enums.SupportedDietaryRestriction;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.mapper.DietaryPreferencesMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DietaryPreferencesMapper.
 * Tests entity-to-DTO mapping with real MapStruct implementation.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-11-26
 */
@SpringBootTest(classes = {
    com.vallexia.user.mapper.DietaryPreferencesMapperImpl.class
})
@ActiveProfiles("test")
@DisplayName("DietaryPreferencesMapper Unit Tests")
class DietaryPreferencesMapperTest {
  
  @Autowired
  private DietaryPreferencesMapper dietaryPreferencesMapper;
  
  // ==================== toDietaryPreferencesDto() Tests ====================
  
  @Test
  @DisplayName("Should map all fields from entity to DTO")
  void shouldMapAllFieldsFromEntityToDto() {
    // Given
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences();
    
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(preferences.getId());
    assertThat(dto.getUserId()).isEqualTo(preferences.getUser().getId());
    assertThat(dto.getRestrictions()).isEqualTo(preferences.getRestrictions());
    assertThat(dto.getAllergies()).isEqualTo(preferences.getAllergies());
    assertThat(dto.getPreferredCuisines()).isEqualTo(preferences.getPreferredCuisines());
  }
  
  @Test
  @DisplayName("Should map restrictions set correctly")
  void shouldMapRestrictionsSetCorrectly() {
    // Given
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences();
    Set<SupportedDietaryRestriction> restrictions = new HashSet<>(Set.of(
        SupportedDietaryRestriction.VEGETARIAN,
        SupportedDietaryRestriction.GLUTEN_FREE,
        SupportedDietaryRestriction.KETO
    ));
    preferences.setRestrictions(restrictions);
    
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    
    // Then
    assertThat(dto.getRestrictions()).isNotNull();
    assertThat(dto.getRestrictions()).hasSize(3);
    assertThat(dto.getRestrictions()).containsExactlyInAnyOrderElementsOf(restrictions);
  }
  
  @Test
  @DisplayName("Should map allergies set correctly")
  void shouldMapAllergiesSetCorrectly() {
    // Given
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences();
    Set<SupportedAllergy> allergies = new HashSet<>(Set.of(
        SupportedAllergy.PEANUTS,
        SupportedAllergy.MILK,
        SupportedAllergy.EGGS
    ));
    preferences.setAllergies(allergies);
    
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    
    // Then
    assertThat(dto.getAllergies()).isNotNull();
    assertThat(dto.getAllergies()).hasSize(3);
    assertThat(dto.getAllergies()).containsExactlyInAnyOrderElementsOf(allergies);
  }
  
  @Test
  @DisplayName("Should map preferred cuisines set correctly")
  void shouldMapPreferredCuisinesSetCorrectly() {
    // Given
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences();
    Set<SupportedCuisineType> cuisines = new HashSet<>(Set.of(
        SupportedCuisineType.ITALIAN,
        SupportedCuisineType.JAPANESE,
        SupportedCuisineType.MEXICAN
    ));
    preferences.setPreferredCuisines(cuisines);
    
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    
    // Then
    assertThat(dto.getPreferredCuisines()).isNotNull();
    assertThat(dto.getPreferredCuisines()).hasSize(3);
    assertThat(dto.getPreferredCuisines()).containsExactlyInAnyOrderElementsOf(cuisines);
  }
  
  @Test
  @DisplayName("Should return null when entity is null")
  void shouldReturnNullWhenEntityIsNull() {
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(null);
    
    // Then
    assertThat(dto).isNull();
  }
  
  @Test
  @DisplayName("Should map partial entity with only required fields")
  void shouldMapPartialEntityWithOnlyRequiredFields() {
    // Given
    DietaryPreferences preferences = new DietaryPreferences();
    preferences.setId(1L);
    preferences.setUser(UserTestFixtures.createUser());
    // Leave other fields null
    
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getUserId()).isEqualTo(preferences.getUser().getId());
  }
  
  @Test
  @DisplayName("Should map empty collections correctly")
  void shouldMapEmptyCollectionsCorrectly() {
    // Given
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences();
    preferences.setRestrictions(new HashSet<>());
    preferences.setAllergies(new HashSet<>());
    preferences.setPreferredCuisines(new HashSet<>());
    
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    
    // Then
    assertThat(dto.getRestrictions()).isNotNull();
    assertThat(dto.getRestrictions()).isEmpty();
    assertThat(dto.getAllergies()).isNotNull();
    assertThat(dto.getAllergies()).isEmpty();
    assertThat(dto.getPreferredCuisines()).isNotNull();
    assertThat(dto.getPreferredCuisines()).isEmpty();
  }
  
  @Test
  @DisplayName("Should handle null collections gracefully")
  void shouldHandleNullCollectionsGracefully() {
    // Given
    DietaryPreferences preferences = UserTestFixtures.createDietaryPreferences();
    preferences.setRestrictions(null);
    preferences.setAllergies(null);
    preferences.setPreferredCuisines(null);
    
    // When
    DietaryPreferencesDto dto = dietaryPreferencesMapper.toDietaryPreferencesDto(preferences);
    
    // Then
    assertThat(dto).isNotNull();
    // MapStruct may set null collections to null or empty sets depending on configuration
    // Both behaviors are acceptable
  }
}
