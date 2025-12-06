package com.vallexia.user.mapper;

import com.vallexia.user.dto.UserProfileDto;
import com.vallexia.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between User entity and DTO.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    /**
     * Convert User entity to UserProfileDto.
     * 
     * @param user User entity
     * @return UserProfileDto
     */
    UserProfileDto toUserProfileDto(User user);
    
    /**
     * Convert UserProfileDto to User entity.
     * 
     * @param userProfileDto UserProfileDto
     * @return User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "dietaryPreferences", ignore = true)
    @Mapping(target = "nutritionalGoals", ignore = true)
    @Mapping(target = "userSettings", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(UserProfileDto userProfileDto);
}
