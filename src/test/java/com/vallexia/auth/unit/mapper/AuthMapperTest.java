package com.vallexia.auth.unit.mapper;

import com.vallexia.auth.dto.JwtResponseDto;
import com.vallexia.auth.dto.RegisterRequestDto;
import com.vallexia.auth.fixtures.AuthTestFixtures;
import com.vallexia.auth.mapper.AuthMapper;
import com.vallexia.user.entity.User;
import com.vallexia.user.entity.enums.SubscriptionStatus;
import com.vallexia.user.fixtures.UserTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for AuthMapper.
 * Tests entity-to-DTO mapping with real MapStruct implementation.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-30
 */
@SpringBootTest(classes = {
    com.vallexia.auth.mapper.AuthMapperImpl.class
})
@ActiveProfiles("test")
@DisplayName("AuthMapper Unit Tests")
class AuthMapperTest {
  
  @Autowired
  private AuthMapper authMapper;
  
  // ==================== toUser() Tests ====================
  
  @Test
  @DisplayName("Should map all fields from RegisterRequestDto to User entity")
  void shouldMapAllFieldsFromRegisterRequestDtoToUserEntity() {
    // Given
    RegisterRequestDto dto = AuthTestFixtures.createRegisterRequestDto();
    
    // When
    User user = authMapper.toUser(dto);
    
    // Then
    assertThat(user).isNotNull();
    assertThat(user.getUsername()).isEqualTo(dto.getUsername());
    assertThat(user.getEmail()).isEqualTo(dto.getEmail());
    // Password should be ignored (handled separately)
    assertThat(user.getPasswordHash()).isNull();
  }
  
  @Test
  @DisplayName("Should set default values for security and account fields")
  void shouldSetDefaultValuesForSecurityAndAccountFields() {
    // Given
    RegisterRequestDto dto = AuthTestFixtures.createRegisterRequestDto();
    
    // When
    User user = authMapper.toUser(dto);
    
    // Then
    assertThat(user.getEnabled()).isTrue();
    assertThat(user.getAccountNonExpired()).isTrue();
    assertThat(user.getAccountNonLocked()).isTrue();
    assertThat(user.getCredentialsNonExpired()).isTrue();
    assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
    assertThat(user.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.FREE);
  }
  
  @Test
  @DisplayName("Should ignore system and relationship fields")
  void shouldIgnoreSystemAndRelationshipFields() {
    // Given
    RegisterRequestDto dto = AuthTestFixtures.createRegisterRequestDto();
    
    // When
    User user = authMapper.toUser(dto);
    
    // Then
    assertThat(user.getId()).isNull(); // Should be ignored
    assertThat(user.getPasswordHash()).isNull(); // Should be ignored
    assertThat(user.getRoles()).isEmpty(); // Should be ignored (initialized to empty set in User entity)
    assertThat(user.getDietaryPreferences()).isNull(); // Should be ignored
    assertThat(user.getNutritionalGoals()).isNull(); // Should be ignored
    assertThat(user.getUserSettings()).isNull(); // Should be ignored
    assertThat(user.getAccountLockedUntil()).isNull(); // Should be ignored
    assertThat(user.getSubscriptionExpiresAt()).isNull(); // Should be ignored
    assertThat(user.getCreatedAt()).isNull(); // Should be ignored
    assertThat(user.getUpdatedAt()).isNull(); // Should be ignored
  }
  
  @Test
  @DisplayName("Should handle null DTO")
  void shouldHandleNullDto() {
    // When
    User user = authMapper.toUser(null);
    
    // Then
    assertThat(user).isNull();
  }
  
  @Test
  @DisplayName("Should handle RegisterRequestDto with country field")
  void shouldHandleRegisterRequestDtoWithCountryField() {
    // Given
    RegisterRequestDto dto = AuthTestFixtures.createRegisterRequestDto();
    dto.setCountry("US");
    
    // When
    User user = authMapper.toUser(dto);
    
    // Then
    assertThat(user).isNotNull();
    assertThat(user.getUsername()).isEqualTo(dto.getUsername());
    assertThat(user.getEmail()).isEqualTo(dto.getEmail());
    // Note: Country is not mapped to User entity (it's used separately for UserSettings)
  }
  
  // ==================== toJwtResponse() Tests ====================
  
  @Test
  @DisplayName("Should map all fields from User to JwtResponseDto")
  void shouldMapAllFieldsFromUserToJwtResponseDto() {
    // Given
    User user = UserTestFixtures.createUser();
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    LocalDateTime expiresAt = AuthTestFixtures.TEST_TOKEN_EXPIRES_AT;
    
    // When
    JwtResponseDto dto = authMapper.toJwtResponse(user, accessToken, refreshToken, expiresAt);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getAccessToken()).isEqualTo(accessToken);
    assertThat(dto.getRefreshToken()).isEqualTo(refreshToken);
    assertThat(dto.getTokenType()).isEqualTo("Bearer");
    assertThat(dto.getId()).isEqualTo(user.getId());
    assertThat(dto.getUsername()).isEqualTo(user.getUsername());
    assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    assertThat(dto.getExpiresAt()).isEqualTo(expiresAt);
    assertThat(dto.getSubscriptionStatus()).isEqualTo(user.getSubscriptionStatus().name());
  }
  
  @Test
  @DisplayName("Should map subscription status correctly")
  void shouldMapSubscriptionStatusCorrectly() {
    // Given
    User user = UserTestFixtures.createUser();
    user.setSubscriptionStatus(SubscriptionStatus.PREMIUM);
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    LocalDateTime expiresAt = AuthTestFixtures.TEST_TOKEN_EXPIRES_AT;
    
    // When
    JwtResponseDto dto = authMapper.toJwtResponse(user, accessToken, refreshToken, expiresAt);
    
    // Then
    assertThat(dto.getSubscriptionStatus()).isEqualTo("PREMIUM");
  }
  
  @Test
  @DisplayName("Should handle null subscription status")
  void shouldHandleNullSubscriptionStatus() {
    // Given
    User user = UserTestFixtures.createUser();
    user.setSubscriptionStatus(null);
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    LocalDateTime expiresAt = AuthTestFixtures.TEST_TOKEN_EXPIRES_AT;
    
    // When
    JwtResponseDto dto = authMapper.toJwtResponse(user, accessToken, refreshToken, expiresAt);
    
    // Then
    assertThat(dto.getSubscriptionStatus()).isNull();
  }
  
  @Test
  @DisplayName("Should throw IllegalArgumentException when user is null")
  void shouldThrowIllegalArgumentExceptionWhenUserIsNull() {
    // Given
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    LocalDateTime expiresAt = AuthTestFixtures.TEST_TOKEN_EXPIRES_AT;
    
    // When & Then
    assertThatThrownBy(() -> authMapper.toJwtResponse(null, accessToken, refreshToken, expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User cannot be null");
  }
  
  @Test
  @DisplayName("Should handle null tokens and expiration time")
  void shouldHandleNullTokensAndExpirationTime() {
    // Given
    User user = UserTestFixtures.createUser();
    
    // When
    JwtResponseDto dto = authMapper.toJwtResponse(user, null, null, null);
    
    // Then
    assertThat(dto).isNotNull();
    assertThat(dto.getAccessToken()).isNull();
    assertThat(dto.getRefreshToken()).isNull();
    assertThat(dto.getExpiresAt()).isNull();
    assertThat(dto.getId()).isEqualTo(user.getId());
    assertThat(dto.getUsername()).isEqualTo(user.getUsername());
  }
  
  @Test
  @DisplayName("Should map all subscription statuses correctly")
  void shouldMapAllSubscriptionStatusesCorrectly() {
    // Given
    User user = UserTestFixtures.createUser();
    String accessToken = AuthTestFixtures.TEST_ACCESS_TOKEN;
    String refreshToken = AuthTestFixtures.TEST_REFRESH_TOKEN;
    LocalDateTime expiresAt = AuthTestFixtures.TEST_TOKEN_EXPIRES_AT;
    
    SubscriptionStatus[] statuses = {
        SubscriptionStatus.FREE,
        SubscriptionStatus.PREMIUM,
        SubscriptionStatus.FAMILY,
        SubscriptionStatus.CANCELLED,
        SubscriptionStatus.EXPIRED
    };
    
    for (SubscriptionStatus status : statuses) {
      // Given
      user.setSubscriptionStatus(status);
      
      // When
      JwtResponseDto dto = authMapper.toJwtResponse(user, accessToken, refreshToken, expiresAt);
      
      // Then
      assertThat(dto.getSubscriptionStatus()).isEqualTo(status.name());
      assertThat(dto.getId()).isEqualTo(user.getId());
      assertThat(dto.getUsername()).isEqualTo(user.getUsername());
      assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }
  }
}
