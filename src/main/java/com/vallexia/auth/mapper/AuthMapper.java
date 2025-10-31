package com.vallexia.auth.mapper;

import com.vallexia.auth.dto.JwtResponseDto;
import com.vallexia.auth.dto.RegisterRequestDto;
import com.vallexia.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

/**
 * MapStruct mapper for authentication-related data transformations.
 * 
 * @author Vallexia Team
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {
  
  /**
   * Map RegisterRequestDto to User entity.
   * Note: Password encoding, role assignment, and related entities are handled separately.
   * 
   * @param dto registration request
   * @return user entity (without password, roles, and relationships)
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)  // Handle separately for encoding
  @Mapping(target = "roles", ignore = true)         // Handle separately
  @Mapping(target = "dietaryPreferences", ignore = true)
  @Mapping(target = "nutritionalGoals", ignore = true)
  @Mapping(target = "enabled", constant = "true")
  @Mapping(target = "accountNonExpired", constant = "true")
  @Mapping(target = "accountNonLocked", constant = "true")
  @Mapping(target = "credentialsNonExpired", constant = "true")
  @Mapping(target = "failedLoginAttempts", constant = "0")
  @Mapping(target = "accountLockedUntil", ignore = true)
  @Mapping(target = "subscriptionStatus", constant = "FREE")
  @Mapping(target = "subscriptionExpiresAt", ignore = true)
  @Mapping(target = "householdSize", ignore = true)  // Set to default (1) in User entity
  @Mapping(target = "mealTypes", ignore = true)      // Set to default (BREAKFAST, LUNCH, DINNER) in User entity
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  User toUser(RegisterRequestDto dto);
  
  /**
   * Create JwtResponseDto from User entity and token details.
   * Custom method implementation provided for complex mapping.
   * 
   * @param user the user entity
   * @param accessToken JWT access token
   * @param refreshToken JWT refresh token
   * @param expiresAt token expiration time
   * @return JWT response DTO
   */
  default JwtResponseDto toJwtResponse(User user, String accessToken, 
                                       String refreshToken, LocalDateTime expiresAt) {
    return JwtResponseDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .expiresAt(expiresAt)
        .subscriptionStatus(user.getSubscriptionStatus() != null ? user.getSubscriptionStatus().name() : null)
        .build();
  }
}
