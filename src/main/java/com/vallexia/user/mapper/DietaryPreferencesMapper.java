package com.vallexia.user.mapper;

import com.vallexia.user.dto.DietaryPreferencesDto;
import com.vallexia.user.entity.DietaryPreferences;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between DietaryPreferences entity and DTO.
 * 
 * @author Henrik Stensgaard
 * @version 1.0
 * @since 2025-10-27
 */
@Mapper(componentModel = "spring")
public interface DietaryPreferencesMapper {
    
    /**
     * Convert DietaryPreferences entity to DietaryPreferencesDto.
     * 
     * @param dietaryPreferences DietaryPreferences entity
     * @return DietaryPreferencesDto
     */
    @Mapping(target = "userId", source = "user.id")
    DietaryPreferencesDto toDietaryPreferencesDto(DietaryPreferences dietaryPreferences);
    
    /**
     * Convert DietaryPreferencesDto to DietaryPreferences entity.
     * 
     * @param dietaryPreferencesDto DietaryPreferencesDto
     * @return DietaryPreferences entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DietaryPreferences toDietaryPreferences(DietaryPreferencesDto dietaryPreferencesDto);
}
