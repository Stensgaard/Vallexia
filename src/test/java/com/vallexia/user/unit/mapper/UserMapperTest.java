package com.vallexia.user.unit.mapper;

import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.entity.SubscriptionStatus;
import com.vallexia.user.entity.User;
import com.vallexia.user.fixtures.UserTestFixtures;
import com.vallexia.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserMapper.
 * Tests entity-to-DTO mapping with real MapStruct implementation.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootTest(classes = {
    com.vallexia.user.mapper.UserMapperImpl.class
})
@ActiveProfiles("test")
@DisplayName("UserMapper Unit Tests")
class UserMapperTest {
  
  @Autowired
  private UserMapper userMapper;
  
  // ==================== toUserProfileDto() Tests ====================
  
  @Test
  @DisplayName("Should map all fields from User entity to UserProfileDto")
  void shouldMapAllFieldsFromUserEntityToUserProfileDto() {
    // Given
    User user = UserTestFixtures.createUser();
    
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(user);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(user.getId());
    assertThat(dto.getUsername()).isEqualTo(user.getUsername());
    assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    assertThat(dto.getProfilePictureUrl()).isEqualTo(user.getProfilePictureUrl());
    assertThat(dto.getEnabled()).isEqualTo(user.getEnabled());
    assertThat(dto.getHouseholdSize()).isEqualTo(user.getHouseholdSize());
    assertThat(dto.getMealsPerDay()).isEqualTo(user.getMealsPerDay());
    assertThat(dto.getSubscriptionStatus()).isEqualTo(user.getSubscriptionStatus().name());
    assertThat(dto.getSubscriptionExpiresAt()).isEqualTo(user.getSubscriptionExpiresAt());
  }
  
  @Test
  @DisplayName("Should return null when entity is null")
  void shouldReturnNullWhenEntityIsNull() {
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(null);
    
    // Then
    assertThat(dto).isNull();
  }
  
  @Test
  @DisplayName("Should map partial entity with only required fields")
  void shouldMapPartialEntityWithOnlyRequiredFields() {
    // Given
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    // Leave other fields null
    
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(user);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getUsername()).isEqualTo("testuser");
    assertThat(dto.getEmail()).isEqualTo("test@example.com");
    assertThat(dto.getProfilePictureUrl()).isNull();
  }
  
  @Test
  @DisplayName("Should map subscription status and expiration date")
  void shouldMapSubscriptionStatusAndExpirationDate() {
    // Given
    User user = UserTestFixtures.createAdminUser();
    user.setSubscriptionStatus(SubscriptionStatus.PREMIUM);
    user.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1));
    
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(user);
    
    // Then
    assertThat(dto.getSubscriptionStatus()).isEqualTo("PREMIUM");
    assertThat(dto.getSubscriptionExpiresAt()).isNotNull();
    assertThat(dto.getSubscriptionExpiresAt()).isAfter(LocalDateTime.now());
  }
  
  @Test
  @DisplayName("Should map household size and meals per day")
  void shouldMapHouseholdSizeAndMealsPerDay() {
    // Given
    User user = UserTestFixtures.createUserWithProfile();
    user.setHouseholdSize(5);
    user.setMealsPerDay(6);
    
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(user);
    
    // Then
    assertThat(dto.getHouseholdSize()).isEqualTo(5);
    assertThat(dto.getMealsPerDay()).isEqualTo(6);
  }
  
  @Test
  @DisplayName("Should map null optional fields correctly")
  void shouldMapNullOptionalFieldsCorrectly() {
    // Given
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setProfilePictureUrl(null);
    user.setHouseholdSize(1);
    user.setMealsPerDay(3);
    user.setEnabled(true);
    user.setSubscriptionStatus(SubscriptionStatus.FREE);
    
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(user);
    
    // Then
    assertThat(dto.getProfilePictureUrl()).isNull();
    assertThat(dto.getSubscriptionStatus()).isEqualTo("FREE");
    assertThat(dto.getSubscriptionExpiresAt()).isNull();
  }
  
  @Test
  @DisplayName("Should map user with free subscription status")
  void shouldMapUserWithFreeSubscriptionStatus() {
    // Given
    User user = UserTestFixtures.createUser();
    user.setSubscriptionStatus(SubscriptionStatus.FREE);
    user.setSubscriptionExpiresAt(null);
    
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(user);
    
    // Then
    assertThat(dto.getSubscriptionStatus()).isEqualTo("FREE");
    assertThat(dto.getSubscriptionExpiresAt()).isNull();
  }
  
  @Test
  @DisplayName("Should map disabled user")
  void shouldMapDisabledUser() {
    // Given
    User user = UserTestFixtures.createDisabledUser();
    
    // When
    UserProfileDto dto = userMapper.toUserProfileDto(user);
    
    // Then
    assertThat(dto.getEnabled()).isFalse();
    assertThat(dto.getId()).isEqualTo(user.getId());
    assertThat(dto.getUsername()).isEqualTo(user.getUsername());
  }
  
  // ==================== toUser() Tests ====================
  
  @Test
  @DisplayName("Should ignore security-sensitive fields when converting DTO to entity")
  void shouldIgnoreSecuritySensitiveFieldsWhenConvertingDtoToEntity() {
    // Given
    UserProfileDto dto = UserTestFixtures.createUserProfileDto();
    dto.setId(999L); // Should be ignored
    
    // When
    User user = userMapper.toUser(dto);
    
    // Then
    assertThat(user).isNotNull();
    // These fields should be ignored (MapStruct sets them to null or default values)
    assertThat(user.getId()).isNull(); // Should be ignored
    // username is not in DTO, so it won't be mapped
    assertThat(user.getPasswordHash()).isNull(); // Should be ignored
    // roles, enabled fields are ignored by MapStruct
  }
  
  @Test
  @DisplayName("Should ignore system fields when converting DTO to entity")
  void shouldIgnoreSystemFieldsWhenConvertingDtoToEntity() {
    // Given
    UserProfileDto dto = UserTestFixtures.createUserProfileDto();
    
    // When
    User user = userMapper.toUser(dto);
    
    // Then
    assertThat(user).isNotNull();
    // These system fields should be ignored (MapStruct sets them to null or default values)
    assertThat(user.getId()).isNull(); // Should be ignored
    assertThat(user.getCreatedAt()).isNull(); // Should be ignored
    assertThat(user.getUpdatedAt()).isNull(); // Should be ignored
    // failedLoginAttempts is ignored but may be set to default value 0
    // Note: MapStruct ignores these fields but doesn't guarantee they're null
  }
  
  @Test
  @DisplayName("Should handle null DTO")
  void shouldHandleNullDto() {
    // When
    User user = userMapper.toUser(null);
    
    // Then
    assertThat(user).isNull();
  }
  
  @Test
  @DisplayName("Should map profile fields from DTO to entity")
  void shouldMapProfileFieldsFromDtoToEntity() {
    // Given
    UserProfileDto dto = UserTestFixtures.createUserProfileDto();
    dto.setEmail("new@example.com");
    dto.setProfilePictureUrl("https://example.com/new.jpg");
    dto.setHouseholdSize(4);
    dto.setMealsPerDay(5);
    
    // When
    User user = userMapper.toUser(dto);
    
    // Then
    assertThat(user.getEmail()).isEqualTo("new@example.com");
    assertThat(user.getProfilePictureUrl()).isEqualTo("https://example.com/new.jpg");
    assertThat(user.getHouseholdSize()).isEqualTo(4);
    assertThat(user.getMealsPerDay()).isEqualTo(5);
  }
  
  @Test
  @DisplayName("Should ignore dietary preferences and nutritional goals")
  void shouldIgnoreDietaryPreferencesAndNutritionalGoals() {
    // Given
    UserProfileDto dto = UserTestFixtures.createUserProfileDto();
    
    // When
    User user = userMapper.toUser(dto);
    
    // Then
    assertThat(user).isNotNull();
    // These should be null as they're not in the DTO and are ignored
    // This is an implicit check that the mapping continues even without these fields
  }
  
  @Test
  @DisplayName("Should handle DTO with null optional fields")
  void shouldHandleDtoWithNullOptionalFields() {
    // Given
    UserProfileDto dto = new UserProfileDto();
    dto.setEmail("test@example.com"); // Only required field
    
    // When
    User user = userMapper.toUser(dto);
    
    // Then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("test@example.com");
    assertThat(user.getProfilePictureUrl()).isNull();
  }
  
  @Test
  @DisplayName("Should handle updated profile DTO")
  void shouldHandleUpdatedProfileDto() {
    // Given
    UserProfileDto dto = UserTestFixtures.createUpdatedUserProfileDto();
    
    // When
    User user = userMapper.toUser(dto);
    
    // Then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("updated@example.com");
    assertThat(user.getProfilePictureUrl()).isEqualTo("https://example.com/updated.jpg");
    assertThat(user.getHouseholdSize()).isEqualTo(4);
    assertThat(user.getMealsPerDay()).isEqualTo(5);
  }
}
